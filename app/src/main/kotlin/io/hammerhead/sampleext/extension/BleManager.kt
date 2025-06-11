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

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onCompletion
import no.nordicsemi.kotlin.ble.client.android.CentralManager
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.client.android.native
import no.nordicsemi.kotlin.ble.client.distinctByPeripheral
import no.nordicsemi.kotlin.ble.core.ConnectionState
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class, ExperimentalCoroutinesApi::class)
@Singleton
class BleManager @Inject constructor(@ApplicationContext private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val centralManager by lazy {
        CentralManager.Factory.native(context, scope)
    }

    private val connectionOptions by lazy {
        CentralManager.ConnectionOptions.Direct(timeout = 120.seconds, retry = 10, retryDelay = 12.seconds)
    }

    fun scan(services: List<Uuid>): Flow<Pair<String, String?>> {
        return centralManager
            .scan {
                Any {
                    services.map {
                        ServiceUuid(it)
                    }
                }
            }
            .distinctByPeripheral()
            .map { Pair(it.peripheral.address, it.peripheral.name) }
            .catch {
                Timber.w(it, "Error in BLE scan")
            }
    }

    fun connect(address: String): Flow<Peripheral?> {
        return flow {
            // Start by scanning until the device is seen and connectable
            val peripheral = centralManager.scan {
                Address(address)
            }.filter { it.isConnectable }.map { it.peripheral }.first()
            try {
                // Connect to the device (including retries)
                repeat(10) { i ->
                    if (i == 0) {
                        Timber.i("Found $peripheral, connecting...")
                    } else {
                        delay(3.seconds)
                        Timber.i("Reconnecting to $peripheral...")
                    }
                    centralManager.connect(peripheral, connectionOptions)
                    Timber.i("Connected to $peripheral")
                    // Emit the peripheral to the caller so they can setup observers
                    emit(peripheral)
                    // Block while connected
                    peripheral.state
                        .filter { it is ConnectionState.Disconnected }
                        .first()
                    Timber.i("Device disconnected from $peripheral, state is ${peripheral.state.value}")
                    // Emit null to the caller so they know it disconnected before re-entering this loop to try to connect again
                    emit(null)
                }
            } finally {
                Timber.i("Disconnecting from $peripheral")
                peripheral.disconnect()
                Timber.i("Disconnected from $peripheral")
            }
        }
    }

    @OptIn(ExperimentalStdlibApi::class, ExperimentalUuidApi::class)
    fun <T> observeCharacteristic(peripheral: Peripheral, service: Uuid, characteristic: Uuid, parser: (ByteArray) -> T): Flow<T> {
        return peripheral.services(listOf(service))
            .flatMapLatest {
                Timber.d("Services changed: $it")
                it.firstOrNull()?.characteristics?.firstOrNull { it.uuid == characteristic }?.let { characteristic ->
                    characteristic.subscribe()
                        .map { data ->
                            val value = parser(data)
                            Timber.d("Data changed: 0x${data.toHexString()} -> $value")
                            value
                        }
                        .onCompletion {
                            Timber.d("Stopped observing $service:$characteristic")
                        }
                } ?: run {
                    Timber.w("Characteristic $service:$characteristic not found")
                    emptyFlow()
                }
            }
            .catch {
                Timber.w("Characteristic $service:$characteristic error: ${it.message}")
            }
    }

    suspend fun <T> readCharacteristic(peripheral: Peripheral, service: Uuid, characteristic: Uuid, parser: (ByteArray) -> T): T? {
        // Wait for service to be discovered then read the characteristic
        return peripheral.services(listOf(service)).mapNotNull { it.firstOrNull() }.firstOrNull()?.let { service ->
            service.characteristics.firstOrNull { it.uuid == characteristic }?.let { characteristic ->
                parser(characteristic.read())
            }
        }
    }

    companion object {
        // UUIDs for the Device Information service (DIS)
        val DIS_SERVICE_UUID = Uuid.parse("0000180A-0000-1000-8000-00805f9b34fb")
        val MANUFACTURER_NAME_CHARACTERISTIC_UUID = Uuid.parse("00002A29-0000-1000-8000-00805f9b34fb")
        val SERIAL_NUMBER_CHARACTERISTIC_UUID = Uuid.parse("00002a25-0000-1000-8000-00805f9b34fb")
        val MODEL_NUMBER_CHARACTERISTIC_UUID = Uuid.parse("00002a24-0000-1000-8000-00805f9b34fb")

        // UUIDs for the Battery Service (BAS)
        val BTS_SERVICE_UUID = Uuid.parse("0000180F-0000-1000-8000-00805f9b34fb")
        val BATTERY_LEVEL_CHARACTERISTIC_UUID = Uuid.parse("00002A19-0000-1000-8000-00805f9b34fb")
    }
}
