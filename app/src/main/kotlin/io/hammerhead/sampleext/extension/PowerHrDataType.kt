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

import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.extension.DataTypeImpl
import io.hammerhead.karooext.internal.Emitter
import io.hammerhead.karooext.models.DataPoint
import io.hammerhead.karooext.models.DataType
import io.hammerhead.karooext.models.StreamState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber

class PowerHrDataType(
    private val karooSystem: KarooSystemService,
    extension: String,
    private val scope: CoroutineScope,
) : DataTypeImpl(extension, "power-hr") {
    override fun startStream(emitter: Emitter<StreamState>) {
        Timber.d("start powerhr stream")
        val job = scope.launch {
            val hrFlow = karooSystem.streamDataFlow(DataType.Type.HEART_RATE)
            val powerFlow = karooSystem.streamDataFlow(DataType.Type.POWER)
            combine(hrFlow, powerFlow) { hr, power ->
                val hrValue = (hr as? StreamState.Streaming)?.dataPoint?.singleValue
                val powerValue = (power as? StreamState.Streaming)?.dataPoint?.singleValue
                if (hrValue != null && powerValue != null) {
                    StreamState.Streaming(
                        DataPoint(
                            dataTypeId,
                            values = mapOf(DataType.Field.SINGLE to hrValue * powerValue / 100.0),
                        ),
                    )
                } else {
                    StreamState.NotAvailable
                }
            }.collect {
                emitter.onNext(it)
            }
        }
        emitter.setCancellable {
            Timber.d("stop powerhr stream")
            job.cancel()
        }
    }
}
