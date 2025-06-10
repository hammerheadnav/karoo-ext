package io.hammerhead.sampleext.extension

import android.annotation.SuppressLint
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.replay
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.suspendCancellableCoroutine
import no.nordicsemi.kotlin.ble.client.android.CentralManager
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.client.android.native
import no.nordicsemi.kotlin.ble.client.distinctByPeripheral
import no.nordicsemi.kotlin.ble.core.ConnectionState
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class, ExperimentalCoroutinesApi::class)
@SuppressLint("StaticFieldLeak")
@Singleton
class BleManager @Inject constructor(@ApplicationContext private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {

    }

    private val centralManager by lazy {
        CentralManager.Factory.native(context, scope)
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
    }

    fun connect(address: String): Flow<Peripheral?> {
        return centralManager.scan {
            Address(address)
        }
            .filter { it.isConnectable }
            .map { it.peripheral }
            .take(1)
            .flatMapLatest { peripheral ->
                peripheral.state
                    .onStart {
                        Timber.i("Connecting to $peripheral")
                        centralManager.connect(peripheral)
                        Timber.i("Connected to $peripheral")
                    }
                    .map {
                        Timber.i("State of $peripheral: $it")
                        when (it) {
                            is ConnectionState.Connected -> peripheral
                            else -> null
                        }
                    }
                    .onCompletion {
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
                Timber.i("Services changed: $it")
                it.firstOrNull()?.characteristics?.firstOrNull { it.uuid == characteristic }?.let { characteristic ->
                    characteristic.subscribe()
                        .map { data ->
                            val value = parser(data)
                            Timber.i("Data changed: 0x${data.toHexString()} -> $value")
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