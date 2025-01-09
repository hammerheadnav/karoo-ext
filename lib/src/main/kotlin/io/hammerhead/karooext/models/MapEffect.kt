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
import kotlinx.serialization.Serializable

/**
 * Effect classes for use in [io.hammerhead.karooext.extension.KarooExtension] call `startMap`
 *
 * @since 1.1.3
 */
@Serializable
sealed class MapEffect

/**
 * Show a list of symbols on the map.
 *
 * This can be called again with the same ID to update symbols.
 *
 * @see [Symbol]
 * @since 1.1.3
 */
@Serializable
data class ShowSymbols(
    /**
     * List of symbols
     *
     * @see [Symbol]
     */
    val symbols: List<Symbol>,
) : MapEffect()

/**
 * Remove symbols by `id` that were previously added with [ShowSymbols]
 *
 * @since 1.1.3
 */
@Serializable
data class HideSymbols(
    /**
     * List of symbol IDs from [ShowSymbols]
     */
    val symbolIds: List<String>,
) : MapEffect()

/**
 * Show a polyline on the map with style
 *
 * @since 1.1.3
 */
@Serializable
data class ShowPolyline(
    /**
     * Unique ID of the polyline within this extension
     */
    val id: String,
    /**
     * Google Encoded polyline format of a list of points. [Spec](https://developers.google.com/maps/documentation/utilities/polylinealgorithm)
     *
     * Precision 5.
     */
    val encodedPolyline: String,
    /**
     * Resolved color of the polyline
     */
    @ColorInt val color: Int,
    /**
     * Thickness of the polyline
     */
    val width: Int,
) : MapEffect()

/**
 * Hide a previously shown polyline
 *
 * @see [ShowPolyline]
 * @since 1.1.3
 */
@Serializable
data class HidePolyline(val id: String) : MapEffect()
