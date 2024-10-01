/**
 * Copyright (c) 2024 Hammerhead Navigation Inc.
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

package io.hammerhead.sampleext

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.models.DataType
import io.hammerhead.karooext.models.HardwareType
import io.hammerhead.karooext.models.Lap
import io.hammerhead.karooext.models.OnStreamState
import io.hammerhead.karooext.models.PerformHardwareAction
import io.hammerhead.karooext.models.PlayBeepPattern
import io.hammerhead.karooext.models.RideState
import io.hammerhead.karooext.models.StreamState
import io.hammerhead.karooext.models.SystemNotification
import io.hammerhead.karooext.models.UserProfile
import io.hammerhead.sampleext.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import timber.log.Timber

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()

    private lateinit var binding: ActivityMainBinding

    private val karooSystem = KarooSystemService(this)
    private val listenerIds = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    binding.connectionStatus.text = "Connected: ${state.connected}"
                    val powerStr = state.power?.let {
                        (it as? StreamState.Streaming)?.dataPoint?.singleValue?.let {
                            "${it}w"
                        } ?: it.toString()
                    } ?: "Unknown"
                    binding.powerData.text = "Power: $powerStr"
                    binding.rideState.text = "Ride State: ${state.rideState?.toString()}"
                }
            }
        }

        binding.notificationButton.setOnClickListener {
            karooSystem.dispatch(
                SystemNotification(
                    "sample-clicked",
                    "You did it!",
                    "You clicked the notify button in the sample.",
                ),
            )
        }

        binding.beepButton.setOnClickListener {
            playBeeps()
        }

        setupHardwareActionListeners()
    }

    override fun onStart() {
        super.onStart()
        listenerIds.add(
            karooSystem.addConsumer(OnStreamState.StartStreaming(DataType.Type.POWER)) { event: OnStreamState ->
                viewModel.updatePower(event.state)
            },
        )
        listenerIds.add(
            karooSystem.addConsumer { rideState: RideState ->
                viewModel.updateRideState(rideState)
            },
        )
        listenerIds.add(
            karooSystem.addConsumer { lap: Lap ->
                Toast.makeText(this, "Lap ${lap.number}!", Toast.LENGTH_SHORT).show()
            },
        )
        listenerIds.add(
            karooSystem.addConsumer { user: UserProfile ->
                Timber.i("User profile loaded as $user")
            },
        )
        listenerIds.add(
            karooSystem.registerConnectionListener { connected ->
                Timber.i("Karoo System connected=$connected")
                viewModel.updateConnected(connected)
            },
        )
    }

    override fun onStop() {
        listenerIds.forEach {
            karooSystem.removeConsumer(it)
        }
        listenerIds.clear()
        super.onStop()
    }

    private fun playBeeps() {
        val tones = when (karooSystem.hardwareType) {
            // K2 beeper is limited in audible frequency and duration
            // so demonstrate a pattern that is similar to the system patterns.
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
            // Karoo can more accurately play frequencies for longer durations,
            // demonstrate that here.
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
        val dispatched = karooSystem.dispatch(PlayBeepPattern(tones))
        Timber.d("Karoo System dispatched beeps=$dispatched")
    }

    private fun setupHardwareActionListeners() {
        binding.topLeftButton.setOnClickListener { karooSystem.dispatch(PerformHardwareAction.TopLeftPress) }
        binding.topRightButton.setOnClickListener { karooSystem.dispatch(PerformHardwareAction.TopRightPress) }
        binding.bottomLeftButton.setOnClickListener { karooSystem.dispatch(PerformHardwareAction.BottomLeftPress) }
        binding.bottomRightButton.setOnClickListener { karooSystem.dispatch(PerformHardwareAction.BottomRightPress) }
        binding.ccButton.setOnClickListener { karooSystem.dispatch(PerformHardwareAction.ControlCenterComboPress) }
    }
}
