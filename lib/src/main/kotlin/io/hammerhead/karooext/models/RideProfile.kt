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

package io.hammerhead.karooext.models

import kotlinx.serialization.Serializable

/**
 * Ride profile with settings and page setup
 *
 * Displayed and chosen on launcher in horizontal scroll
 * and take effect shortly after a profile is scrolled too
 * even if a ride isn't started.
 *
 * @since 1.1.5
 */
@Serializable
data class RideProfile(
    /**
     * Unique ID of this profile
     */
    val id: String,
    /**
     * Name given to this profile
     */
    val name: String,
    /**
     * List of each page in the profile
     */
    val pages: List<Page>,
    /**
     * Whether this profile is designated as indoor with no GPS
     */
    val indoor: Boolean,
    /**
     * Configuration of audio alerts
     *
     * @see AudioAlerts
     */
    val audioAlerts: AudioAlerts,
    /**
     * Configuration of auto-pause
     *
     * @see AutoPause
     */
    val autoPause: AutoPause,
    /**
     * List of IDs for sensors which the user has specifically
     * disabled in this profile.
     *
     * @see SavedDevices.SavedDevice.id
     */
    val disabledDevices: List<String>,
    /**
     * Activity type for this profile that is selected at ride review by default.
     *
     * One of: RIDE, EBIKE, MOUNTAIN_BIKE, GRAVEL, EMOUNTAIN_BIKE, VELOMOBILE
     */
    val defaultActivityType: String,
    /**
     * Preference for how routing is configured in this profile.
     *
     * One of: ROAD, GRAVEL, MTB
     */
    val routingPreference: String,
) {
    /**
     * A page in a profile
     */
    @Serializable
    data class Page(
        /**
         * If this is the map page
         */
        val mapPage: Boolean,
        /**
         * List of elements in this page
         *
         * @see Element
         */
        val elements: List<Element>,
    ) {
        /**
         * Element configured in a page
         */
        @Serializable
        data class Element(
            /**
             * Data type of this element
             *
             * @see DataType.Type
             */
            val dataTypeId: String,
            /**
             * Pair of column span x row span
             * Total grid size is 60, so Pair(60, 15) would indicate 1/4 height, full width
             */
            val gridSize: Pair<Int, Int>,
        )
    }

    /**
     * Configuration of audio alerts
     */
    @Serializable
    data class AudioAlerts(
        /**
         * Top-level enabled preference
         */
        val audioAlertsEnabled: Boolean,
        /**
         * Enabled preference for sub-category turn-by-turn
         */
        val tbtEnabled: Boolean,
        /**
         * Enabled preference for sub-category workouts
         */
        val workoutsEnabled: Boolean,
        /**
         * Enabled preference for sub-category radar
         */
        val radarEnabled: Boolean,
        /**
         * Enabled preference for sub-category SLS
         */
        val slsEnabled: Boolean,
        /**
         * Enabled preference for sub-category phone notifications
         */
        val phoneNotifications: Boolean,
        /**
         * Enabled preference for sub-category LEV (eBike)
         */
        val levEnabled: Boolean,
    )

    /**
     * Configuration of auto-pause
     *
     * @see RideState.Paused.auto
     */
    @Serializable
    data class AutoPause(
        /**
         * If auto-pause is used in this profile
         */
        val enabled: Boolean,
        /**
         * The speed (in m/s) under which the ride will be paused
         */
        val speedThreshold: Double,
    )

    /**
     * @suppress
     */
    override fun toString(): String {
        return "RideProfile($id, name=$name)"
    }
}
