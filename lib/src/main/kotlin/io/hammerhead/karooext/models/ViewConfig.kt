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

package io.hammerhead.karooext.models

import io.hammerhead.karooext.extension.DataTypeImpl
import kotlinx.serialization.Serializable

/**
 * Configuration details about the view passed on start
 *
 * @see [DataTypeImpl.startView]
 */
@Serializable
data class ViewConfig(
    /**
     * Pair of column span x row span
     * Total grid size is 60, so Pair(60, 15) would indicate 1/4 height, full width
     */
    val gridSize: Pair<Int, Int>,
    /**
     * Size (in pixels) of the current view as configured in the user profile
     */
    val viewSize: Pair<Int, Int>,
    /**
     * Font size used in standard numeric view of this grid size in sp
     */
    val textSize: Int,
    /**
     * User-configured alignment of this data field
     * @since 1.1.2
     */
    val alignment: Alignment = Alignment.RIGHT,
    /**
     * Whether the user has configured their data field to include boundaries
     *
     * Useful if the style/design of a custom graphic field depends on the precense of borders
     *
     * @since 1.1.2
     */
    val boundariesEnabled: Boolean = false,
    /**
     * Whether the view is in preview mode (page editing) or in ride
     *
     * @since 1.1.2
     */
    val preview: Boolean = false,
) {
    /**
     * Alignment of data fields in-ride profiles
     * @since 1.1.2
     */
    @Serializable
    enum class Alignment {
        /**
         * Number/graphic aligned on the left side of the view
         */
        LEFT,

        /**
         * Number/graphic aligned in the center of the view
         */
        CENTER,

        /**
         * Number/graphic aligned on the right side of the view
         *
         * This is the default if the user hasn't changed their alignment and is also the default on
         * any device running karoo-ext prior to 1.1.2
         */
        RIGHT,
    }
}
