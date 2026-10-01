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
import io.hammerhead.karooext.extension.ViewFrame
import io.hammerhead.karooext.models.DataType
import io.hammerhead.karooext.models.StreamState
import io.hammerhead.karooext.models.UpdateGraphicConfig
import io.hammerhead.karooext.models.ViewConfig
import io.hammerhead.karooext.models.ViewEvent
import io.hammerhead.karooext.streamData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import timber.log.Timber

@OptIn(ExperimentalGlanceRemoteViewsApi::class)
class CustomSpeedDataType(
    private val karooSystem: KarooSystemService,
    extension: String,
) : DataTypeImpl(extension, "custom-speed") {
    private val glance = GlanceRemoteViews()

    override fun streamFlow(): Flow<StreamState> {
        return karooSystem.streamData(DataType.Type.SPEED).map {
            when (it) {
                is StreamState.Streaming -> {
                    // Transform speed data point into
                    it.copy(
                        dataPoint = it.dataPoint.copy(
                            dataTypeId = dataTypeId,
                            values = mapOf(DataType.Field.SINGLE to it.dataPoint.singleValue!!),
                        ),
                    )
                }
                else -> it
            }
        }
    }

    override fun viewEvents(context: Context, config: ViewConfig): Flow<ViewEvent> = flow {
        // Show numeric speed data numerically
        emit(UpdateGraphicConfig(formatDataTypeId = DataType.Type.SPEED))
        // Toggle header config forever
        repeat(Int.MAX_VALUE) {
            emit(UpdateGraphicConfig(showHeader = it % 2 == 0))
            delay(2000)
        }
    }

    override fun viewFlow(context: Context, config: ViewConfig): Flow<ViewFrame> {
        return karooSystem.streamData(DataType.Type.SPEED).map {
            val speed = (it as? StreamState.Streaming)?.dataPoint?.singleValue?.toInt() ?: 0
            ViewFrame {
                Timber.d("Rendering speed view with $speed")
                glance.compose(context, DpSize.Unspecified) {
                    CustomSpeed(speed, config.alignment)
                }.remoteViews
            }
        }
    }
}
