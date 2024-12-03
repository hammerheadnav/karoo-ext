/**
 * Copyright (c) 2024 SRAM LLC.
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

package io.hammerhead.sampleext.extension

import io.hammerhead.karooext.internal.Emitter
import io.hammerhead.karooext.models.BatteryStatus
import io.hammerhead.karooext.models.ConnectionStatus
import io.hammerhead.karooext.models.DataPoint
import io.hammerhead.karooext.models.DataType
import io.hammerhead.karooext.models.Device
import io.hammerhead.karooext.models.DeviceEvent
import io.hammerhead.karooext.models.OnConnectionStatus
import io.hammerhead.karooext.models.OnDataPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * This class demonstrates providing a shifting data which increments each
 * front/rear gear within the max range.
 *
 * This is an example of providing more than one data type for the device along with multiple
 * values in each data point.
 */
class IncrementalShiftingSource(extension: String, private val id: Int) : SampleDevice {
    override val source by lazy {
        Device(
            extension,
            "$PREFIX-$id",
            listOf(
                DataType.Source.SHIFTING_FRONT_GEAR,
                DataType.Source.SHIFTING_REAR_GEAR,
                DataType.Source.SHIFTING_BATTERY,
            ),
            "Inc. Shifting $id",
        )
    }

    /**
     * Connect and start feeding [DeviceEvent]
     *
     * @see [DeviceEvent]
     */
    override fun connect(emitter: Emitter<DeviceEvent>) {
        val job = CoroutineScope(Dispatchers.IO).launch {
            // Update device is now connected
            emitter.onNext(OnConnectionStatus(ConnectionStatus.CONNECTED))
            // Start streaming data
            repeat(Int.MAX_VALUE) {
                emitter.onNext(
                    OnDataPoint(
                        DataPoint(
                            DataType.Type.SHIFTING_FRONT_GEAR,
                            values = mapOf(
                                DataType.Field.SHIFTING_FRONT_GEAR to 1 + it % 2.0,
                                DataType.Field.SHIFTING_FRONT_GEAR_MAX to 2.0,
                            ),
                            sourceId = source.uid,
                        ),
                    ),
                )
                emitter.onNext(
                    OnDataPoint(
                        DataPoint(
                            DataType.Type.SHIFTING_REAR_GEAR,
                            values = mapOf(
                                DataType.Field.SHIFTING_REAR_GEAR to 1 + it % 12.0,
                                DataType.Field.SHIFTING_REAR_GEAR_MAX to 12.0,
                            ),
                            sourceId = source.uid,
                        ),
                    ),
                )
                val rearBattery = (1.0 + it) % BatteryStatus.entries.size
                emitter.onNext(
                    OnDataPoint(
                        DataPoint(
                            DataType.Type.SHIFTING_BATTERY,
                            values = mapOf(
                                DataType.Field.SHIFTING_BATTERY_STATUS to rearBattery,
                                DataType.Field.SHIFTING_BATTERY_STATUS_FRONT_DERAILLEUR to it.toDouble() % BatteryStatus.entries.size,
                                DataType.Field.SHIFTING_BATTERY_STATUS_REAR_DERAILLEUR to rearBattery,
                            ),
                            sourceId = source.uid,
                        ),
                    ),
                )
                delay(1000)
            }
            awaitCancellation()
        }
        emitter.setCancellable {
            job.cancel()
        }
    }

    companion object {
        const val PREFIX = "shift"
    }
}
