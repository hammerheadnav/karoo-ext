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

import io.hammerhead.karooext.extension.KarooExtension
import kotlinx.serialization.Serializable

/**
 * An event for a specific device.
 *
 * @see [KarooExtension.connectDevice]
 */
@Serializable
sealed class DeviceEvent

/**
 * Connection state changed
 *
 * @see [ConnectionStatus]
 */
@Serializable
data class OnConnectionStatus(val status: ConnectionStatus) : DeviceEvent()

/**
 * Battery status changed
 *
 * @see [BatteryStatus]
 */
@Serializable
data class OnBatteryStatus(val status: BatteryStatus) : DeviceEvent()

/**
 * Manufacturer info was fetch or changed
 *
 * @see [ManufacturerInfo]
 */
@Serializable
data class OnManufacturerInfo(val info: ManufacturerInfo) : DeviceEvent()

/**
 * Device produced a new data point.
 *
 * Typical sensors emit data at 1Hz (once per second) and should continue
 * to emit even if the value is unchanged. If data is not emitted for some amount
 * of time, the Karoo system will assume the sensor is idle and treat the view/stream
 * accordingly.
 *
 * @see [DataPoint]
 */
@Serializable
data class OnDataPoint(val dataPoint: DataPoint) : DeviceEvent()
