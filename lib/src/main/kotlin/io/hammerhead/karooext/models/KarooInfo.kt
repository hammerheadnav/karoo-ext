package io.hammerhead.karooext.models

import kotlinx.serialization.Serializable

@Serializable
data class KarooInfo(
    val serial: String = "unknown",
    val hardwareType: HardwareType = HardwareType.UNKNOWN,
)
