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
import kotlin.random.Random

/**
 * This device represents a source of data that provides data
 * outside the existing define sources in [DataType.Source] for a data type
 * that is defined by this extension.
 */
class BespokeDataSource(extension: String, private val id: Int) : SampleDevice {
    override val source by lazy {
        Device(
            extension,
            "$PREFIX-$id",
            listOf(DataType.dataTypeId(extension, BespokeDataType.TYPE_ID)),
            "Bespoke $id",
        )
    }

    /**
     * Connect and start feeding [DeviceEvent]
     *
     * @see [DeviceEvent]
     */
    override fun connect(emitter: Emitter<DeviceEvent>) {
        val job = CoroutineScope(Dispatchers.IO).launch {
            emitter.onNext(OnConnectionStatus(ConnectionStatus.CONNECTED))
            // Start streaming random data
            repeat(Int.MAX_VALUE) {
                emitter.onNext(
                    OnDataPoint(
                        DataPoint(
                            source.dataTypes.first(),
                            values = mapOf(DataType.Field.SINGLE to Random.nextInt(0, 100).toDouble()),
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
        const val PREFIX = "bespoke"
    }
}
