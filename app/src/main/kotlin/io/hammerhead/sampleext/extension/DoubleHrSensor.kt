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

import android.annotation.SuppressLint
import io.hammerhead.karooext.internal.Emitter
import io.hammerhead.karooext.models.BatteryStatus
import io.hammerhead.karooext.models.ConnectionStatus
import io.hammerhead.karooext.models.DataPoint
import io.hammerhead.karooext.models.DataType
import io.hammerhead.karooext.models.Device
import io.hammerhead.karooext.models.DeviceEvent
import io.hammerhead.karooext.models.ManufacturerInfo
import io.hammerhead.karooext.models.OnBatteryStatus
import io.hammerhead.karooext.models.OnConnectionStatus
import io.hammerhead.karooext.models.OnDataPoint
import io.hammerhead.karooext.models.OnManufacturerInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Sample of a real device backed by BLE connection for [DoubleHrDataType] which isn't actually
 * providing standard [DataType.Source.HEART_RATE] but rather it's own unique type to this extensions.
 * BLE HR sensor is used for ease of testing the sample because these sensors are more available than
 * something like glucose or blood pressure.
 */
class DoubleHrSensor(
    private val bleManager: BleManager,
    extension: String,
    private val address: String,
    private val name: String?,
) : SampleDevice {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override val source by lazy {
        Device(
            extension,
            "$PREFIX-$address",
            listOf(DataType.dataTypeId(extension, DoubleHrDataType.TYPE_ID)),
            "2hr ${name ?: "??"}",
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class, ExperimentalUuidApi::class)
    @SuppressLint("MissingPermission")
    override fun connect(emitter: Emitter<DeviceEvent>) {
        val job = scope.launch {
            bleManager.connect(address)
                .flatMapLatest { peripheral ->
                    peripheral?.let {
                        val connected = flowOf(OnConnectionStatus(ConnectionStatus.CONNECTED))
                        val hrChanges = bleManager.observeCharacteristic(peripheral, HRS_SERVICE_UUID, HRS_MEASUREMENT_CHARACTERISTIC_UUID) { data ->
                            val flag = data[0]
                            if (flag.toInt() and 0x01 != 0) {
                                // 16-bit HR
                                ((data[2].toInt() and 0xFF) shl 8) or (data[1].toInt() and 0xFF)
                            } else {
                                // 8-bit HR
                                data[1].toInt() and 0xFF
                            }
                        }.map { hr ->
                            OnDataPoint(
                                DataPoint(
                                    source.dataTypes.first(),
                                    values = mapOf(DataType.Field.SINGLE to hr * 2.0),
                                    sourceId = source.uid,
                                ),
                            )
                        }
                        val batteryChanges = bleManager.observeCharacteristic(peripheral, BleManager.BTS_SERVICE_UUID, BleManager.BATTERY_LEVEL_CHARACTERISTIC_UUID) { data ->
                            if (data.isNotEmpty()) data[0].toInt() and 0xFF else -1
                        }.map { percent ->
                            OnBatteryStatus(BatteryStatus.fromPercentage(percent))
                        }
                        val manufacturerInfo = flow {
                            val manufacturer =
                                bleManager.readCharacteristic(peripheral, BleManager.DIS_SERVICE_UUID, BleManager.MANUFACTURER_NAME_CHARACTERISTIC_UUID) { data ->
                                    data.decodeToString()
                                }
                            val serialNumber = bleManager.readCharacteristic(peripheral, BleManager.DIS_SERVICE_UUID, BleManager.SERIAL_NUMBER_CHARACTERISTIC_UUID) { data ->
                                data.decodeToString()
                            }
                            val modelNumber = bleManager.readCharacteristic(peripheral, BleManager.DIS_SERVICE_UUID, BleManager.MODEL_NUMBER_CHARACTERISTIC_UUID) { data ->
                                data.decodeToString()
                            }
                            emit(OnManufacturerInfo(ManufacturerInfo(manufacturer, serialNumber, modelNumber)))
                        }
                        merge(connected, hrChanges, batteryChanges, manufacturerInfo)
                    } ?: run {
                        flowOf(OnConnectionStatus(ConnectionStatus.SEARCHING))
                    }
                }.collect { event ->
                    emitter.onNext(event)
                }
        }
        emitter.setCancellable {
            job.cancel()
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    companion object {
        const val PREFIX = "2hr"

        // UUIDs for the Heart Rate service
        val HRS_SERVICE_UUID = Uuid.parse("0000180D-0000-1000-8000-00805f9b34fb")
        val HRS_MEASUREMENT_CHARACTERISTIC_UUID = Uuid.parse("00002A37-0000-1000-8000-00805f9b34fb")
    }
}
