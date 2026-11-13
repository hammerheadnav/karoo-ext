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

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.extension.DataTypeImpl
import io.hammerhead.karooext.internal.Emitter
import io.hammerhead.karooext.internal.ViewEmitter
import io.hammerhead.karooext.models.DataPoint
import io.hammerhead.karooext.models.DataType
import io.hammerhead.karooext.models.HttpResponseState
import io.hammerhead.karooext.models.InRideAlert
import io.hammerhead.karooext.models.OnHttpResponse
import io.hammerhead.karooext.models.StreamState
import io.hammerhead.karooext.models.ViewConfig
import io.hammerhead.sampleext.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

const val ACTION_HA_BUTTON_CLICKED = "io.hammerhead.sampleext.HA_BUTTON_CLICKED"
const val EXTRA_BUTTON_ID = "button_id"

class HaButtonDataType(
    extension: String,
    typeId: String,
    val button: HaButton,
    val karooSystem: KarooSystemService,
    val context: Context,
) : DataTypeImpl(extension, typeId), HaClickable {
    private var isConfirmationPending = false
    private var confirmationTimeoutJob: Job? = null
    private val _countdownSeconds = MutableStateFlow<Int?>(null)

    override fun startStream(emitter: Emitter<StreamState>) {
        val job = CoroutineScope(Dispatchers.IO).launch {
            _countdownSeconds.collect { seconds ->
                if (seconds != null) {
                    emitter.onNext(
                        StreamState.Streaming(
                            DataPoint(
                                dataTypeId = dataTypeId,
                                values = mapOf(DataType.Field.SINGLE to seconds.toDouble()),
                            ),
                        ),
                    )
                } else {
                    emitter.onNext(StreamState.Idle)
                }
            }
        }
        emitter.setCancellable { job.cancel() }
    }

    override fun startView(context: Context, config: ViewConfig, emitter: ViewEmitter) {
        // Register this data type with the receiver so it can handle clicks
        HaButtonReceiver.registerDataType(
            button.actionId,
            this,
            ViewEmitterProvider { emitter },
        )

        // Create RemoteViews with button that sends intent on click
        val views = createButtonViews(context, false)
        emitter.updateView(views)
    }

    private fun createButtonViews(context: Context, isConfirmation: Boolean): RemoteViews {
        val buttonText = if (isConfirmation) {
            "TAP AGAIN (5s)"
        } else {
            button.displayName
        }

        val color = if (isConfirmation) {
            0xFFFFEB3B.toInt() // Yellow for initial confirmation
        } else {
            0xFF2196F3.toInt() // Blue for normal state
        }

        return RemoteViews(context.packageName, R.layout.ha_button_view).apply {
            setTextViewText(R.id.button_text, buttonText)
            setTextColor(R.id.button_text, if (isConfirmation) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
            setInt(R.id.button_text, "setBackgroundColor", color)

            // Set up click intent for the button. Unique data per button + state keeps the
            // intents filterEquals-distinct so their PendingIntents can't collapse onto one
            // (PendingIntent matching ignores extras).
            val intent = Intent(ACTION_HA_BUTTON_CLICKED).apply {
                setClass(context, HaButtonReceiver::class.java)
                data = Uri.parse("ha-button://${button.actionId}/${if (isConfirmation) "confirm" else "press"}")
                putExtra(EXTRA_BUTTON_ID, button.actionId)
                putExtra("confirmation", isConfirmation)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                button.actionId.hashCode() + if (isConfirmation) 1000 else 0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            setOnClickPendingIntent(R.id.button_text, pendingIntent)
        }
    }

    override fun handleClick(isConfirmation: Boolean, emitter: ViewEmitter) {
        if (isConfirmation) {
            // Second click - execute the action
            confirmationTimeoutJob?.cancel()
            isConfirmationPending = false
            _countdownSeconds.value = null
            callHaButton(button, karooSystem, context)
            // Reset button to normal state
            val views = createButtonViews(context, false)
            emitter.updateView(views)
        } else {
            // First click - show confirmation
            if (!isConfirmationPending) {
                isConfirmationPending = true

                // Reset to normal state after 5 seconds if not confirmed
                confirmationTimeoutJob = GlobalScope.launch {
                    for (remainingSeconds in 5 downTo 1) {
                        _countdownSeconds.value = remainingSeconds
                        val views = createCountdownButtonViews(context, remainingSeconds)
                        emitter.updateView(views)
                        delay(1000)
                    }
                    _countdownSeconds.value = null
                    isConfirmationPending = false
                    val resetViews = createButtonViews(context, false)
                    emitter.updateView(resetViews)
                }
            }
        }
    }

    private fun createCountdownButtonViews(context: Context, remainingSeconds: Int): RemoteViews {
        // Color gradient from yellow (5s) through orange to red (1s)
        val color = when (remainingSeconds) {
            5 -> 0xFFFFEB3B.toInt() // Yellow
            4 -> 0xFFFFC107.toInt() // Amber
            3 -> 0xFFFF9800.toInt() // Orange
            2 -> 0xFFFF5722.toInt() // Deep Orange
            1 -> 0xFFE53935.toInt() // Red
            else -> 0xFFFFEB3B.toInt()
        }

        return RemoteViews(context.packageName, R.layout.ha_button_view).apply {
            setTextViewText(R.id.button_text, "TAP AGAIN (${remainingSeconds}s)")
            setTextColor(R.id.button_text, 0xFF000000.toInt()) // Black text
            setInt(R.id.button_text, "setBackgroundColor", color)

            // Set up click intent for confirmation. Unique data keeps it filterEquals-distinct
            // from other buttons' intents (PendingIntent matching ignores extras).
            val intent = Intent(ACTION_HA_BUTTON_CLICKED).apply {
                setClass(context, HaButtonReceiver::class.java)
                data = Uri.parse("ha-button://${button.actionId}/confirm")
                putExtra(EXTRA_BUTTON_ID, button.actionId)
                putExtra("confirmation", true)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                button.actionId.hashCode() + 1000,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            setOnClickPendingIntent(R.id.button_text, pendingIntent)
        }
    }

    fun callHa() {
        callHaButton(button, karooSystem, context)
    }
}

fun callHaButton(button: HaButton, karooSystem: KarooSystemService, context: Context) {
    val config = AppConfig.load(context)
    val url = config.apiUrl("services/${button.domain}/${button.service}")
    val headers = config.requestHeaders()
    val bodyJson = if (button.entityId != null) {
        """{"entity_id":"${button.entityId}"}"""
    } else {
        "{}"
    }

    // Show immediate feedback so the user knows the tap was registered
    karooSystem.dispatch(
        InRideAlert(
            id = "ha-action-${button.actionId}",
            icon = R.drawable.ic_sample,
            title = button.displayName,
            detail = "Sending…",
            autoDismissMs = 10_000,
            backgroundColor = R.color.orange,
            textColor = R.color.white,
        ),
    )

    val listenerRef = object {
        var id: String = ""
    }

    listenerRef.id = karooSystem.addConsumer(
        OnHttpResponse.MakeHttpRequest(
            method = "POST",
            url = url,
            headers = headers,
            body = bodyJson.toByteArray(),
            // Fail fast instead of silently queuing: a button press should give immediate
            // feedback, never fire minutes later when a connection happens to return.
            waitForConnection = false,
        ),
    ) { response: OnHttpResponse ->
        when (val state = response.state) {
            is HttpResponseState.Complete -> {
                Timber.d("HA response for ${button.displayName}: ${state.statusCode}")
                val (detail, bgColor) = if (state.statusCode in 200..299) {
                    "Command sent" to R.color.green
                } else {
                    // Surface HA's response body (e.g. {"message":"401: Unauthorized"}) so the
                    // real reason is visible on the Karoo, not just the bare status code.
                    val bodyText = state.body
                        ?.toString(Charsets.UTF_8)
                        ?.trim()
                        ?.takeIf { it.isNotEmpty() }
                        ?.take(140)
                    val errorMsg = when {
                        state.error != null -> state.error
                        bodyText != null -> "HTTP ${state.statusCode}: $bodyText"
                        else -> "HTTP ${state.statusCode}"
                    }
                    Timber.e("HA error for ${button.displayName}: $errorMsg")
                    "Error: $errorMsg" to R.color.red
                }
                karooSystem.dispatch(
                    InRideAlert(
                        id = "ha-action-${button.actionId}",
                        icon = R.drawable.ic_sample,
                        title = button.displayName,
                        detail = detail,
                        autoDismissMs = 3_000,
                        backgroundColor = bgColor,
                        textColor = R.color.white,
                    ),
                )
                karooSystem.removeConsumer(listenerRef.id)
            }

            else -> {
                Timber.d("HA request for ${button.displayName} in progress...")
            }
        }
    }
}
