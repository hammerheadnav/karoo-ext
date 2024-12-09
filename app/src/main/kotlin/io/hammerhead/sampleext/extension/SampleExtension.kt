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

package io.hammerhead.sampleext.extension

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import dagger.hilt.android.AndroidEntryPoint
import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.extension.KarooExtension
import io.hammerhead.karooext.internal.Emitter
import io.hammerhead.karooext.models.DataType
import io.hammerhead.karooext.models.Device
import io.hammerhead.karooext.models.DeviceEvent
import io.hammerhead.karooext.models.InRideAlert
import io.hammerhead.karooext.models.KarooEffect
import io.hammerhead.karooext.models.MarkLap
import io.hammerhead.karooext.models.StreamState
import io.hammerhead.karooext.models.SystemNotification
import io.hammerhead.karooext.models.UserProfile
import io.hammerhead.sampleext.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlin.reflect.full.createInstance

@AndroidEntryPoint
class SampleExtension : KarooExtension("sample", "1.0") {
    @Inject
    lateinit var karooSystem: KarooSystemService

    private var serviceJob: Job? = null
    private val devices = ConcurrentHashMap<String, SampleDevice>()

    override val types by lazy {
        listOf(
            PowerHrDataType(karooSystem, extension),
            CustomSpeedDataType(karooSystem, extension),
            BespokeDataType(extension),
        )
    }

    override fun startScan(emitter: Emitter<Device>) {
        // Find a new sources every 5 seconds
        val job = CoroutineScope(Dispatchers.IO).launch {
            delay(1000)
            repeat(Int.MAX_VALUE) {
                val hr = StaticHrSource(extension, 100 + it * 10)
                devices.putIfAbsent(hr.source.uid, hr)
                emitter.onNext(hr.source)
                delay(1000)
                val shift = IncrementalShiftingSource(extension, it)
                devices.putIfAbsent(shift.source.uid, shift)
                emitter.onNext(shift.source)
                delay(1000)
                val bespoke = BespokeDataSource(extension, it)
                devices.putIfAbsent(bespoke.source.uid, bespoke)
                emitter.onNext(bespoke.source)
                delay(1000)
            }
        }
        emitter.setCancellable {
            job.cancel()
        }
    }

    override fun connectDevice(uid: String, emitter: Emitter<DeviceEvent>) {
        val id = uid.substringAfterLast("-").toIntOrNull() ?: return
        Timber.d("Connect to $id")
        devices.getOrPut(uid) {
            if (uid.contains(IncrementalShiftingSource.PREFIX)) {
                IncrementalShiftingSource(extension, id)
            } else if (uid.contains(StaticHrSource.PREFIX)) {
                StaticHrSource(extension, id)
            } else if (uid.contains(BespokeDataSource.PREFIX)) {
                BespokeDataSource(extension, id)
            } else {
                throw IllegalArgumentException("unknown type for $uid")
            }
        }.connect(emitter)
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate() {
        super.onCreate()
        serviceJob = CoroutineScope(Dispatchers.IO).launch {
            karooSystem.connect { connected ->
                if (connected) {
                    karooSystem.dispatch(
                        SystemNotification(
                            "sample-started",
                            "Sample extension started",
                            action = "See it",
                            actionIntent = "io.hammerhead.sampleext.MAIN",
                        ),
                    )
                }
            }
            launch {
                // Mark a lap and show an in-ride alert every mile/km
                val userProfile = karooSystem.consumerFlow<UserProfile>().first()
                karooSystem.streamDataFlow(DataType.Type.DISTANCE)
                    .mapNotNull { (it as? StreamState.Streaming)?.dataPoint?.singleValue }
                    // meters to user's preferred unit system (mi or km)
                    .map {
                        when (userProfile.preferredUnit.distance) {
                            UserProfile.PreferredUnit.UnitType.METRIC -> it / 1000
                            UserProfile.PreferredUnit.UnitType.IMPERIAL -> it / 1609.345
                        }.toInt()
                    }
                    // each unique kilometer
                    .distinctUntilChanged()
                    // only emit on change (exclude initial value)
                    .drop(1)
                    .collect {
                        karooSystem.dispatch(
                            InRideAlert(
                                id = "distance-marker",
                                icon = R.drawable.ic_sample,
                                title = getString(R.string.alert_title),
                                detail = getString(R.string.alert_detail, it),
                                autoDismissMs = 10_000,
                                backgroundColor = R.color.green,
                                textColor = R.color.light_green,
                            ),
                        )
                        karooSystem.dispatch(MarkLap)
                    }
            }
            launch {
                // Handle actions that can't be shown in MainActivity because
                // they are for in-ride scenarios. Receiving these intents is like
                // if an extension got a command from a sensor or API that maps to the in-ride actions.
                //
                // Test with: adb shell am broadcast -a io.hammerhead.sample.IN_RIDE_ACTION --es action io.hammerhead.karooext.models.MarkLap
                // Works with any KarooEffect that has no required parameters:
                //  - MarkLap, PauseRide, ResumeRide, ShowMapPage, ZoomPage, TurnScreenOff, TurnScreenOn, and PerformHardwareActions
                callbackFlow {
                    val intentFilter = IntentFilter("io.hammerhead.sample.IN_RIDE_ACTION")
                    val receiver = object : BroadcastReceiver() {
                        override fun onReceive(context: Context, intent: Intent) {
                            trySend(intent)
                        }
                    }
                    registerReceiver(receiver, intentFilter)
                    awaitClose { unregisterReceiver(receiver) }
                }
                    .mapNotNull {
                        it.extras?.getString("action")?.let { action ->
                            try {
                                val clazz = Class.forName(action).kotlin
                                (clazz.objectInstance ?: clazz.createInstance()) as? KarooEffect
                            } catch (e: Exception) {
                                Timber.w(e, "Unknown action $action")
                                null
                            }
                        }
                    }
                    .collect { effect ->
                        karooSystem.dispatch(effect)
                    }
            }
        }
    }

    override fun onDestroy() {
        serviceJob?.cancel()
        serviceJob = null
        karooSystem.disconnect()
        super.onDestroy()
    }
}
