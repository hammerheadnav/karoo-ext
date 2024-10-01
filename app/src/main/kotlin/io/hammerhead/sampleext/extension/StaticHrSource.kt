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

package io.hammerhead.sampleext.extension

import io.hammerhead.karooext.internal.Emitter
import io.hammerhead.karooext.models.BatteryStatus
import io.hammerhead.karooext.models.ConnectionStatus
import io.hammerhead.karooext.models.DataPoint
import io.hammerhead.karooext.models.DataType
import io.hammerhead.karooext.models.Device
import io.hammerhead.karooext.models.DeviceEvent
import io.hammerhead.karooext.models.ManufacturerInfo
import io.hammerhead.karooext.models.OnBatteryStatus
import io.hammerhead.karooext.models.OnConnectionStatus
import io.hammerhead.karooext.models.OnDataPoint
import io.hammerhead.karooext.models.OnManufacturerInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class StaticHrSource(extension: String, private val hr: Int) {
    val source by lazy {
        Device(
            extension,
            "static-hr-$hr",
            listOf(DataType.Source.HEART_RATE),
            "Static HR $hr",
        )
    }

    /**
     * Connect and start feeding [DeviceEvent]
     *
     * @see [DeviceEvent]
     */
    fun connect(emitter: Emitter<DeviceEvent>) {
        val job = CoroutineScope(Dispatchers.IO).launch {
            // 2s searching
            emitter.onNext(OnConnectionStatus(ConnectionStatus.SEARCHING))
            delay(2000)
            // Update device is now connected
            emitter.onNext(OnConnectionStatus(ConnectionStatus.CONNECTED))
            delay(1000)
            // Update battery status
            emitter.onNext(OnBatteryStatus(BatteryStatus.GOOD))
            delay(1000)
            // Send manufacturer info
            emitter.onNext(OnManufacturerInfo(ManufacturerInfo("Hammerhead", "1234", "HR-EXT-1")))
            delay(1000)
            // Start streaming data
            repeat(Int.MAX_VALUE) {
                emitter.onNext(
                    OnDataPoint(
                        DataPoint(
                            source.dataTypes.first(),
                            values = mapOf(DataType.Field.HEART_RATE to hr.toDouble() + it % 3),
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
        fun fromUid(extension: String, uid: String): StaticHrSource? {
            return uid.substringAfterLast("-").toIntOrNull()?.let {
                StaticHrSource(extension, it)
            }
        }
    }
}
