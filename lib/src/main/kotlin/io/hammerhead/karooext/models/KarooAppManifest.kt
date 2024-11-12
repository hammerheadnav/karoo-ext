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
 * The JSON contract that Karoo System uses when fetching the latest application
 * manifest provided by your application in meta data.
 *
 * @see [io.hammerhead.karooext.MANIFEST_URL_META]
 * @since 1.1.1
 */
@Serializable
data class KarooAppManifest(
    /**
     * The human-readable name of your application
     */
    val label: String,
    /**
     * Reverse-dot annotated package name: com.example.application
     */
    val packageName: String,
    /**
     * URL of the latest hosted APK
     */
    val latestApkUrl: String,
    /**
     * Version name (string) of the latest APK at `latestApkUrl`
     */
    val latestVersion: String,
    /**
     * Version code (numeric) and incrementing, used to determine
     * if an update is needed versus existing installed version code
     */
    val latestVersionCode: Int,
    /**
     * Fully qualified URL of an application icon image
     */
    val iconUrl: String? = null,
    /**
     * Name of publisher/author/developer of application
     */
    val developer: String? = null,
    /**
     * Full text description of the application
     */
    val description: String? = null,
    /**
     * Any notes related to the latest release of the application
     */
    val releaseNotes: String? = null,
    /**
     * List of URLs of images to show with your app details
     */
    val screenshotUrls: List<String>? = null,
)
