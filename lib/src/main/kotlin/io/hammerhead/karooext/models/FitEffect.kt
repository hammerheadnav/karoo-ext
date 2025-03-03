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
 * Effects that allow extensions to augment the recorded FIT file for each
 * ride with additional data
 *
 * See [FIT SDK](https://developer.garmin.com/fit/overview/)
 *
 * @since 1.1.4
 */
@Serializable
sealed interface FitEffect

/**
 * Encapsulation of [FitEffect] that have values
 */
@Serializable
sealed interface FitEffectWithValues : FitEffect {
    /**
     * @suppress
     */
    val values: List<FieldValue>

    /**
     * @suppress
     */
    @Transient
    val developerFields: Set<DeveloperField>
        get() = values.mapNotNull { it.developerField }.toSet()

    /**
     * @suppress
     */
    fun copyWith(values: List<FieldValue>): FitEffectWithValues
}

/**
 * Developer Data Fields can be added to any message at runtime through the use of self-describing field definitions.
 * These field definitions are included in the message definitions of the messages that they are used with,
 * allowing for the custom fields to be decoded without the need for any prior knowledge of the Developer Data Fields.
 *
 * [Working With Developer Data Fields](https://developer.garmin.com/fit/cookbook/developer-data/)
 *
 * @since 1.1.4
 */
@Serializable
data class DeveloperField(
    /**
     * Unique field number
     *
     * For a given extension, each associated Field Description should have a sequential Field Definition number.
     * This value is also an index and should start at 0 and increase sequentially by 1.
     * A FIT file can contain up to 255 unique Field Descriptions per extension.
     *
     * The definition of a developer field will be written to the FIT file when first used in any [FitEffectWithValues].
     */
    val fieldDefinitionNumber: Short,
    /**
     * Base type of this data
     *
     * See the Garmin FIT SDK for constants.
     */
    val fitBaseTypeId: Short,
    /**
     * Field name for this developer field
     */
    val fieldName: String,
    /**
     * Units associated with the developer field
     */
    val units: String,
    /**
     * The Native Field Number provides a way to convey which native field the data should be considered as.
     *
     * The Native Field Number is a more deterministic way of conveying this information when compared to the field name and unit properties,
     * which are defined using strings and may vary subtly from vendor to vendor making comparisons difficult.
     */
    val nativeFieldNum: Short? = null,
    /**
     * Populated by Karoo System and ignored if set by extensions
     */
    val developerDataIndex: Short = 0,
)

/**
 * Field and value pair
 *
 * @since 1.1.4
 */
@Suppress("unused")
@Serializable
data class FieldValue(
    /**
     * Standard field number
     */
    val fieldNum: Int,
    /**
     * Value of this data
     *
     * Final FIT file will be casted/rounded based on FIT defined type.
     */
    val value: Double,
    /**
     * Associated developer field if non-standard
     */
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
 * Write a new EventMesg to the FIT file
 *
 * @since 1.1.4
 */
@Serializable
data class WriteEventMesg(
    /**
     * Ordinal of com.garmin.fit.Event from FIT SDK
     */
    val event: Short,
    /**
     * Ordinal of com.garmin.fit.EventType from FIT SDK
     */
    val eventType: Short,
    /**
     * Values associated with this event
     */
    override val values: List<FieldValue>,
) : FitEffectWithValues {
    /**
     * @suppress
     */
    override fun copyWith(values: List<FieldValue>): FitEffectWithValues {
        return copy(values = values)
    }
}

/**
 * Write values to the RecordMesg which are written to the FIT file at 1Hz
 *
 * @since 1.1.4
 */
@Serializable
data class WriteToRecordMesg(
    /**
     * Values to add to the RecordMesg
     */
    override val values: List<FieldValue>,
) : FitEffectWithValues {
    /**
     * Convenience constructor for a single field/value
     */
    constructor(value: FieldValue) : this(listOf(value))

    /**
     * @suppress
     */
    override fun copyWith(values: List<FieldValue>): FitEffectWithValues {
        return copy(values = values)
    }
}

/**
 * Write values to the final SessionMesg for the ride
 *
 * @since 1.1.4
 */
@Serializable
data class WriteToSessionMesg(
    /**
     * Values to add to the SessionMesg
     */
    override val values: List<FieldValue>,
) : FitEffectWithValues {
    /**
     * Convenience constructor for a single field/value
     */
    constructor(value: FieldValue) : this(listOf(value))

    /**
     * @suppress
     */
    override fun copyWith(values: List<FieldValue>): FitEffectWithValues {
        return copy(values = values)
    }
}

/**
 * Internal use by Karoo System when the first [DeveloperField] is used in [FitEffectWithValues]
 *
 * Ignored if emitted by an extension
 */
@Serializable
data class WriteDeveloperDataIdMesg(val developerDataId: Short) : FitEffect

/**
 * Internal use by Karoo System when a new [DeveloperField] is used in [FitEffectWithValues]
 *
 * Ignored if emitted by an extension
 */
@Serializable
data class WriteFieldDescriptionMesg(val developerField: DeveloperField) : FitEffect
