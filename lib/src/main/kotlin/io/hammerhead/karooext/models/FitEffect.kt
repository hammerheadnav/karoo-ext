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
import kotlinx.serialization.Transient

/**
 * @since 1.1.4
 */
@Serializable
sealed interface FitEffect

@Serializable
sealed interface FitEffectWithValues : FitEffect {
    val values: List<FieldValue>

    @Transient
    val developerFields: Set<DeveloperField>
        get() = values.mapNotNull { it.developerField }.toSet()

    fun copyWith(values: List<FieldValue>): FitEffectWithValues
}

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
    val fieldNum: Int,
    val value: Double,
    val developerField: DeveloperField?,
) {
    /**
     * Create field value with standard field definition
     */
    constructor(fieldNum: Int, value: Double) : this(fieldNum, value, null)

    /**
     * Create field value for a custom-defined developer field
     */
    constructor(developerField: DeveloperField, value: Double) : this(developerField.fieldDefinitionNumber.toInt(), value, developerField)
}

/**
 * @since 1.1.4
 */
@Serializable
data class WriteEventMesg(
    val event: Short,
    val eventType: Short,
    override val values: List<FieldValue>,
) : FitEffectWithValues {
    override fun copyWith(values: List<FieldValue>): FitEffectWithValues {
        return copy(values = values)
    }
}

/**
 * @since 1.1.4
 */
@Serializable
data class WriteToRecordMesg(
    override val values: List<FieldValue>,
) : FitEffectWithValues {
    constructor(value: FieldValue) : this(listOf(value))

    override fun copyWith(values: List<FieldValue>): FitEffectWithValues {
        return copy(values = values)
    }
}

/**
 * @since 1.1.4
 */
@Serializable
data class WriteToSessionMesg(
    override val values: List<FieldValue>,
) : FitEffectWithValues {
    constructor(value: FieldValue) : this(listOf(value))

    override fun copyWith(values: List<FieldValue>): FitEffectWithValues {
        return copy(values = values)
    }
}

/**
 * @suppress
 */
@Serializable
data class WriteDeveloperDataIdMesg(val developerDataId: Short) : FitEffect

/**
 * @suppress
 */
@Serializable
data class WriteFieldDescriptionMesg(val developerField: DeveloperField) : FitEffect
