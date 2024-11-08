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

@file:Suppress("unused")

package io.hammerhead.karooext

/**
 * Current version of the karoo-ext library dependency.
 */
const val EXT_LIB_VERSION: String = BuildConfig.LIB_VERSION

/**
 * The filter used by the Karoo System to find extensions.
 * This should be included in your AndroidManifest.xml like [app](app/src/main/AndroidManifest.xml)
 *
 * ```xml
 * <intent-filter>
 *     <action android:name="io.hammerhead.karooext.KAROO_EXTENSION" />
 * </intent-filter>
 * ```
 */
const val KAROO_EXTENSION_INTENT_FILTER = "io.hammerhead.karooext.KAROO_EXTENSION"

/**
 * The meta-data on your extension that Karoo System uses for static resources of your extension.
 * This should be included in your AndroidManifest.xml like [app](app/src/main/AndroidManifest.xml)
 *
 * ```xml
 * <meta-data
 *    android:name="io.hammerhead.karooext.EXTENSION_INFO"
 *    android:resource="@xml/extension_info"/>
 * ```
 */
const val EXTENSION_INFO_META_KEY = "io.hammerhead.karooext.EXTENSION_INFO"

/**
 * @suppress
 */
const val BUNDLE_VALUE = "value"

/**
 * @suppress
 */
const val BUNDLE_PACKAGE = "package"
