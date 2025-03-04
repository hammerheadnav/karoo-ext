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

@Serializable
data class RideProfile(
    val id: String,
    val name: String,
    val pages: List<Page>,
    val indoor: Boolean,
    val audioAlerts: AudioAlerts,
    val autoPause: AutoPause,
    val disabledDevices: List<String>,
    val defaultActivityType: String,
    val routingPreference: String,
) {
    @Serializable
    data class Page(
        val mapPage: Boolean,
        val elements: List<Element>,
    ) {
        @Serializable
        data class Element(
            /**
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

    @Serializable
    data class AudioAlerts(
        val audioAlertsEnabled: Boolean,
        val tbtEnabled: Boolean,
        val workoutsEnabled: Boolean,
        val radarEnabled: Boolean,
        val slsEnabled: Boolean,
        val phoneNotifications: Boolean,
        val levEnabled: Boolean,
    )

    @Serializable
    data class AutoPause(
        val enabled: Boolean,
        val speedThreshold: Double,
    )

    override fun toString(): String {
        return "RideProfile($id, name=$name)"
    }
}
