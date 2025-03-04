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

import androidx.annotation.ColorInt
import io.hammerhead.karooext.extension.DataTypeImpl
import kotlinx.serialization.Serializable

/**
 * Events to change how a view is shown in-ride
 *
 * @see [DataTypeImpl.startView]
 */
@Serializable
sealed class ViewEvent

/**
 * Updates the way the graphic data field is shown in the view.
 *
 * Can be sparsely populated to preserve values from previous.
 */
@Serializable
data class UpdateGraphicConfig(
    /**
     * Show data type icon and name.
     *
     * If never non-null, defaults to true
     */
    val showHeader: Boolean? = null,
    /**
     * If [Field.SINGLE] is present in streaming data point (from startStream),
     * control how it is formatted and rendered.
     *
     * Setting this can be used to overlay graphical elements on existing numeric data field treatment
     * and requires streaming data updates from `startStream`.
     *
     * If never included, defaults to null and streaming data is not rendered.
     */
    val formatDataTypeId: String? = null,
) : ViewEvent()

/**
 * Display an alternate message in the standard stream container.
 */
@Serializable
data class ShowCustomStreamState(
    val message: String?,
    @ColorInt val color: Int?,
) : ViewEvent()

/**
 * Update the way a numeric data types are shown in the view.
 */
@Serializable
data class UpdateNumericConfig(
    /**
     * If a single field is present in streaming data point (from startStream),
     * control how it is formatted and rendered. Use an ID string from [DataType.Type]
     * of the matching type which will account for precision and unit conversion.
     *
     * If never applied, defaults to integer precision.
     */
    val formatDataTypeId: String,
) : ViewEvent()
