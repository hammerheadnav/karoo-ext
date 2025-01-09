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
 * Request ANT radio be turned on for use by this app.
 *
 * When finished (if applicable), follow with [ReleaseAnt]
 *
 * @since 1.1.2
 */
@Serializable
data class RequestAnt(
    /**
     * Unique string identifier for this resource, used later in [ReleaseAnt]
     */
    val resourceId: String,
) : KarooEffect()

/**
 * Release a previous [RequestAnt] call
 *
 * @since 1.1.2
 */
@Serializable
data class ReleaseAnt(
    /**
     * Unique string identifier for this resource used in [RequestAnt]
     */
    val resourceId: String,
) : KarooEffect()

/**
 * Mark a lap at the current position in ride
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

/**
 * Add a Karoo-style notification to Control Center with optional ability to
 * launch an activity on click.
 */
@Serializable
data class SystemNotification(
    /**
     * Unique ID for this notification, can be used in subsequent calls to update the existing notification.
     */
    val id: String,
    /**
     * First line of messaging for the notification
     */
    val message: String,
    /**
     * Optional second line of detailed messaging for the notification
     */
    val subText: String? = null,
    /**
     * Optional header text to display in top-left of notification
     */
    val header: String? = null,
    /**
     * Specific style to apply to this notification
     */
    val style: Style = Style.EVENT,
    /**
     * Action that the user should take.
     * If null, "Open" will be use when there is an action or "Dismiss" otherwise.
     */
    val action: String? = null,
    /**
     * Optional intent that maps to an activity to launch on click
     */
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

/**
 * Display an important alert in ride app.
 * This should be used for critical messaging related to the current ride.
 */
@Serializable
data class InRideAlert(
    /**
     * Unique string identifier for this alert.
     */
    val id: String,
    /**
     * Icon to show along with the message for this alert.
     */
    @DrawableRes val icon: Int,
    /**
     * The title line of this alert
     */
    val title: String,
    /**
     * Additional information about this alert
     */
    val detail: String?,
    /**
     * Duration (in ms) that this alert should show for
     */
    val autoDismissMs: Long?,
    /**
     * Background color resource
     */
    @ColorRes val backgroundColor: Int,
    /**
     * Text color resource
     */
    @ColorRes val textColor: Int,
) : KarooEffect()

/**
 * Set or clear the custom image URL used in the launcher background.
 */
@Serializable
data class ApplyLauncherBackground(
    /**
     * Fully qualified image URL to load as the custom launcher background or
     * null to clear and revert to default.
     */
    val url: String?,
) : KarooEffect()

/**
 * Switch the currently visible in-ride page to the map, optionally changing zoom if already on map
 */
@Serializable
data class ShowMapPage(
    /**
     * If already on the map page, should the map zoom level be toggled
     */
    val zoom: Boolean = true,
) : KarooEffect()

/**
 * Adjust zoom on currently visible in-ride elements that respond to zoom
 */
@Serializable
data class ZoomPage(
    /**
     * Direction of zoom
     *   true - zoom in
     *   false - zoom out
     */
    val zoomIn: Boolean = true,
) : KarooEffect()

/**
 * Open map pin activity to allow user the choice to navigate to this
 * point or save this point as a POI.
 *
 * @since 1.1.3
 */
@Serializable
data class LaunchPinDrop(
    val pin: Symbol.POI,
) : KarooEffect()
