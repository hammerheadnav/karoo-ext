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

import kotlinx.serialization.Serializable

/**
 * @since 1.1.4
 */
@Serializable
sealed class FitEffect


/**
 * @since 1.1.4
 */
@Serializable
data class DeveloperField(
    val fieldDefinitionNumber: Short,
    val fitBaseTypeId: Short,
    val fieldName: String,
    val units: String,
    val nativeFieldNum: Short? = null,
    /**
     * Do not use. Populated by Karoo System.
     */
    val developerDataIndex: Short = 0,
)

/**
 * @since 1.1.4
 */
@Suppress("unused", "DataClassPrivateConstructor")
@Serializable
data class FieldValue private constructor(
    val fieldNum: Short,
    val value: Double,
    val developerField: DeveloperField?,
) {
    /**
     * Create field value with standard field definition
     */
    constructor(fieldNum: Short, value: Double) : this(fieldNum, value, null)

    /**
     * Create field value for a custom-defined developer field
     */
    constructor(developerField: DeveloperField, value: Double) : this(developerField.fieldDefinitionNumber, value, developerField)
}

/**
 * @since 1.1.4
 */
@Serializable
data class WriteFieldDescriptionMesg(
    val developerField: DeveloperField,
) : FitEffect()

/**
 * @since 1.1.4
 */
@Serializable
data class WriteEventMesg(
    val event: Short,
    val eventType: Short,
    val values: List<FieldValue>,
) : FitEffect()

/**
 * @since 1.1.4
 */
@Serializable
data class WriteToRecordMesg(val values: List<FieldValue>) : FitEffect() {
    constructor(value: FieldValue) : this(listOf(value))
}