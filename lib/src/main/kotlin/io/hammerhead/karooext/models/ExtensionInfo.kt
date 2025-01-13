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

import android.graphics.drawable.Drawable
import io.hammerhead.karooext.extension.KarooExtension

/**
 * Extension information derived from XML meta-data on [KarooExtension] service.
 */
data class ExtensionInfo(
    /**
     * Extension id
     *
     * Use a string constant in XML.
     */
    val id: String,
    /**
     * Human-readable name for extension.
     *
     * A string resource can be used in XML.
     */
    val displayName: String,
    /**
     * Icon drawable for the extension.
     *
     * Use a drawable resource in XML.
     */
    val icon: Drawable,
    /**
     * Whether this extension scans for devices.
     *
     * @see [KarooExtension.startScan]
     */
    val scansDevices: Boolean,
    /**
     * Whether this extension provides map layer.
     *
     * @see [KarooExtension.startMap]
     * @since 1.1.3
     */
    val mapLayer: Boolean,
    /**
     * Whether this extension writes to the FIT file.
     *
     * @see [KarooExtension.startFit]
     */
    val fitFile: Boolean,
    /**
     * Static list of [DataType]s this extension provides.
     *
     * @see [DataType]
     */
    val dataTypes: List<DataType>,
)
