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

package io.hammerhead.sampleext.extension

import android.content.Context
import androidx.compose.ui.unit.DpSize
import androidx.glance.appwidget.ExperimentalGlanceRemoteViewsApi
import androidx.glance.appwidget.GlanceRemoteViews
import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.extension.DataTypeImpl
import io.hammerhead.karooext.internal.ViewEmitter
import io.hammerhead.karooext.models.UpdateGraphicConfig
import io.hammerhead.karooext.models.ViewConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Visual countdown button data type.
 *
 * Displays a Glance-composed view that shows the button name normally (blue background),
 * and during the 5-second confirmation countdown shows a color-shifting background
 * plus a segmented progress bar draining from 5 to 0 via [CountdownVisualView].
 *
 * Registered alongside [HaButtonDataType] per button; the user places whichever
 * mode they prefer on their Karoo data page.
 */
@OptIn(ExperimentalGlanceRemoteViewsApi::class)
class HaButtonVisualDataType(
    extension: String,
    typeId: String,
    val button: HaButton,
    val karooSystem: KarooSystemService,
    val context: Context,
) : DataTypeImpl(extension, typeId), HaClickable {

    private val glance = GlanceRemoteViews()
    private var isConfirmationPending = false
    private var confirmationTimeoutJob: Job? = null
    private var interactive = true

    override fun startView(context: Context, config: ViewConfig, emitter: ViewEmitter) {
        Timber.d("Starting visual view for ${button.displayName} ($typeId)")
        // No click handler in preview (page editing) mode, so Karoo gets the long-press to edit.
        interactive = !config.preview
        HaButtonReceiver.registerDataType(typeId, this, ViewEmitterProvider { emitter })
        emitter.onNext(UpdateGraphicConfig(showHeader = false))

        val initJob = CoroutineScope(Dispatchers.IO).launch {
            emitter.updateView(buildView(context, null))
        }

        emitter.setCancellable {
            Timber.d("Stopping visual view for ${button.displayName}")
            initJob.cancel()
            confirmationTimeoutJob?.cancel()
        }
    }

    override fun handleClick(isConfirmation: Boolean, emitter: ViewEmitter) {
        if (isConfirmation) {
            confirmationTimeoutJob?.cancel()
            isConfirmationPending = false
            callHaButton(button, karooSystem, context)
            CoroutineScope(Dispatchers.IO).launch {
                emitter.updateView(buildView(context, null))
            }
        } else {
            if (!isConfirmationPending) {
                isConfirmationPending = true
                confirmationTimeoutJob = CoroutineScope(Dispatchers.IO).launch {
                    for (remaining in 5 downTo 1) {
                        emitter.updateView(buildView(context, remaining))
                        delay(1000)
                    }
                    isConfirmationPending = false
                    emitter.updateView(buildView(context, null))
                }
            }
        }
    }

    private suspend fun buildView(context: Context, remainingSeconds: Int?) =
        glance.compose(context, DpSize.Unspecified) {
            CountdownVisualView(
                buttonName = button.displayName,
                receiverKey = typeId,
                remainingSeconds = remainingSeconds,
                interactive = interactive,
            )
        }.remoteViews
}
