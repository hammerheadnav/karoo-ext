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

import io.hammerhead.karooext.internal.Emitter
import io.hammerhead.karooext.models.StreamState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Collections

class DataTypeImplFlowTest {
    private class RecordingEmitter : Emitter<StreamState> {
        val emitted: MutableList<StreamState> = Collections.synchronizedList(mutableListOf())

        @Volatile
        var error: Throwable? = null

        @Volatile
        var completed = false

        var recordedCancellable: (() -> Unit)? = null

        override fun onNext(t: StreamState) {
            emitted.add(t)
        }

        override fun onError(t: Throwable) {
            error = t
        }

        override fun onComplete() {
            completed = true
        }

        override fun setCancellable(cancellable: () -> Unit) {
            this.recordedCancellable = cancellable
        }

        override fun cancel() {
            recordedCancellable?.invoke()
        }
    }

    @Test
    fun streamFlowForwardsValuesAndCancels() = runBlocking<Unit> {
        val closed = CompletableDeferred<Unit>()
        val impl = object : DataTypeImpl("test", "flow") {
            override fun streamFlow(): Flow<StreamState> = callbackFlow {
                trySend(StreamState.Searching)
                trySend(StreamState.Idle)
                trySend(StreamState.NotAvailable)
                awaitClose { closed.complete(Unit) }
            }
        }
        val emitter = RecordingEmitter()

        impl.startStream(emitter)
        withTimeout(2_000) {
            while (emitter.emitted.size < 3) {
                delay(10)
            }
        }

        assertEquals(
            listOf(StreamState.Searching, StreamState.Idle, StreamState.NotAvailable),
            emitter.emitted.toList(),
        )

        val cancellable = emitter.recordedCancellable
        assertNotNull(cancellable)
        cancellable!!.invoke()
        withTimeout(2_000) { closed.await() }
        delay(50)
        assertNull(emitter.error)
        assertFalse(emitter.completed)
    }

    @Test
    fun foreignCancellationIsReported() = runBlocking<Unit> {
        val impl = object : DataTypeImpl("test", "foreign-cancel") {
            override fun streamFlow(): Flow<StreamState> = flow {
                emit(StreamState.Searching)
                throw CancellationException("upstream timed out")
            }
        }
        val emitter = RecordingEmitter()

        impl.startStream(emitter)
        withTimeout(2_000) {
            while (emitter.error == null) {
                delay(10)
            }
        }
        assertTrue(emitter.error is CancellationException)
    }

    @Test
    fun throwingEmitterDoesNotCrash() = runBlocking<Unit> {
        val impl = object : DataTypeImpl("test", "throwing") {
            override fun streamFlow(): Flow<StreamState> = flow {
                emit(StreamState.Searching)
                emit(StreamState.Idle)
            }
        }
        val errors = Collections.synchronizedList(mutableListOf<Throwable>())
        val emitter = object : Emitter<StreamState> {
            override fun onNext(t: StreamState) {
                throw RuntimeException("boom")
            }

            override fun onError(t: Throwable) {
                errors.add(t)
            }

            override fun onComplete() {
            }

            override fun setCancellable(cancellable: () -> Unit) {
            }

            override fun cancel() {
            }
        }

        impl.startStream(emitter)
        withTimeout(2_000) {
            while (errors.isEmpty()) {
                delay(10)
            }
        }
        assertTrue(errors.isNotEmpty())
    }

    @Test
    fun normalCompletionCallsOnComplete() = runBlocking<Unit> {
        val impl = object : DataTypeImpl("test", "complete") {
            override fun streamFlow(): Flow<StreamState> = flowOf(StreamState.Idle)
        }
        val emitter = RecordingEmitter()

        impl.startStream(emitter)
        withTimeout(2_000) {
            while (!emitter.completed) {
                delay(10)
            }
        }
        assertTrue(emitter.completed)
    }
}
