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

package io.hammerhead.karooext.extension

import android.content.Context
import android.widget.RemoteViews
import io.hammerhead.karooext.internal.Emitter
import io.hammerhead.karooext.internal.ViewEmitter
import io.hammerhead.karooext.models.DataType
import io.hammerhead.karooext.models.StreamState
import io.hammerhead.karooext.models.ViewConfig
import io.hammerhead.karooext.models.ViewEvent

/**
 * Base class for implementation of [DataType].
 *
 * Use for streaming data and custom data type views.
 *
 * If [RemoteViews] are being updated in `startView`, [DataType.graphical] should be true.
 *
 * @see [KarooExtension.types]
 * @sample [dataTypeImplUsage]
 * @sample [visualDataTypeImplUsage]
 */
abstract class DataTypeImpl(
    val extension: String,
    val typeId: String,
) {
    val dataTypeId: String
        get() = DataType.dataTypeId(extension, typeId)

    /**
     * Start emitting [StreamState] events for this data type.
     * This is required for use as a standard numeric view or if any data
     * from this data type needs to be consumed by the custom view or another data type.
     *
     * Streaming will start as soon as a UI element or another streaming data type
     * subscribes to this data type's stream.
     */
    open fun startStream(emitter: Emitter<StreamState>) {}

    /**
     * Start emitting [ViewEvent] events and [RemoteViews] for this data type's custom view.
     *
     * Start is called when a view is attached to a UI (ride app or pages app).
     */
    open fun startView(context: Context, config: ViewConfig, emitter: ViewEmitter) {}
}
