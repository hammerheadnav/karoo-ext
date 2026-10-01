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

package io.hammerhead.karooext

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class KarooSystemFlowsTest {
    private class FakeConsumer {
        var onError: ((String) -> Unit)? = null
        var onComplete: (() -> Unit)? = null
        val unregistered = CompletableDeferred<String>()

        fun flow(): Flow<Int> = consumerFlow(
            { error, complete, event ->
                onError = error
                onComplete = complete
                event(1)
                event(2)
                "consumer-id"
            },
            { unregistered.complete(it) },
        )
    }

    @Test
    fun forwardsEventsAndUnregistersOnCancel() = runBlocking<Unit> {
        val consumer = FakeConsumer()

        val values = withTimeout(2_000) { consumer.flow().take(2).toList() }

        assertEquals(listOf(1, 2), values)
        assertEquals("consumer-id", withTimeout(2_000) { consumer.unregistered.await() })
    }

    @Test
    fun errorClosesWithKarooEventException() = runBlocking<Unit> {
        val consumer = FakeConsumer()

        try {
            withTimeout(2_000) {
                consumer.flow().collect { if (it == 2) consumer.onError!!("boom") }
            }
            fail("expected KarooEventException")
        } catch (e: KarooEventException) {
            assertEquals("boom", e.message)
        }
        assertTrue(consumer.unregistered.isCompleted)
    }

    @Test
    fun completeEndsFlow() = runBlocking<Unit> {
        val consumer = FakeConsumer()
        val values = mutableListOf<Int>()

        withTimeout(2_000) {
            consumer.flow().collect {
                values.add(it)
                if (it == 2) consumer.onComplete!!()
            }
        }

        assertEquals(listOf(1, 2), values)
        assertTrue(consumer.unregistered.isCompleted)
    }
}
