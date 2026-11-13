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

import android.content.Context
import io.hammerhead.sampleext.BuildConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber

@Serializable
data class ButtonConfig(
    val actionId: String,
    val displayName: String,
    val domain: String,
    val service: String,
    val entityId: String? = null,
)

fun ButtonConfig.toHaButton() = HaButton(actionId, displayName, domain, service, entityId)

/** Full URL of a Home Assistant REST endpoint, e.g. `apiUrl("services/light/toggle")`. */
fun AppConfig.apiUrl(path: String) = "${baseUrl.trim().trimEnd('/')}/api/$path"

/** Headers sent with every Home Assistant request. */
fun AppConfig.requestHeaders(): Map<String, String> = buildMap {
    // No token: authentication may be injected by a proxy; an empty
    // "Bearer " header would make Home Assistant reject the request.
    if (accessToken.isNotBlank()) {
        put("Authorization", "Bearer ${accessToken.trim()}")
    }
    put("Content-Type", "application/json")
    // Optional extra header (e.g. for a reverse proxy / Cloudflare Access). Added last so it
    // can deliberately override a default header.
    if (customHeaderKey.isNotBlank()) {
        put(customHeaderKey.trim(), customHeaderValue.trim())
    }
}

@Serializable
data class AppConfig(
    val baseUrl: String = BuildConfig.HA_BASE_URL,
    val accessToken: String = BuildConfig.HA_ACCESS_TOKEN,
    val customHeaderKey: String = "",
    val customHeaderValue: String = "",
    val buttons: List<ButtonConfig> = defaultButtons,
) {
    companion object {
        val defaultButtons = listOf(
            ButtonConfig("ha-btn-1", "Garage", "button", "press", "button.garage_impuls"),
            ButtonConfig("ha-btn-2", "Lights", "light", "toggle", "light.outdoor"),
            ButtonConfig("ha-btn-3", "Fan", "switch", "toggle", "switch.fan"),
        )

        private const val PREFS_NAME = "ha_config"
        private const val KEY_CONFIG = "config"

        private val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

        fun load(context: Context): AppConfig {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val raw = prefs.getString(KEY_CONFIG, null) ?: return AppConfig()
            return try {
                json.decodeFromString(raw)
            } catch (e: Exception) {
                Timber.e(e, "Failed to parse AppConfig, using defaults")
                AppConfig()
            }
        }

        fun save(context: Context, config: AppConfig) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_CONFIG, json.encodeToString(config)).apply()
        }

        fun reset(context: Context) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().clear().apply()
        }
    }
}
