/**
 * Copyright (c) 2024 SRAM LLC.
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

package io.hammerhead.karooext.models

import kotlinx.serialization.Serializable

/**
 * Base sealed class for events that can be consumed from the Karoo System.
 */
@Serializable
sealed class KarooEvent

/**
 * Base sealed class for parameters required to start consuming a specific event from the Karoo System.
 */
@Serializable
sealed class KarooEventParams

/**
 * Observe the current ride state (activity recording).
 *
 * On starting, a consumer will be provided with the current state and then subsequently called
 * when the state changes.
 */
@Serializable
sealed class RideState : KarooEvent() {
    /**
     * Recording not yet started or already finished.
     */
    @Serializable
    data object Idle : RideState()

    /**
     * Ride is actively recording
     */
    @Serializable
    data object Recording : RideState()

    /**
     * Ride is paused
     */
    @Serializable
    data class Paused(
        /**
         * true - ride is paused by auto-pause
         * false - ride is manually paused
         */
        val auto: Boolean,
    ) : RideState()

    /**
     * Default params for [RideState] event listener
     */
    @Serializable
    data object Params : KarooEventParams()
}

/**
 * Listen to lap changes for the current ride.
 *
 * Only called on lap changes.
 */
@Serializable
data class Lap(
    val number: Int,
    val durationMs: Long,
    val trigger: String,
) : KarooEvent() {

    /**
     * Default params for [Lap] event listener
     */
    @Serializable
    data object Params : KarooEventParams()
}

/**
 * Listen to streaming data for a specific data type.
 *
 * Require params [StartStreaming].
 */
@Serializable
data class OnStreamState(val state: StreamState) : KarooEvent() {
    /**
     * Params for [OnStreamState] event listener
     *
     * @see [StreamState]
     */
    @Serializable
    data class StartStreaming(val dataTypeId: String) : KarooEventParams()
}

/**
 * Listen to the currently configured user profile.
 *
 * A consumer will be provided with the current value and then subsequently called on changes.
 */
@Serializable
data class UserProfile(
    /**
     * Rider's configured weight in kilograms
     */
    val weight: Float,
    /**
     * Rider's configured unit system
     *
     * @see PreferredUnit
     */
    val preferredUnit: PreferredUnit,
    /**
     * Rider's configured max heart rate
     */
    val maxHr: Int,
    /**
     * Rider's configured resting heart rate
     */
    val restingHr: Int,
    /**
     * Rider's configured heart rate zones
     *
     * @see Zone
     */
    val heartRateZones: List<Zone>,
    /**
     * Rider's configured functional threshold power
     */
    val ftp: Int,
    /**
     * Rider's configured power zones
     *
     * @see Zone
     */
    val powerZones: List<Zone>,
) : KarooEvent() {

    /**
     * Preferred units split by specific types.
     *
     * When an overall type is selected, all will match.
     */
    @Serializable
    data class PreferredUnit(
        /**
         * Unit used for distance-related information.
         */
        val distance: UnitType,
        /**
         * Unit used for elevation-related information.
         */
        val elevation: UnitType,
        /**
         * Unit used for temperature-related information.
         */
        val temperature: UnitType,
        /**
         * Unit used for weight-related information.
         */
        val weight: UnitType,
    ) {
        /**
         * Choice of units for metric or imperial systems.
         */
        enum class UnitType {
            METRIC,
            IMPERIAL,
        }
    }

    /**
     * Definition of a zone used by power or HR zones
     */
    @Serializable
    data class Zone(val min: Int, val max: Int)

    /**
     * Default params for [UserProfile] event listener
     */
    @Serializable
    data object Params : KarooEventParams()
}

/**
 * Make an HTTP request via Karoo's best network connection.
 *
 * A wifi connection will be used if connected, otherwise, if supported, the request
 * can be performed via BT to a connected companion app. Because of this, HTTP calls
 * made via this method should be:
 *   1. limited in size (<100K, uploading or downloading large files will take a long time)
 *   2. targeted to an in-ride experience that is important to the current ride state
 *
 * Require params [MakeHttpRequest].
 */
@Serializable
data class OnHttpResponse(val state: HttpResponseState) : KarooEvent() {
    /**
     * Params for [OnHttpResponse] event listener
     *
     * @see [HttpResponseState]
     */
    @Serializable
    data class MakeHttpRequest(
        /**
         * HTTP request method: GET, POST, PUT, etc.
         */
        val method: String,
        /**
         * URL to send the request to
         */
        val url: String,
        /**
         * Any custom headers to include
         */
        val headers: Map<String, String> = emptyMap(),
        /**
         * Body of the request
         */
        val body: ByteArray? = null,
        /**
         * Queue this request until a connection becomes available
         */
        val waitForConnection: Boolean = true,
    ) : KarooEventParams() {
        init {
            body?.size?.let {
                check(it <= MAX_REQUEST_SIZE) {
                    "REQUEST_TOO_LARGE"
                }
            }
        }
    }

    companion object {
        // 100KB maximum for request/response body
        const val MAX_REQUEST_SIZE = 100_000
    }
}
