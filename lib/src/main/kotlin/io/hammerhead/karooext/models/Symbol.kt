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

import androidx.annotation.DrawableRes
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface Symbol {
    /**
     * ID unique to this extension which identifies the symbol
     */
    val id: String

    /**
     * Point of interest which denotes a position and optional types/name information.
     */
    @Serializable
    data class POI(
        /**
         * ID unique to this extension which identifies the symbol
         */
        override val id: String,
        /**
         * Latitude of the point
         */
        val lat: Double,
        /**
         * Longitude of the point
         */
        val lng: Double,
        /**
         * The type of POI
         *
         * @see [Types]
         */
        @SerialName("poiType")
        val type: String = Types.GENERIC,
        /**
         * Optional name of the POI
         */
        val name: String? = null,
        /**
         * Optional distances that a route POI is found along the route polyline
         *
         * Because POIs are considered along the route by proximity they can appear more than once
         *
         * @since 1.1.6
         */
        val distancesAlongRoute: List<Double> = emptyList(),
    ) : Symbol {
        /**
         * Supported string types for POI
         *
         * @see [POI]
         */
        @Suppress("unused")
        object Types {
            const val AID_STATION = "aid_station"
            const val ATM = "atm"
            const val BAR = "bar"
            const val BIKE_PARKING = "bike_parking"
            const val BIKE_SHARE = "bike_share"
            const val BIKE_SHOP = "bike_shop"
            const val CAMPING = "camping"
            const val CAUTION = "caution"
            const val COFFEE = "coffee"
            const val CONTROL = "control"
            const val CONVENIENCE_STORE = "convenience_store"
            const val FERRY = "ferry"
            const val FIRST_AID = "first_aid"
            const val FOOD = "food"
            const val GAS_STATION = "gas_station"
            const val GENERIC = "generic"
            const val GEOCACHE = "geocache"
            const val HOME = "home"
            const val HOSPITAL = "hospital"
            const val LIBRARY = "library"
            const val LODGING = "lodging"
            const val MONUMENT = "monument"
            const val PARK = "park"
            const val PARKING = "parking"
            const val REST_STOP = "rest_stop"
            const val RESTROOM = "restroom"
            const val SHOPPING = "shopping"
            const val SHOWER = "shower"
            const val SUMMIT = "summit"
            const val SWIMMING = "swimming"
            const val TRAILHEAD = "trailhead"
            const val TRANSIT_CENTER = "transit_center"
            const val VIEWPOINT = "viewpoint"
            const val WATER = "water"
            const val WINERY = "winery"
        }
    }

    /**
     * An icon on the map (
     */
    @Serializable
    data class Icon(
        /**
         * ID unique to this extension which identifies the symbol
         */
        override val id: String,
        /**
         * Latitude of the point
         */
        val lat: Double,
        /**
         * Longitude of the point
         */
        val lng: Double,
        /**
         * Resource ID of the drawable for this symbol
         */
        @DrawableRes val iconRes: Int,
        /**
         * Direction the icon is drawn on the map. 0 is North, 90 is East, 180 is South, -90 is West.
         */
        val orientation: Float,
    ) : Symbol
}
