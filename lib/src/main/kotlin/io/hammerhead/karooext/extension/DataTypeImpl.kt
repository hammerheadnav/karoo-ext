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

package io.hammerhead.karooext.extension

import android.content.Context
import android.widget.RemoteViews
import io.hammerhead.karooext.internal.Emitter
import io.hammerhead.karooext.internal.ViewEmitter
import io.hammerhead.karooext.models.DataType
import io.hammerhead.karooext.models.StreamState
import io.hammerhead.karooext.models.ViewConfig
import io.hammerhead.karooext.models.ViewEvent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * A lazily rendered [RemoteViews] frame for a custom data type view.
 *
 * Returning [ViewFrame]s from [DataTypeImpl.viewFlow] defers rendering until a frame is actually
 * sent, so frames dropped by throttling cost nothing. [render] suspends so Glance's
 * `GlanceRemoteViews.compose` can be called directly.
 *
 * @since 1.2.0
 */
fun interface ViewFrame {
    suspend fun render(): RemoteViews
}

/**
 * Base class for implementation of [DataType].
 *
 * Use for streaming data and custom data type views.
 *
 * If [RemoteViews] are being updated in `startView`, [DataType.graphical] should be true.
 *
 * @see [KarooExtension.types]
 * @sample [dataTypeImplUsage]
 * @sample [flowDataTypeImplUsage]
 * @sample [visualDataTypeImplUsage]
 * @sample [flowVisualDataTypeImplUsage]
 */
abstract class DataTypeImpl(
    val extension: String,
    val typeId: String,
) {
    val dataTypeId: String
        get() = DataType.dataTypeId(extension, typeId)

    /**
     * Flow-based alternative to [startStream].
     *
     * Return a [Flow] of [StreamState] and the lib collects it into an [Emitter]. Collection is
     * cancelled when the stream stops. Return null to opt out.
     *
     * @since 1.2.0
     */
    open fun streamFlow(): Flow<StreamState>? = null

    /**
     * Flow-based alternative to emitting views in [startView].
     *
     * Return a [Flow] of [ViewFrame] and the lib renders and forwards the ones it sends, throttled
     * to one update per second. A frame whose [ViewFrame.render] throws is skipped; an exception
     * from the flow itself ends the view with an error. Collection is cancelled when the view
     * stops. Return null to opt out.
     *
     * @since 1.2.0
     */
    open fun viewFlow(context: Context, config: ViewConfig): Flow<ViewFrame>? = null

    /**
     * Flow-based alternative to emitting [ViewEvent]s in [startView].
     *
     * Return a [Flow] of [ViewEvent] and the lib collects it into a [ViewEmitter]. Collection is
     * cancelled when the view stops. Return null to opt out.
     *
     * @since 1.2.0
     */
    open fun viewEvents(context: Context, config: ViewConfig): Flow<ViewEvent>? = null

    /**
     * Start emitting [StreamState] events for this data type.
     * This is required for use as a standard numeric view or if any data
     * from this data type needs to be consumed by the custom view or another data type.
     *
     * Streaming will start as soon as a UI element or another streaming data type
     * subscribes to this data type's stream.
     *
     * By default collects [streamFlow] when provided. Subclasses using [streamFlow] must not
     * override this or call [Emitter.setCancellable] themselves, which replaces the lib's
     * cancellation.
     */
    open fun startStream(emitter: Emitter<StreamState>) {
        val flow = streamFlow() ?: return
        val scope = newScope()
        emitter.setCancellable { scope.cancel() }
        scope.launch {
            try {
                flow.collect { emitNext(emitter, it) }
                runCatching { emitter.onComplete() }
                    .onFailure { Timber.e(it, "$TAG failed to complete stream") }
            } catch (e: CancellationException) {
                currentCoroutineContext().ensureActive()
                reportError(emitter, e)
            } catch (e: Throwable) {
                reportError(emitter, e)
            }
        }
    }

    /**
     * Start emitting [ViewEvent] events and [RemoteViews] for this data type's custom view.
     *
     * Start is called when a view is attached to a UI (ride app or pages app).
     *
     * By default collects [viewFlow] and [viewEvents] when provided. The view stays attached until
     * it is stopped, even if both flows complete. Subclasses using those flows must not override
     * this or call [Emitter.setCancellable] themselves, which replaces the lib's cancellation.
     */
    open fun startView(context: Context, config: ViewConfig, emitter: ViewEmitter) {
        val frames = viewFlow(context, config)
        val events = viewEvents(context, config)
        if (frames == null && events == null) return
        val scope = newScope()
        emitter.setCancellable { scope.cancel() }
        scope.launch {
            try {
                coroutineScope {
                    frames?.let {
                        launchCollector {
                            it.conflate().collect { frame ->
                                val view = renderFrame(frame)
                                if (view != null) {
                                    // A slow render can finish after stopView
                                    currentCoroutineContext().ensureActive()
                                    emitter.updateView(view)
                                }
                                delay(VIEW_UPDATE_SPACING_MS)
                            }
                        }
                    }
                    events?.let {
                        launchCollector {
                            it.collect { event -> emitNext(emitter, event) }
                        }
                    }
                }
                // A view stays attached until stopView, even once both flows finish
                awaitCancellation()
            } catch (e: CancellationException) {
                currentCoroutineContext().ensureActive()
                reportError(emitter, e)
            } catch (e: Throwable) {
                reportError(emitter, e)
            }
        }
    }

    // A child ending in a foreign CancellationException would otherwise just stop silently
    private fun CoroutineScope.launchCollector(block: suspend () -> Unit) = launch {
        try {
            block()
        } catch (e: CancellationException) {
            currentCoroutineContext().ensureActive()
            throw IllegalStateException("View flow cancelled", e)
        }
    }

    // Render failures skip the frame; upstream, onNext and updateView failures end the view
    private suspend fun renderFrame(frame: ViewFrame): RemoteViews? {
        return try {
            frame.render()
        } catch (e: CancellationException) {
            currentCoroutineContext().ensureActive()
            Timber.e(e, "$TAG failed to render view")
            null
        } catch (e: Exception) {
            Timber.e(e, "$TAG failed to render view")
            null
        }
    }

    // Every launch catches Throwable, so no handler or supervisor is needed
    private fun newScope(): CoroutineScope = CoroutineScope(Job() + Dispatchers.IO)

    // Rethrow so collection stops; the caller reports the error once
    private fun <T> emitNext(emitter: Emitter<T>, value: T) {
        try {
            emitter.onNext(value)
        } catch (e: Exception) {
            Timber.e(e, "$TAG failed to emit value")
            throw e
        }
    }

    private fun reportError(emitter: Emitter<*>, e: Throwable) {
        runCatching { emitter.onError(e) }
            .onFailure { Timber.e(it, "$TAG failed to emit error") }
    }

    private companion object {
        const val TAG = "DataTypeImpl"
        const val VIEW_UPDATE_SPACING_MS = 1_000L
    }
}
