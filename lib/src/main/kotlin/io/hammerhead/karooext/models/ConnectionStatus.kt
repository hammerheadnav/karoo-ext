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
 * Enumeration of device connection status.
 *
 * @see [OnConnectionStatus]
 */
@Serializable
enum class ConnectionStatus {
    /**
     * Source is enabled and connected
     */
    CONNECTED,

    /**
     * Source is enabled but not yet connected
     */
    SEARCHING,

    /**
     * Source is disabled
     */
    DISABLED,

    /**
     * Source is enabled but never connected in time
     */
    DISCONNECTED,
}
