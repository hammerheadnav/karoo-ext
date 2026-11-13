/**
 * Copyright (c) 2025 SRAM LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.hammerhead.sampleext

import timber.log.Timber
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.BindException
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.net.URLDecoder
import java.security.SecureRandom
import java.util.concurrent.atomic.AtomicInteger

/**
 * Tiny single-threaded HTTP server that lets the user type long values (URL, access token,
 * proxy header) on their phone instead of on the Karoo.
 *
 * The server only runs while the "Set up from phone" dialog is open, every submission must carry
 * a one-time PIN shown on the Karoo, and it shuts itself down after [MAX_FAILED_ATTEMPTS] wrong PINs.
 * Secrets are never sent back to the browser.
 */
class PhoneSetupServer(
    private val prefillBaseUrl: String,
    private val prefillHeaderKey: String,
    private val onReceived: (SetupValues) -> Unit,
    private val onLockedOut: () -> Unit,
) {
    /** Values submitted from the phone. `null` means "leave this field unchanged". */
    data class SetupValues(
        val baseUrl: String?,
        val accessToken: String?,
        val headerKey: String?,
        val headerValue: String?,
    )

    val pin: String = SecureRandom().nextInt(1_000_000).toString().padStart(6, '0')

    @Volatile
    private var serverSocket: ServerSocket? = null
    private val failedAttempts = AtomicInteger(0)

    /** Starts listening and returns the bound port. */
    fun start(): Int {
        val socket = try {
            ServerSocket(DEFAULT_PORT)
        } catch (e: BindException) {
            Timber.w(e, "Port $DEFAULT_PORT busy, using a random port")
            ServerSocket(0)
        }
        serverSocket = socket
        Thread({ acceptLoop(socket) }, "phone-setup-server").apply {
            isDaemon = true
            start()
        }
        return socket.localPort
    }

    fun stop() {
        val socket = serverSocket ?: return
        serverSocket = null
        try {
            socket.close()
        } catch (ignored: IOException) {
        }
    }

    private fun acceptLoop(socket: ServerSocket) {
        while (!socket.isClosed) {
            val client = try {
                socket.accept()
            } catch (ignored: IOException) {
                break // socket closed by stop()
            }
            // One thread per connection: browsers open idle pre-connect sockets that would otherwise
            // block the real request until the socket timeout fires.
            Thread({
                try {
                    client.use { handle(it) }
                } catch (e: Exception) {
                    Timber.w(e, "Phone setup request failed")
                }
            }, "phone-setup-request").apply {
                isDaemon = true
                start()
            }
        }
    }

    private fun handle(client: Socket) {
        client.soTimeout = 10_000
        // ISO-8859-1 maps bytes 1:1 to chars, so Content-Length (bytes) == chars to read.
        val reader = BufferedReader(InputStreamReader(client.getInputStream(), Charsets.ISO_8859_1))
        val requestLine = reader.readLine() ?: return
        val parts = requestLine.split(" ")
        if (parts.size < 2) return respond(client, 400, page("Bad request"))
        val method = parts[0]
        val path = parts[1].substringBefore('?')

        var contentLength = 0
        while (true) {
            val line = reader.readLine() ?: break
            if (line.isEmpty()) break
            val idx = line.indexOf(':')
            if (idx > 0 && line.substring(0, idx).trim().equals("Content-Length", ignoreCase = true)) {
                contentLength = line.substring(idx + 1).trim().toIntOrNull() ?: 0
            }
        }

        when {
            path != "/" -> respond(client, 404, page("Not found"))
            method == "GET" -> respond(client, 200, formPage(error = null))
            method == "POST" -> {
                if (contentLength !in 0..MAX_BODY_BYTES) {
                    return respond(client, 413, page("Request too large"))
                }
                val buf = CharArray(contentLength)
                var read = 0
                while (read < contentLength) {
                    val n = reader.read(buf, read, contentLength - read)
                    if (n < 0) break
                    read += n
                }
                val form = try {
                    parseForm(String(buf, 0, read))
                } catch (e: IllegalArgumentException) {
                    return respond(client, 400, page("Malformed form data"))
                }
                handleSubmit(client, form)
            }
            else -> respond(client, 405, page("Method not allowed"))
        }
    }

    private fun handleSubmit(client: Socket, form: Map<String, String>) {
        if (form["pin"]?.trim() != pin) {
            if (failedAttempts.incrementAndGet() >= MAX_FAILED_ATTEMPTS) {
                respond(client, 403, page("Too many wrong PINs. Setup stopped — reopen it on the Karoo."))
                stop()
                onLockedOut()
            } else {
                respond(client, 403, formPage(error = "Wrong PIN"))
            }
            return
        }
        fun field(name: String) = form[name]?.trim()?.takeIf { it.isNotEmpty() }
        onReceived(
            SetupValues(
                baseUrl = field("baseUrl"),
                accessToken = field("accessToken"),
                headerKey = field("headerKey"),
                headerValue = field("headerValue"),
            ),
        )
        respond(client, 200, page("Values sent ✔<br>Check them on the Karoo and tap <b>Save</b>."))
    }

    private fun parseForm(body: String): Map<String, String> =
        body.split('&')
            .filter { it.isNotEmpty() }
            .associate { pair ->
                val key = pair.substringBefore('=')
                val value = pair.substringAfter('=', "")
                URLDecoder.decode(key, "UTF-8") to URLDecoder.decode(value, "UTF-8")
            }

    private fun respond(client: Socket, status: Int, html: String) {
        val reason = when (status) {
            200 -> "OK"
            400 -> "Bad Request"
            403 -> "Forbidden"
            404 -> "Not Found"
            405 -> "Method Not Allowed"
            413 -> "Payload Too Large"
            else -> "Error"
        }
        val body = html.toByteArray(Charsets.UTF_8)
        val header = "HTTP/1.1 $status $reason\r\n" +
            "Content-Type: text/html; charset=utf-8\r\n" +
            "Content-Length: ${body.size}\r\n" +
            "Cache-Control: no-store\r\n" +
            "Connection: close\r\n\r\n"
        client.getOutputStream().apply {
            write(header.toByteArray(Charsets.ISO_8859_1))
            write(body)
            flush()
        }
    }

    private fun formPage(error: String?): String = page(
        """
        ${error?.let { "<p class=\"err\">${escape(it)}</p>" } ?: ""}
        <form method="post" action="/" autocomplete="off">
          <label>PIN shown on the Karoo
            <input name="pin" inputmode="numeric" required></label>
          <label>HA Base URL
            <input name="baseUrl" type="url" value="${escape(prefillBaseUrl)}"
                   placeholder="https://homeassistant.example.com"></label>
          <label>Home Assistant Token <small>(HA profile → Security → Long-lived access tokens)</small>
            <textarea name="accessToken" rows="4" placeholder="Leave empty to keep current"></textarea></label>
          <label>Proxy Header Name <small>(optional, e.g. for Cloudflare)</small>
            <input name="headerKey" value="${escape(prefillHeaderKey)}" placeholder="CF-Access-Client-Secret"></label>
          <label>Proxy Header Value
            <textarea name="headerValue" rows="3" placeholder="Leave empty to keep current"></textarea></label>
          <button type="submit">Send to Karoo</button>
        </form>
        """.trimIndent(),
    )

    private fun page(content: String): String = """
        <!doctype html>
        <html><head><meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Karoo HA setup</title>
        <style>
          body{font-family:system-ui,sans-serif;margin:0 auto;max-width:480px;padding:16px;}
          label{display:block;margin:12px 0;font-weight:600;}
          input,textarea{display:block;width:100%;box-sizing:border-box;margin-top:4px;padding:10px;
            font:inherit;font-weight:normal;border:1px solid #999;border-radius:6px;}
          button{width:100%;padding:12px;font:inherit;font-weight:600;border:0;border-radius:6px;
            background:#03a9f4;color:#fff;margin-top:8px;}
          .err{color:#c62828;font-weight:600;}
        </style></head>
        <body><h2>Home Assistant → Karoo</h2>$content</body></html>
    """.trimIndent()

    private fun escape(s: String) = s
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")

    companion object {
        private const val DEFAULT_PORT = 8080
        private const val MAX_BODY_BYTES = 16 * 1024
        private const val MAX_FAILED_ATTEMPTS = 5

        /** First site-local IPv4 address of an active interface (normally the WiFi address). */
        fun localIpv4(): String? = try {
            NetworkInterface.getNetworkInterfaces()?.toList().orEmpty()
                .filter { it.isUp && !it.isLoopback }
                .flatMap { it.inetAddresses.toList() }
                .filterIsInstance<Inet4Address>()
                .firstOrNull { it.isSiteLocalAddress }
                ?.hostAddress
        } catch (e: Exception) {
            Timber.w(e, "Could not determine local IP")
            null
        }
    }
}
