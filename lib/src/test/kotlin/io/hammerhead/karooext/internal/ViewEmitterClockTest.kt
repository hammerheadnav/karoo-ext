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

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ViewEmitterClockTest {
    private var now = 10_000L

    @Test
    fun firstCallPasses() {
        val gate = UpdateGate { now }
        assertTrue(gate.tryAcquire())
    }

    @Test
    fun callBeforeWindowRejected() {
        val gate = UpdateGate { now }
        assertTrue(gate.tryAcquire())
        now += 899
        assertFalse(gate.tryAcquire())
    }

    @Test
    fun callAtWindowPasses() {
        val gate = UpdateGate { now }
        assertTrue(gate.tryAcquire())
        now += 900
        assertTrue(gate.tryAcquire())
    }

    @Test
    fun decisionFollowsInjectedClockNotWallClock() {
        // Wall-clock sized values behave the same as small monotonic ones; advancing
        // the injected clock is what lets a later call through.
        var wall = System.currentTimeMillis()
        val gate = UpdateGate { wall }
        assertTrue(gate.tryAcquire())
        wall += 899
        assertFalse(gate.tryAcquire())
        wall += 1
        assertTrue(gate.tryAcquire())
    }
}
