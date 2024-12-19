package io.hammerhead.karooext.models

import androidx.annotation.DrawableRes
import kotlinx.serialization.Serializable

@Serializable
sealed interface Symbol {
    @Serializable
    data class POI(
        val id: String,
        val lat: Double,
        val lng: Double,
        val type: String = GENERIC,
        val name: String? = null,
    ) : Symbol {
        companion object {
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

    @Serializable
    data class Icon(
        val id: String,
        val lat: Double,
        val lng: Double,
        @DrawableRes val iconRes: Int,
        val orientation: Float,
    ) : Symbol
}
