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

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import kotlinx.serialization.Serializable

/**
 * Base sealed class for effects that can be dispatched to the Karoo System.
 */
@Serializable
sealed class KarooEffect

/**
 * Play a beep pattern using the device's internal beeper hardware.
 */
@Serializable
data class PlayBeepPattern(val tones: List<Tone>) : KarooEffect() {
    /**
     * A single tone to play
     */
    @Serializable
    data class Tone(
        /**
         * Frequency of tone or null for quiet
         */
        val frequency: Int?,
        /**
         * Duration of this tone in ms
         */
        val durationMs: Int,
    )
}

/**
 * Perform a hardware action as if the physical action was done by the rider.
 */
@Serializable
sealed class PerformHardwareAction : KarooEffect() {
    /**
     * Simulate top left (page left) button, sometimes called A.
     */
    @Serializable
    data object TopLeftPress : PerformHardwareAction()

    /**
     * Simulate top right (page right) button, sometimes called B.
     */
    @Serializable
    data object TopRightPress : PerformHardwareAction()

    /**
     * Simulate bottom left (back) button, sometimes called C.
     */
    @Serializable
    data object BottomLeftPress : PerformHardwareAction()

    /**
     * Simulate bottom right (accept/navigate in) button, sometimes called D.
     */
    @Serializable
    data object BottomRightPress : PerformHardwareAction()

    /**
     * Simulate control center HW action, key combo of top left and top right.
     */
    @Serializable
    data object ControlCenterComboPress : PerformHardwareAction()

    /**
     * Simulate in-ride drawer action, key combo of bottom left and bottom right.
     */
    @Serializable
    data object DrawerActionComboPress : PerformHardwareAction()
}

/**
 * Turn the screen off
 */
@Serializable
data object TurnScreenOff : KarooEffect()

/**
 * Turn the screen on
 */
@Serializable
data object TurnScreenOn : KarooEffect()

/**
 * Request host BT be turned on for use by this app.
 *
 * When finished (if applicable), follow with [ReleaseBluetooth]
 */
@Serializable
data class RequestBluetooth(
    /**
     * Unique string identifier for this BT resource, used later in [ReleaseBluetooth]
     */
    val resourceId: String,
) : KarooEffect()

/**
 * Release a previous [RequestBluetooth] call
 */
@Serializable
data class ReleaseBluetooth(
    /**
     * Unique string identifier for this BT resource used in [RequestBluetooth]
     */
    val resourceId: String,
) : KarooEffect()

/**
 * Mark at lap at the current position in ride
 */
@Serializable
data object MarkLap : KarooEffect()

/**
 * Pause the currently recording ride (only applicable when [RideState.Recording])
 */
@Serializable
data object PauseRide : KarooEffect()

/**
 * Resume the currently recording ride (only applicable when [RideState.Paused] and [RideState.Paused.auto] is false)
 */
@Serializable
data object ResumeRide : KarooEffect()

@Serializable
data class SystemNotification(
    val id: String,
    val message: String,
    val subText: String? = null,
    val header: String? = null,
    val style: Style = Style.EVENT,
    val action: String? = null,
    val actionIntent: String? = null,
) : KarooEffect() {

    enum class Style {
        EVENT,
        ERROR,
        UPDATE,
        EDUCATION,
        SETUP,
    }
}

@Serializable
data class InRideAlert(
    val id: String,
    @DrawableRes val icon: Int,
    val title: String,
    val detail: String?,
    val autoDismissMs: Long?,
    @ColorRes val backgroundColor: Int,
    @ColorRes val textColor: Int,
) : KarooEffect()
