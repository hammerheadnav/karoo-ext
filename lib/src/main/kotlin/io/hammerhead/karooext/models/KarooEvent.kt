/**
 * Copyright (c) 2024 Hammerhead Navigation Inc.
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
