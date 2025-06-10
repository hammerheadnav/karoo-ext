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
 * Enumeration of battery status.
 *
 * @see [OnBatteryStatus]
 */
@Serializable
enum class BatteryStatus {
    NEW,
    GOOD,
    OK,
    LOW,
    CRITICAL,
    INVALID,
    ;

    companion object {
        fun fromPercentage(percentage: Int): BatteryStatus {
            return when {
                percentage > 95 -> NEW
                percentage > 80 -> GOOD
                percentage > 45 -> OK
                percentage > 15 -> LOW
                percentage > 0 -> CRITICAL
                else -> INVALID
            }
        }
    }
}
