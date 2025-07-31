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

package io.hammerhead.karooext.internal

import android.os.Bundle
import android.widget.RemoteViews
import io.hammerhead.karooext.BUNDLE_PACKAGE
import io.hammerhead.karooext.BUNDLE_VALUE
import io.hammerhead.karooext.aidl.IHandler
import io.hammerhead.karooext.models.ViewEvent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber

/**
 * @suppress
 */
@OptIn(ExperimentalSerializationApi::class)
val DefaultJson = Json {
    encodeDefaults = true
    explicitNulls = false
    ignoreUnknownKeys = true
}

/**
 * @suppress
 */
inline fun <reified T> T.bundleWithSerializable(packageName: String): Bundle {
    return Bundle().also {
        it.putString(BUNDLE_VALUE, DefaultJson.encodeToString(this))
        it.putString(BUNDLE_PACKAGE, packageName)
    }
}

/**
 * Interface to allow emitting events by type.
 */
interface Emitter<T> {
    /**
     * Provide a new event to consumers.
     */
    fun onNext(t: T)

    /**
     * Propagate an error [Throwable.message] to consumers.
     */
    fun onError(t: Throwable)

    /**
     * Notify consumers of end of stream.
     */
    fun onComplete()

    /**
     * Set a callback to be invoke on cancellation of this emitter.
     */
    fun setCancellable(cancellable: () -> Unit)

    /**
     * @suppress
     */
    fun cancel()

    /**
     * @suppress
     */
    companion object {
        inline fun <reified T> create(packageName: String, handler: IHandler): Emitter<T> {
            return object : Emitter<T> {
                private var cancellable: (() -> Unit)? = null
                override fun onNext(t: T) {
                    handler.onNext(t.bundleWithSerializable(packageName))
                }

                override fun onError(t: Throwable) {
                    handler.onError(t.message)
                }

                override fun onComplete() {
                    handler.onComplete()
                }

                override fun setCancellable(cancellable: () -> Unit) {
                    this.cancellable = cancellable
                }

                override fun cancel() {
                    cancellable?.invoke()
                }
            }
        }
    }
}

/**
 * Special [Emitter] that includes a function to update [RemoteViews] in addition
 * to [ViewEvent]s.
 *
 * [updateView] can only be called at 1Hz, views emitted more frequently will be dropped.
 */
class ViewEmitter(
    private val packageName: String,
    private val handler: IHandler,
    private val eventEmitter: Emitter<ViewEvent> = Emitter.create<ViewEvent>(packageName, handler),
) : Emitter<ViewEvent> by eventEmitter {
    private var lastViewUpdate: Long = 0

    fun updateView(view: RemoteViews) {
        val now = System.currentTimeMillis()
        // Intention is to limit to ~1Hz with 100ms for slop
        if (now - lastViewUpdate < 900) {
            Timber.w("ViewEmitter: ignoring updateView, too soon")
            return
        }
        lastViewUpdate = now
        val bundle = Bundle()
        bundle.putParcelable("view", view)
        bundle.putString(BUNDLE_PACKAGE, packageName)
        handler.onNext(bundle)
    }
}
