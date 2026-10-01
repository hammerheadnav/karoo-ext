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

@file:JvmName("KarooSystemFlows")

package io.hammerhead.karooext

import io.hammerhead.karooext.models.KarooEvent
import io.hammerhead.karooext.models.KarooEventParams
import io.hammerhead.karooext.models.OnStreamState
import io.hammerhead.karooext.models.StreamState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber

/**
 * Error consuming a Karoo System event.
 *
 * @since 1.2.0
 */
class KarooEventException(message: String) : Exception(message)

/**
 * Shared implementation of the event flows. [register] adds the consumer and returns its id.
 *
 * Not inline so the implementation stays in the lib instead of being copied into each caller.
 */
@PublishedApi
internal fun <T> consumerFlow(
    register: (
        onError: (String) -> Unit,
        onComplete: () -> Unit,
        onEvent: (T) -> Unit,
    ) -> String,
    unregister: (String) -> Unit,
): Flow<T> {
    return callbackFlow {
        val consumerId = register(
            { close(KarooEventException(it)) },
            { close() },
        ) { event ->
            trySend(event).onFailure {
                if (!isClosedForSend) Timber.w("$TAG: collector too slow, dropped $event")
            }
        }
        awaitClose { unregister(consumerId) }
    }
}

/**
 * Consume [KarooEvent]s of type [T] matching [params] as a [Flow].
 *
 * The consumer is removed when the flow stops. On error the flow closes with a
 * [KarooEventException] and it completes when the system completes the consumer. Up to 64 events
 * are buffered; if the collector falls further behind, newer events are dropped and logged, so
 * apply `buffer` for more room or [conflate] for state-like events.
 *
 * The flow does not complete on system disconnect or [KarooSystemService.disconnect]; cancel the
 * collecting scope to remove the consumer.
 *
 * @since 1.2.0
 */
inline fun <reified T : KarooEvent> KarooSystemService.events(
    params: KarooEventParams,
): Flow<T> = consumerFlow(
    { onError, onComplete, onEvent ->
        addConsumer<T>(params, onError = onError, onComplete = onComplete, onEvent = onEvent)
    },
    ::removeConsumer,
)

/**
 * Consume [KarooEvent]s of type [T] using the default params as a [Flow].
 *
 * The consumer is removed when the flow stops. On error the flow closes with a
 * [KarooEventException] and it completes when the system completes the consumer. Up to 64 events
 * are buffered; if the collector falls further behind, newer events are dropped and logged, so
 * apply `buffer` for more room or [conflate] for state-like events.
 *
 * The flow does not complete on system disconnect or [KarooSystemService.disconnect]; cancel the
 * collecting scope to remove the consumer.
 *
 * Collecting throws [IllegalArgumentException] if [T] has no default params; use the overload
 * that takes [KarooEventParams].
 *
 * @since 1.2.0
 */
inline fun <reified T : KarooEvent> KarooSystemService.events(): Flow<T> = consumerFlow(
    { onError, onComplete, onEvent ->
        addConsumer<T>(onError = onError, onComplete = onComplete, onEvent = onEvent)
    },
    ::removeConsumer,
)

/**
 * Observe [StreamState] for the given [dataTypeId] as a conflated [Flow].
 *
 * @since 1.2.0
 */
fun KarooSystemService.streamData(dataTypeId: String): Flow<StreamState> {
    return events<OnStreamState>(OnStreamState.StartStreaming(dataTypeId)).conflate().map { it.state }
}

/**
 * Suspend until the Karoo System is connected.
 *
 * Only returns after [KarooSystemService.connect] has been called and the bind succeeds.
 *
 * @since 1.2.0
 */
suspend fun KarooSystemService.awaitConnected() {
    connectionState.first { it }
}

private const val TAG = "KarooSystemFlows"
