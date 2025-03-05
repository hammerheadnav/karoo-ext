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

@file:OptIn(FlowPreview::class)

package io.hammerhead.sampleext

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.models.ActiveRidePage
import io.hammerhead.karooext.models.ActiveRideProfile
import io.hammerhead.karooext.models.ApplyLauncherBackground
import io.hammerhead.karooext.models.Bikes
import io.hammerhead.karooext.models.DataType
import io.hammerhead.karooext.models.HardwareType
import io.hammerhead.karooext.models.HttpResponseState
import io.hammerhead.karooext.models.KarooEffect
import io.hammerhead.karooext.models.Lap
import io.hammerhead.karooext.models.OnGlobalPOIs
import io.hammerhead.karooext.models.OnHttpResponse
import io.hammerhead.karooext.models.OnMapZoomLevel
import io.hammerhead.karooext.models.OnNavigationState
import io.hammerhead.karooext.models.OnStreamState
import io.hammerhead.karooext.models.PlayBeepPattern
import io.hammerhead.karooext.models.RideProfile
import io.hammerhead.karooext.models.RideState
import io.hammerhead.karooext.models.SavedDevices
import io.hammerhead.karooext.models.StreamState
import io.hammerhead.karooext.models.Symbol
import io.hammerhead.karooext.models.UserProfile
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@Serializable
data class HttpBinResponse(val data: String)

data class MainData(
    val connected: Boolean = false,
    val power: StreamState? = null,
    val rideState: RideState? = null,
    val homeBackgroundSet: Boolean = false,
    val httpStatus: String? = null,
    val navigationState: OnNavigationState.NavigationState? = null,
    val globalPOIs: List<Symbol.POI> = emptyList(),
    val savedDevices: List<SavedDevices.SavedDevice> = emptyList(),
    val bikes: List<Bikes.Bike> = emptyList(),
    val rideProfile: RideProfile? = null,
    val activePage: RideProfile.Page? = null,
)

val json = Json {
    ignoreUnknownKeys = true
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val karooSystem: KarooSystemService,
) : ViewModel() {
    private val mutableState = MutableStateFlow(MainData())
    val state: StateFlow<MainData> = mutableState.asStateFlow()

    init {
        initializeEvents()
    }

    fun toggleHomeBackground() {
        val changed = karooSystem.dispatch(
            ApplyLauncherBackground(
                if (mutableState.value.homeBackgroundSet) {
                    null
                } else {
                    "https://images.unsplash.com/photo-1590146758147-74a80644616a?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w1MDI2Mjh8MHwxfHNlYXJjaHw4fHxwdXp6bGV8ZW58MHwwfHx8MTcyNzg3NjMwMnww&ixlib=rb-4.0.3&q=80&w=1080"
                },
            ),
        )
        if (changed) {
            mutableState.update { it.copy(homeBackgroundSet = !it.homeBackgroundSet) }
        }
    }

    fun makeHttpRequest(payload: String) {
        viewModelScope.launch {
            try {
                callbackFlow {
                    val listenerId = karooSystem.addConsumer(
                        OnHttpResponse.MakeHttpRequest(
                            "POST",
                            "https://httpbin.org/anything",
                            headers = mapOf(
                                "Content-Type" to "text/plain",
                            ),
                            body = payload.toByteArray(),
                            // Don't queue this
                            waitForConnection = false,
                        ),
                    ) { event: OnHttpResponse ->
                        Timber.i("Http response event $event")
                        val message = when (val state = event.state) {
                            is HttpResponseState.Complete -> {
                                val resp = state.body?.decodeToString()?.let {
                                    json.decodeFromString<HttpBinResponse>(it)
                                }
                                "Status ${state.statusCode}: ${state.error ?: resp?.data}"
                            }
                            is HttpResponseState.InProgress -> "In Progress"
                            is HttpResponseState.Queued -> "Queued"
                        }
                        trySend(message)
                    }
                    awaitClose {
                        karooSystem.removeConsumer(listenerId)
                    }
                }
                    .timeout(10.seconds)
                    .collect { message ->
                        mutableState.update { it.copy(httpStatus = message) }
                    }
            } catch (e: TimeoutCancellationException) {
                mutableState.update { it.copy(httpStatus = null) }
            }
        }
    }

    private fun initializeEvents() {
        viewModelScope.launch {
            suspendCancellableCoroutine { cont ->
                karooSystem.connect { connected ->
                    Timber.i("Karoo System connected=$connected")
                    mutableState.update { it.copy(connected = connected) }
                }
                karooSystem.addConsumer(OnStreamState.StartStreaming(DataType.Type.POWER)) { event: OnStreamState ->
                    mutableState.update { it.copy(power = event.state) }
                }
                karooSystem.addConsumer { rideState: RideState ->
                    mutableState.update { it.copy(rideState = rideState) }
                }
                karooSystem.addConsumer { navigationState: OnNavigationState ->
                    mutableState.update { it.copy(navigationState = navigationState.state) }
                }
                karooSystem.addConsumer { event: OnGlobalPOIs ->
                    mutableState.update { it.copy(globalPOIs = event.pois) }
                }
                karooSystem.addConsumer { event: SavedDevices ->
                    mutableState.update { it.copy(savedDevices = event.devices) }
                }
                karooSystem.addConsumer { event: Bikes ->
                    mutableState.update { it.copy(bikes = event.bikes) }
                }
                karooSystem.addConsumer { event: ActiveRideProfile ->
                    mutableState.update { it.copy(rideProfile = event.profile) }
                }
                karooSystem.addConsumer { event: ActiveRidePage ->
                    mutableState.update { it.copy(activePage = event.page) }
                }
                karooSystem.addConsumer { zoom: OnMapZoomLevel ->
                    Timber.i("Map zoom $zoom")
                }
                karooSystem.addConsumer { lap: Lap ->
                    Timber.i("Lap ${lap.number}!")
                }
                karooSystem.addConsumer { user: UserProfile ->
                    Timber.i("User profile loaded as $user")
                }

                cont.invokeOnCancellation {
                    Timber.i("Disconnecting from Karoo System")
                    karooSystem.disconnect()
                }
            }
        }
    }

    fun playBeeps() {
        val tones = when (karooSystem.hardwareType) {
            // K2 beeper is limited in audible frequency and duration, like this one.
            HardwareType.K2 -> {
                listOf(
                    PlayBeepPattern.Tone(5000, 200),
                    PlayBeepPattern.Tone(null, 50),
                    PlayBeepPattern.Tone(5000, 200),
                    PlayBeepPattern.Tone(null, 50),
                    PlayBeepPattern.Tone(5000, 250),
                    PlayBeepPattern.Tone(null, 100),
                    PlayBeepPattern.Tone(4000, 350),
                )
            }
            // Karoo can more accurately play frequencies for longer durations, demonstrated here.
            HardwareType.KAROO -> {
                val tempo = 108
                val wholeNote = (60000 * 4) / tempo
                listOf(
                    PlayBeepPattern.Tone(466, wholeNote / 8),
                    PlayBeepPattern.Tone(466, wholeNote / 8),
                    PlayBeepPattern.Tone(466, wholeNote / 8),
                    PlayBeepPattern.Tone(698, wholeNote / 2),
                    PlayBeepPattern.Tone(1047, wholeNote / 2),
                    PlayBeepPattern.Tone(932, wholeNote / 8),
                    PlayBeepPattern.Tone(880, wholeNote / 8),
                    PlayBeepPattern.Tone(784, wholeNote / 8),
                    PlayBeepPattern.Tone(1397, wholeNote / 2),
                    PlayBeepPattern.Tone(1047, wholeNote / 4),
                    PlayBeepPattern.Tone(932, wholeNote / 8),
                    PlayBeepPattern.Tone(880, wholeNote / 8),
                    PlayBeepPattern.Tone(784, wholeNote / 8),
                    PlayBeepPattern.Tone(1397, wholeNote / 2),
                    PlayBeepPattern.Tone(1047, wholeNote / 4),
                    PlayBeepPattern.Tone(932, wholeNote / 8),
                    PlayBeepPattern.Tone(880, wholeNote / 8),
                    PlayBeepPattern.Tone(932, wholeNote / 8),
                    PlayBeepPattern.Tone(784, wholeNote / 2),
                )
            }
            else -> return
        }
        if (karooSystem.dispatch(PlayBeepPattern(tones))) {
            Timber.d("Karoo System dispatched beeps")
        }
    }

    fun dispatchEffect(effect: KarooEffect) {
        if (karooSystem.dispatch(effect)) {
            Timber.d("Karoo System dispatched effect=$effect")
        }
    }
}
