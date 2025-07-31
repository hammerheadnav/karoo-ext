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

import io.hammerhead.karooext.models.OnHttpResponse.MakeHttpRequest
import io.hammerhead.karooext.models.OnStreamState.StartStreaming
import io.hammerhead.karooext.models.UserProfile.PreferredUnit
import io.hammerhead.karooext.models.UserProfile.Zone
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

/**
 * Observe the current position and orientation
 *
 * @since 1.1.3
 */
@Serializable
data class OnLocationChanged(
    /**
     * Latitude of current position
     */
    val lat: Double,
    /**
     * Longitude of current position
     */
    val lng: Double,
    /**
     * Current orientation, heading, direction
     *
     * - 0 is North, 180 is South
     */
    val orientation: Double?,
) : KarooEvent() {

    /**
     * Default params for [OnLocationChanged] event listener
     */
    @Serializable
    data object Params : KarooEventParams()
}

/**
 * Observe the rider's POIs save to their account
 *
 * @see [Symbol.POI]
 * @since 1.1.3
 */
@Serializable
data class OnGlobalPOIs(
    /**
     * List of POIs
     *
     * @see [Symbol.POI]
     */
    val pois: List<Symbol.POI>,
) : KarooEvent() {

    /**
     * Default params for [OnGlobalPOIs] event listener
     */
    @Serializable
    data object Params : KarooEventParams()
}

/**
 * Observe the state of navigation: route selection or destination
 *
 * @since 1.1.3
 */
@Serializable
data class OnNavigationState(
    /**
     * Current state
     *
     * @see [NavigationState]
     * @since 1.1.3
     */
    val state: NavigationState,
) : KarooEvent() {
    /**
     * Encapsulation of navigation state options
     *
     * @since 1.1.3
     */
    @Serializable
    sealed class NavigationState {
        /**
         * No navigation is currently running
         */
        @Serializable
        data object Idle : NavigationState()

        /**
         * Navigating a saved route
         */
        @Serializable
        data class NavigatingRoute(
            /**
             * Google encoded polyline, precision 5, of the selected route.
             */
            val routePolyline: String,
            /**
             * Distance (in meters) of the full route.
             */
            val routeDistance: Double,
            /**
             * Pair of distance, elevation, encoded as Google polyline, precision 1, for the selected route.
             *
             * @since 1.1.6
             */
            val routeElevationPolyline: String? = null,
            /**
             * Google encoded polyline, precision 5, of the path to navigate back to the route.
             *
             * Null when on route or off route and using breadcrumb navigation.
             */
            val rejoinPolyline: String?,
            /**
             * Distance along `routePolyline` that `rejoinPolyline` meets.
             */
            val rejoinDistance: Double?,
            /**
             * Name of the route
             */
            val name: String,
            /**
             * Whether navigating in reverse
             */
            val reversed: Boolean,
            /**
             * If breadcrumb navigation is being used (disabled turn-by-turn)
             */
            val breadcrumb: Boolean,
            /**
             * POIs associated with the route
             *
             * @see [Symbol.POI] [OnGlobalPOIs]
             */
            val pois: List<Symbol.POI>,
            /**
             * Climbs along the route
             *
             * @since 1.1.6
             */
            val climbs: List<Climb> = emptyList(),
        ) : NavigationState() {
            /**
             * @suppress
             */
            override fun toString(): String {
                return "NavigatingRoute($name, routePolyline=[${routePolyline.length}], routeDistance=$routeDistance, routeElevation=[${routeElevationPolyline?.length}], rejoinPolyline=[${rejoinPolyline?.length}], rejoinDistance=$rejoinDistance, reversed=$reversed, breadcrumb=$breadcrumb, pois=${pois.map { "POI(${it.name ?: it.type}, dists=${it.distancesAlongRoute.map { it.toInt() }})" }}, climbs=$climbs)"
            }
        }

        /**
         * Navigation to a destination POI
         */
        @Serializable
        data class NavigatingToDestination(
            /**
             * Destination the rider selected to navigate to.
             */
            val destination: Symbol.POI,
            /**
             * The polyline from the rider's original location to the destination.
             *
             * This will change if the rider deviates from the previous suggested path to the destination.
             */
            val polyline: String,
            /**
             * Pair of distance, elevation, encoded as Google polyline, precision 1, along the suggested path.
             *
             * @since 1.1.6
             */
            val elevationPolyline: String? = null,
            /**
             * Climbs along the path to destination
             *
             * @since 1.1.6
             */
            val climbs: List<Climb> = emptyList(),
        ) : NavigationState() {
            /**
             * @suppress
             */
            override fun toString(): String {
                return "NavigatingToDestination($destination, polyline=[${polyline.length}], elevationPolyline=[${elevationPolyline?.length}], climbs=$climbs)"
            }
        }

        /**
         * Data for a climb within a route
         *
         * @since 1.1.6
         */
        @Serializable
        data class Climb(
            /**
             * Distance along the route (m)
             */
            val startDistance: Double,
            /**
             * Length of the climb (m)
             */
            val length: Double,
            /**
             * Average grade over the climb (%)
             */
            val grade: Double,
            /**
             * Total ascent of the climb (m)
             */
            val totalElevation: Double,
        )
    }

    /**
     * Default params for [OnNavigationState] event listener
     *
     * @since 1.1.3
     */
    @Serializable
    data object Params : KarooEventParams()
}

/**
 * Observe the zoom level of the map
 *
 * @since 1.1.3
 */
@Serializable
data class OnMapZoomLevel(
    /**
     * Zoom level: [8.0, 18.0] where smaller is more zoomed out and larger is zoomed in.
     *
     * Example: the map page default cycle of zooms uses value [13.0, 15.0, 16.0]
     * but manual adjustments can go beyond this to the full range.
     */
    val zoomLevel: Double,
) : KarooEvent() {
    /**
     * Default params for [OnMapZoomLevel] event listener
     */
    @Serializable
    data object Params : KarooEventParams()
}

/**
 * Observe saved devices
 *
 * @since 1.1.5
 */
@Serializable
data class SavedDevices(
    /**
     * List of save devices
     *
     * @see SavedDevice
     */
    val devices: List<SavedDevice>,
) : KarooEvent() {
    /**
     * A device added by the user and saved in the device list
     */
    @Serializable
    data class SavedDevice(
        /**
         * ID of the device
         *
         * @see Device.fromDeviceUid
         */
        val id: String,
        /**
         * The connection for this sensor
         *
         * One of: ANT_PLUS, BLE, OTHER, EXTENSION
         */
        val connectionType: String,
        /**
         * The name of the sensor
         */
        val name: String,
        /**
         * If the sensor is currently enabled
         */
        val enabled: Boolean,
        /**
         * Details of the top-level (or only) device
         */
        val details: DeviceDetail,
        /**
         * Associated components and their details
         *
         * Null if single device
         */
        val components: Map<String, DeviceDetail>?,
        /**
         * Data types that this sensor provides
         *
         * @see DataType.Type
         */
        val supportedDataTypes: List<String>,
        /**
         * Optional configured gearing info
         *
         * @see GearInfo
         */
        val gearInfo: GearInfo?,
    ) {
        /**
         * Saved device details
         */
        @Serializable
        data class DeviceDetail(
            /**
             * Battery status at last check
             *
             * @see lastBatteryUpdate
             */
            val lastBattery: BatteryStatus?,
            /**
             * Timestamp (ms since epoch) that `lastBattery` was updated
             */
            val lastBatteryUpdate: Long?,
            /**
             * Manufacturer name
             */
            val manufacturer: String?,
            /**
             * Serial number
             */
            val serialNumber: String?,
        )

        /**
         * Saved gear info
         */
        @Serializable
        data class GearInfo(
            /**
             * Number of chainrings
             */
            val maxFrontGears: Int,
            /**
             * Number of cogs in the cassette
             */
            val maxRearGears: Int,
            /**
             * User-provided teeth in each chainring (size matches `maxFrontGears`)
             */
            val frontTeeth: List<Int>?,
            /**
             * User-provided teeth in each cog of cassette (size matches `maxRearGears`)
             */
            val rearTeeth: List<Int>?,
        )
    }

    /**
     * Default params for [SavedDevices] event listener
     */
    @Serializable
    data object Params : KarooEventParams()
}

/**
 * Observe bikes
 *
 * @since 1.1.5
 */
@Serializable
data class Bikes(
    /**
     * List of all bikes
     *
     * @see Bike
     */
    val bikes: List<Bike>,
) : KarooEvent() {
    /**
     * A saved bike
     */
    @Serializable
    data class Bike(
        /**
         * Unique ID
         */
        val id: String,
        /**
         * Name of the bike
         */
        val name: String,
        /**
         * Distance saved to this bike on Karoo in meters.
         */
        val odometer: Double,
    )

    /**
     * Default params for [Bikes] event listener
     */
    @Serializable
    data object Params : KarooEventParams()
}

/**
 * Observe the active ride profile
 *
 * @since 1.1.5
 */
@Serializable
data class ActiveRideProfile(
    /**
     * The current profile that is active (selected by user on launcher)
     */
    val profile: RideProfile,
) : KarooEvent() {
    /**
     * Default params for [ActiveRideProfile] event listener
     */
    @Serializable
    data object Params : KarooEventParams()
}

/**
 * Observe the visible ride page
 *
 * @since 1.1.5
 */
@Serializable
data class ActiveRidePage(
    /**
     * The current page with a profile that is visible to the user
     */
    val page: RideProfile.Page,
) : KarooEvent() {
    /**
     * Default params for [ActiveRidePage] event listener
     */
    @Serializable
    data object Params : KarooEventParams()
}
