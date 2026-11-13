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

package io.hammerhead.sampleext.extension

import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.models.HttpResponseState
import io.hammerhead.karooext.models.OnHttpResponse
import timber.log.Timber

data class ConnectionTestResult(val success: Boolean, val message: String)

/**
 * Calls `GET /api/` (answers `{"message": "API running."}` when the URL and token are valid) through the
 * Karoo system, i.e. the same route the buttons use.
 *
 * [onResult] may be called on a binder thread. Returns the consumer id so the caller can cancel the
 * request with [KarooSystemService.removeConsumer], or `null` if the request was not started.
 */
fun testHaConnection(
    karooSystem: KarooSystemService,
    config: AppConfig,
    onResult: (ConnectionTestResult) -> Unit,
): String? {
    if (config.baseUrl.isBlank()) {
        onResult(ConnectionTestResult(false, "Enter the HA Base URL first"))
        return null
    }
    val url = config.apiUrl("")
    val listenerRef = object {
        var id: String = ""
    }
    listenerRef.id = karooSystem.addConsumer(
        OnHttpResponse.MakeHttpRequest(
            method = "GET",
            url = url,
            headers = config.requestHeaders(),
            waitForConnection = false,
        ),
        onError = { error ->
            karooSystem.removeConsumer(listenerRef.id)
            onResult(ConnectionTestResult(false, "Error: $error"))
        },
    ) { response: OnHttpResponse ->
        val state = response.state as? HttpResponseState.Complete ?: return@addConsumer
        karooSystem.removeConsumer(listenerRef.id)
        Timber.d("Connection test $url -> ${state.statusCode} ${state.error ?: ""}")
        onResult(interpret(state))
    }
    return listenerRef.id
}

private fun interpret(state: HttpResponseState.Complete): ConnectionTestResult {
    val body = state.body?.toString(Charsets.UTF_8)?.trim().orEmpty()
    // HTML bodies (e.g. a Cloudflare block page) are noise on the small screen.
    val snippet = body.takeIf { it.isNotEmpty() && !it.startsWith("<") }?.take(140)?.let { ": $it" } ?: ""
    return when {
        state.error != null && state.statusCode !in 100..599 ->
            ConnectionTestResult(false, "Not reachable: ${state.error}")
        state.statusCode in 200..299 && body.contains("API running") ->
            ConnectionTestResult(true, "Connected — Home Assistant API is running")
        state.statusCode in 200..299 ->
            // e.g. a reverse-proxy login page (missing/wrong proxy header) or a wrong base URL
            ConnectionTestResult(false, "HTTP ${state.statusCode}, but this is not the HA API — check URL and proxy header")
        state.statusCode == 401 ->
            ConnectionTestResult(false, "401 Unauthorized from Home Assistant — check the Home Assistant token$snippet")
        state.statusCode == 403 ->
            ConnectionTestResult(false, "403 Forbidden — check the proxy header$snippet")
        state.statusCode == 404 ->
            ConnectionTestResult(false, "404 Not found — check the base URL (without /api)")
        else ->
            ConnectionTestResult(false, "HTTP ${state.statusCode}${state.error?.let { " ($it)" } ?: ""}$snippet")
    }
}
