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

import kotlinx.serialization.Serializable

/**
 * Container for streaming data updates.
 */
@Serializable
data class DataPoint(
    /**
     * Type of data represented.
     *
     * Note, this is full data type id, not extension `typeId`.
     *
     * @see [DataType]
     */
    val dataTypeId: String,
    /**
     * Mapping of all current field -> values
     *
     * @see [Field]
     */
    val values: Map<String, Double> = emptyMap(),
    /**
     * Unique identifier of the source of this data
     */
    val sourceId: String? = null,
) {
    /**
     * Helper to access a single value when a data type is known to have only one.
     */
    val singleValue: Double?
        get() = values.values.firstOrNull()
}
