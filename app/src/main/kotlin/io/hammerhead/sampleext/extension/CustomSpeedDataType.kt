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

import android.content.Context
import androidx.compose.ui.unit.DpSize
import androidx.glance.appwidget.ExperimentalGlanceRemoteViewsApi
import androidx.glance.appwidget.GlanceRemoteViews
import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.extension.DataTypeImpl
import io.hammerhead.karooext.internal.Emitter
import io.hammerhead.karooext.internal.ViewEmitter
import io.hammerhead.karooext.models.DataType
import io.hammerhead.karooext.models.StreamState
import io.hammerhead.karooext.models.UpdateGraphicConfig
import io.hammerhead.karooext.models.ViewConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalGlanceRemoteViewsApi::class)
class CustomSpeedDataType(
    private val karooSystem: KarooSystemService,
    extension: String,
) : DataTypeImpl(extension, "custom-speed") {
    private val glance = GlanceRemoteViews()

    override fun startStream(emitter: Emitter<StreamState>) {
        Timber.d("start speed stream")
        val job = CoroutineScope(Dispatchers.IO).launch {
            karooSystem.streamDataFlow(DataType.Type.SPEED).collect {
                when (it) {
                    is StreamState.Streaming -> {
                        // Transform speed data point into
                        emitter.onNext(
                            it.copy(
                                dataPoint = it.dataPoint.copy(
                                    dataTypeId = dataTypeId,
                                    values = mapOf(DataType.Field.SINGLE to it.dataPoint.singleValue!!),
                                ),
                            ),
                        )
                    }
                    else -> emitter.onNext(it)
                }
            }
        }
        emitter.setCancellable {
            Timber.d("stop speed stream")
            job.cancel()
        }
    }

    override fun startView(context: Context, config: ViewConfig, emitter: ViewEmitter) {
        Timber.d("Starting speed view with $emitter and config $config")
        val configJob = CoroutineScope(Dispatchers.IO).launch {
            // Show numeric speed data numerically
            emitter.onNext(UpdateGraphicConfig(formatDataTypeId = DataType.Type.SPEED))
            // Toggle header config forever
            repeat(Int.MAX_VALUE) {
                emitter.onNext(UpdateGraphicConfig(showHeader = it % 2 == 0))
                delay(2000)
            }
            awaitCancellation()
        }
        val viewJob = CoroutineScope(Dispatchers.IO).launch {
            karooSystem.streamDataFlow(DataType.Type.SPEED).collect {
                val speed = (it as? StreamState.Streaming)?.dataPoint?.singleValue?.toInt() ?: 0
                Timber.d("Updating speed view ($emitter) with $speed")
                val result = glance.compose(context, DpSize.Unspecified) {
                    CustomSpeed(speed, config.alignment)
                }
                emitter.updateView(result.remoteViews)
            }
        }
        emitter.setCancellable {
            Timber.d("Stopping speed view with $emitter")
            configJob.cancel()
            viewJob.cancel()
        }
    }
}
