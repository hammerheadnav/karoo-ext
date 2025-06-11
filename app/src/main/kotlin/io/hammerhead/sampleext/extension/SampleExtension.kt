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

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.mapbox.geojson.Point
import com.mapbox.geojson.utils.PolylineUtils
import com.mapbox.turf.TurfConstants
import com.mapbox.turf.TurfMeasurement
import dagger.hilt.android.AndroidEntryPoint
import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.extension.KarooExtension
import io.hammerhead.karooext.internal.Emitter
import io.hammerhead.karooext.models.DataType
import io.hammerhead.karooext.models.DeveloperField
import io.hammerhead.karooext.models.Device
import io.hammerhead.karooext.models.DeviceEvent
import io.hammerhead.karooext.models.FieldValue
import io.hammerhead.karooext.models.FitEffect
import io.hammerhead.karooext.models.InRideAlert
import io.hammerhead.karooext.models.KarooEffect
import io.hammerhead.karooext.models.MapEffect
import io.hammerhead.karooext.models.MarkLap
import io.hammerhead.karooext.models.OnLocationChanged
import io.hammerhead.karooext.models.OnMapZoomLevel
import io.hammerhead.karooext.models.ReleaseBluetooth
import io.hammerhead.karooext.models.RequestBluetooth
import io.hammerhead.karooext.models.RideState
import io.hammerhead.karooext.models.ShowPolyline
import io.hammerhead.karooext.models.ShowSymbols
import io.hammerhead.karooext.models.StreamState
import io.hammerhead.karooext.models.Symbol
import io.hammerhead.karooext.models.SystemNotification
import io.hammerhead.karooext.models.UserProfile
import io.hammerhead.karooext.models.WriteEventMesg
import io.hammerhead.karooext.models.WriteToRecordMesg
import io.hammerhead.karooext.models.WriteToSessionMesg
import io.hammerhead.sampleext.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.reflect.full.createInstance
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
@AndroidEntryPoint
class SampleExtension : KarooExtension("sample", "1.0") {
    @Inject
    lateinit var karooSystem: KarooSystemService

    @Inject
    lateinit var bleManager: BleManager

    private var serviceJob: Job? = null
    private val devices = ConcurrentHashMap<String, SampleDevice>()

    override val types by lazy {
        listOf(
            PowerHrDataType(karooSystem, extension),
            CustomSpeedDataType(karooSystem, extension),
            BespokeDataType(extension),
            DoubleHrDataType(extension),
        )
    }

    override fun startScan(emitter: Emitter<Device>) {
        // Find a new sources every 5 seconds
        val job = CoroutineScope(Dispatchers.IO).launch {
            val staticSources = flow {
                delay(1000)
                repeat(Int.MAX_VALUE) {
                    val hr = StaticHrSource(extension, 100 + it * 10)
                    emit(hr)
                    delay(1000)
                    val shift = IncrementalShiftingSource(extension, it)
                    emit(shift)
                    delay(1000)
                    val bespoke = BespokeDataSource(extension, it)
                    emit(bespoke)
                    delay(1000)
                }
            }
            val bleSources = bleManager.scan(listOf(DoubleHrSensor.HRS_SERVICE_UUID)).map { (address, name) ->
                Timber.i("BLE Scanned found $address: $name")
                DoubleHrSensor(bleManager, extension, address, name)
            }
            merge(staticSources, bleSources).collect { device ->
                devices.putIfAbsent(device.source.uid, device)
                emitter.onNext(device.source)
            }
        }
        emitter.setCancellable {
            job.cancel()
        }
    }

    override fun connectDevice(uid: String, emitter: Emitter<DeviceEvent>) {
        Timber.d("Connect to $uid")
        devices.getOrPut(uid) {
            val id = uid.substringAfterLast("-").toIntOrNull()
            if (uid.contains(IncrementalShiftingSource.PREFIX) && id != null) {
                IncrementalShiftingSource(extension, id)
            } else if (uid.contains(StaticHrSource.PREFIX) && id != null) {
                StaticHrSource(extension, id)
            } else if (uid.contains(BespokeDataSource.PREFIX) && id != null) {
                BespokeDataSource(extension, id)
            } else if (uid.contains(DoubleHrSensor.PREFIX)) {
                DoubleHrSensor(bleManager, extension, uid.substringAfterLast("-"), null)
            } else {
                throw IllegalArgumentException("unknown type for $uid")
            }
        }.connect(emitter)
    }

    override fun startMap(emitter: Emitter<MapEffect>) {
        val job = CoroutineScope(Dispatchers.IO).launch {
            combine(karooSystem.consumerFlow<OnLocationChanged>(), karooSystem.consumerFlow<OnMapZoomLevel>()) { location, mapZoom ->
                Pair(location, mapZoom)
            }
                .collect { (location, mapZoom) ->
                    val source = Point.fromLngLat(location.lng, location.lat)
                    val totalDistance = when {
                        mapZoom.zoomLevel >= 15.0 -> 100.0
                        mapZoom.zoomLevel >= 12.0 -> 200.0
                        else -> 300.0
                    }
                    val dest = TurfMeasurement.destination(source, totalDistance, 45.0, TurfConstants.UNIT_METERS)
                    val half = TurfMeasurement.destination(source, totalDistance / 2, 45.0, TurfConstants.UNIT_METERS)
                    emitter.onNext(
                        ShowSymbols(
                            listOf(
                                Symbol.POI(
                                    id = "away",
                                    lat = dest.latitude(),
                                    lng = dest.longitude(),
                                ),
                                Symbol.Icon(
                                    id = "half",
                                    lat = half.latitude(),
                                    lng = half.longitude(),
                                    orientation = 0f,
                                    iconRes = R.drawable.ic_arrow,
                                ),
                            ),
                        ),
                    )
                    val polyline = PolylineUtils.encode(listOf(source, dest), 5)
                    emitter.onNext(ShowPolyline("45", polyline, getColor(R.color.colorPrimary), 4))
                }
        }
        emitter.setCancellable {
            job.cancel()
        }
    }

    private val doughnutsField by lazy {
        DeveloperField(
            fieldDefinitionNumber = 0,
            fitBaseTypeId = 136, // FitBaseType.Float32
            fieldName = "Doughnuts Earned",
            units = "doughnuts",
        )
    }

    override fun startFit(emitter: Emitter<FitEffect>) {
        val job = CoroutineScope(Dispatchers.IO).launch {
            karooSystem.streamDataFlow(DataType.Type.ELAPSED_TIME)
                .mapNotNull { (it as? StreamState.Streaming)?.dataPoint?.singleValue?.div(1000) }
                .combine(karooSystem.consumerFlow<RideState>()) { seconds, rideState ->
                    Pair(seconds, rideState)
                }
                .collect { (seconds, rideState) ->
                    // One to start and another one earned every 20 minutes (rounded to 0.1)
                    val doughnuts = 1 + (seconds / 120.0).roundToInt() / 10.0
                    val doughnutsField = FieldValue(doughnutsField, doughnuts)
                    when (rideState) {
                        is RideState.Idle -> {}
                        // When paused, write to SessionMesg so it's committed infrequently
                        // Last set will be saved at end of activity
                        is RideState.Paused -> {
                            Timber.d("Doughnuts session now $doughnuts")
                            emitter.onNext(WriteToSessionMesg(doughnutsField))
                        }
                        // When recording, write doughnuts and power to record messages
                        is RideState.Recording -> {
                            Timber.d("Doughnuts now $doughnuts")
                            emitter.onNext(WriteToRecordMesg(doughnutsField))

                            // Power: saw-tooth [100, 200]
                            val fakePower = 100 + seconds.mod(200.0).minus(100).absoluteValue
                            Timber.d("Power now $fakePower")
                            emitter.onNext(
                                WriteToRecordMesg(
                                    /**
                                     * From FIT SDK:
                                     * public static final int PowerFieldNum = 7;
                                     */
                                    FieldValue(7, fakePower),
                                ),
                            )
                        }
                    }
                    if (seconds == 42.0) {
                        // Off-course marker at 42 seconds with doughnuts included
                        emitter.onNext(
                            WriteEventMesg(
                                event = 7, // OFF_COURSE((short)7),
                                eventType = 3, // MARKER((short)3),
                                values = listOf(doughnutsField),
                            ),
                        )
                    }
                }
        }
        emitter.setCancellable {
            job.cancel()
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate() {
        super.onCreate()
        serviceJob = CoroutineScope(Dispatchers.IO).launch {
            karooSystem.connect { connected ->
                if (connected) {
                    karooSystem.dispatch(RequestBluetooth(extension))
                    val message = if (ActivityCompat.checkSelfPermission(this@SampleExtension, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                        "Sample extension started (permissions granted)"
                    } else {
                        "Sample extension started (needs permissions)"
                    }
                    karooSystem.dispatch(
                        SystemNotification(
                            "sample-started",
                            message,
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
        karooSystem.dispatch(ReleaseBluetooth(extension))
        karooSystem.disconnect()
        super.onDestroy()
    }
}
