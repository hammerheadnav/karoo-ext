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

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import kotlinx.serialization.Serializable

@Serializable
sealed class MapEffect

@Serializable
data class ShowSymbols(
    val symbols: List<Symbol>,
) : MapEffect() {
    @Serializable
    data class Symbol(
        val point: NamedCoordinates,
        @DrawableRes val customIcon: Int? = null,
        val orientation: Double? = null,
        val clickable: Boolean = false,
    )
}

@Serializable
data class ShowPolyline(
    val id: String,
    val encodedPolyline: String,
    @ColorInt val color: Int,
    val width: Int,
) : MapEffect()

@Serializable
data class HidePolyline(val id: String) : MapEffect()
