package io.hammerhead.karooext.models

import kotlinx.serialization.Serializable

@Serializable
data class NamedCoordinates(
    val lat: Double,
    val lng: Double,
    val name: String? = null,
)
