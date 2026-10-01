/**
 * Copyright (c) 2026 SRAM LLC.
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
import android.content.ContextWrapper
import android.os.Bundle
import android.os.IBinder
import android.widget.RemoteViews
import io.hammerhead.karooext.aidl.IHandler
import io.hammerhead.karooext.internal.Emitter
import io.hammerhead.karooext.internal.ViewEmitter
import io.hammerhead.karooext.models.ShowCustomStreamState
import io.hammerhead.karooext.models.ViewConfig
import io.hammerhead.karooext.models.ViewEvent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Collections
import java.util.concurrent.atomic.AtomicInteger

class DataTypeImplViewFlowTest {
    private val context: Context = ContextWrapper(null)
    private val config = ViewConfig(gridSize = 60 to 15, viewSize = 480 to 120, textSize = 24)

    private class RecordingHandler(private val failOnNext: Boolean = false) : IHandler {
        val views = AtomicInteger()

        override fun onNext(bundle: Bundle?) {
            if (failOnNext) throw RuntimeException("dead handler")
            views.incrementAndGet()
        }

        override fun onError(msg: String?) {}

        override fun onComplete() {}

        override fun asBinder(): IBinder? = null
    }

    private class RecordingEvents : Emitter<ViewEvent> {
        val events: MutableList<ViewEvent> = Collections.synchronizedList(mutableListOf())

        @Volatile
        var error: Throwable? = null

        @Volatile
        var completed = false

        @Volatile
        var recordedCancellable: (() -> Unit)? = null

        override fun onNext(t: ViewEvent) {
            events.add(t)
        }

        override fun onError(t: Throwable) {
            error = t
        }

        override fun onComplete() {
            completed = true
        }

        override fun setCancellable(cancellable: () -> Unit) {
            recordedCancellable = cancellable
        }

        override fun cancel() {
            recordedCancellable?.invoke()
        }
    }

    private suspend fun waitUntil(condition: () -> Boolean) {
        withTimeout(5_000) {
            while (!condition()) delay(10)
        }
    }

    @Test
    fun finishedViewEventsDoNotCompleteView() = runBlocking<Unit> {
        val event = ShowCustomStreamState("hi", null)
        val impl = object : DataTypeImpl("test", "events") {
            override fun viewEvents(context: Context, config: ViewConfig): Flow<ViewEvent> = flowOf(event)
        }
        val events = RecordingEvents()

        impl.startView(context, config, ViewEmitter("pkg", RecordingHandler(), events))
        waitUntil { events.events.isNotEmpty() }
        delay(100)

        assertEquals(listOf<ViewEvent>(event), events.events.toList())
        assertFalse(events.completed)
        assertNotNull(events.recordedCancellable)
        events.cancel()
    }

    @Test
    fun droppedFramesAreNotRendered() = runBlocking<Unit> {
        val renders = AtomicInteger()
        val firstRendered = CompletableDeferred<Unit>()
        val frame = ViewFrame {
            renders.incrementAndGet()
            firstRendered.complete(Unit)
            RemoteViews("pkg", 0)
        }
        val impl = object : DataTypeImpl("test", "lazy") {
            override fun viewFlow(context: Context, config: ViewConfig): Flow<ViewFrame> = flow {
                emit(frame)
                // Rest arrive while the loop waits out its 1s spacing
                firstRendered.await()
                repeat(19) { emit(frame) }
            }
        }
        val handler = RecordingHandler()
        val events = RecordingEvents()

        impl.startView(context, config, ViewEmitter("pkg", handler, events))
        waitUntil { handler.views.get() == 2 }
        delay(1_200)

        assertEquals(2, renders.get())
        assertEquals(2, handler.views.get())
        events.cancel()
    }

    @Test
    fun cancelledViewFlowReportsError() = runBlocking<Unit> {
        val impl = object : DataTypeImpl("test", "upstream-cancel") {
            override fun viewEvents(context: Context, config: ViewConfig): Flow<ViewEvent> = flow {
                throw CancellationException("upstream timed out")
            }

            override fun viewFlow(context: Context, config: ViewConfig): Flow<ViewFrame> = flow {
                awaitCancellation()
            }
        }
        val events = RecordingEvents()

        impl.startView(context, config, ViewEmitter("pkg", RecordingHandler(), events))
        waitUntil { events.error != null }

        assertTrue(events.error is IllegalStateException)
    }

    @Test
    fun failedRenderSkipsFrame() = runBlocking<Unit> {
        val impl = object : DataTypeImpl("test", "render-fail") {
            override fun viewFlow(context: Context, config: ViewConfig): Flow<ViewFrame> = flow {
                emit(ViewFrame { throw CancellationException("render timed out") })
                delay(50)
                emit(ViewFrame { RemoteViews("pkg", 0) })
            }
        }
        val handler = RecordingHandler()
        val events = RecordingEvents()

        impl.startView(context, config, ViewEmitter("pkg", handler, events))
        waitUntil { handler.views.get() == 1 }

        assertEquals(null, events.error)
        events.cancel()
    }

    @Test
    fun failedUpdateViewReportsError() = runBlocking<Unit> {
        val impl = object : DataTypeImpl("test", "update-fail") {
            override fun viewFlow(context: Context, config: ViewConfig): Flow<ViewFrame> =
                flowOf(ViewFrame { RemoteViews("pkg", 0) })
        }
        val events = RecordingEvents()

        impl.startView(context, config, ViewEmitter("pkg", RecordingHandler(failOnNext = true), events))
        waitUntil { events.error != null }

        assertTrue(events.error is RuntimeException)
    }

    @Test
    fun noUpdateAfterCancel() = runBlocking<Unit> {
        val impl = object : DataTypeImpl("test", "cancel") {
            override fun viewFlow(context: Context, config: ViewConfig): Flow<ViewFrame> = flow {
                repeat(Int.MAX_VALUE) {
                    emit(ViewFrame { RemoteViews("pkg", 0) })
                    delay(100)
                }
            }
        }
        val handler = RecordingHandler()
        val events = RecordingEvents()

        impl.startView(context, config, ViewEmitter("pkg", handler, events))
        waitUntil { handler.views.get() == 1 }
        events.cancel()
        delay(1_500)

        assertEquals(1, handler.views.get())
        assertEquals(null, events.error)
    }
}
