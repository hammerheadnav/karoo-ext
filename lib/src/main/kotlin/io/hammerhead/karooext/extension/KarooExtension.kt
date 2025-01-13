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

package io.hammerhead.karooext.extension

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import io.hammerhead.karooext.EXT_LIB_VERSION
import io.hammerhead.karooext.aidl.IHandler
import io.hammerhead.karooext.aidl.IKarooExtension
import io.hammerhead.karooext.internal.Emitter
import io.hammerhead.karooext.internal.ViewEmitter
import io.hammerhead.karooext.internal.serializableFromBundle
import io.hammerhead.karooext.models.DataType
import io.hammerhead.karooext.models.Device
import io.hammerhead.karooext.models.DeviceEvent
import io.hammerhead.karooext.models.ExtensionInfo
import io.hammerhead.karooext.models.FitEffect
import io.hammerhead.karooext.models.MapEffect
import io.hammerhead.karooext.models.StreamState
import io.hammerhead.karooext.models.ViewConfig
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

/**
 * Base class for implementation of Karoo Extension.
 *
 * @sample [karooExtensionUsage]
 */
abstract class KarooExtension(
    /**
     * Extension ID, matching [ExtensionInfo.id] from extension manifest.
     *
     * This is different from your application id (com.something) and cannot contain '.'
     */
    val extension: String,
    /**
     * Extension version (separate from [EXT_LIB_VERSION]).
     */
    val version: String,
) : Service() {
    private val emitters = ConcurrentHashMap<String, Emitter<*>>()

    init {
        check(extension.none { it == '.' }) { "extension ID cannot contain '.'" }
    }

    /**
     * @suppress
     */
    final override fun onBind(intent: Intent): IBinder {
        Timber.i("$TAG: extension $extension [$version] started by Karoo System")
        return binder
    }

    private val binder by lazy {
        object : IKarooExtension.Stub() {
            override fun libVersion(): String {
                return EXT_LIB_VERSION
            }

            override fun startScan(id: String, handler: IHandler) {
                val emitter = Emitter.create<Device>(packageName, handler)
                emitters[id] = emitter
                Timber.d("$TAG: startScan $id")
                startScan(emitter)
            }

            override fun stopScan(id: String) {
                Timber.d("$TAG: stopScan $id")
                emitters.remove(id)?.cancel()
            }

            override fun connectDevice(id: String, uid: String, handler: IHandler) {
                val emitter = Emitter.create<DeviceEvent>(packageName, handler)
                emitters[id] = emitter
                Timber.d("$TAG: connectDevice $id $uid")
                connectDevice(uid, emitter)
            }

            override fun disconnectDevice(id: String) {
                Timber.d("$TAG: disconnectDevice $id")
                emitters.remove(id)?.cancel()
            }

            override fun startStream(id: String, typeId: String, handler: IHandler) {
                types.firstOrNull { it.typeId == typeId }?.let {
                    val emitter = Emitter.create<StreamState>(packageName, handler)
                    emitters[id] = emitter
                    Timber.d("$TAG: startStream $id $typeId")
                    it.startStream(emitter)
                }
            }

            override fun stopStream(id: String) {
                Timber.d("$TAG: stopStream $id")
                emitters.remove(id)?.cancel()
            }

            override fun startView(id: String, typeId: String, config: Bundle, handler: IHandler) {
                val viewConfig = config.serializableFromBundle<ViewConfig>() ?: return
                types.firstOrNull { it.typeId == typeId }?.let {
                    val emitter = ViewEmitter(packageName, handler)
                    emitters[id] = emitter
                    Timber.d("$TAG: startView $id $typeId")
                    it.startView(this@KarooExtension, viewConfig, emitter)
                }
            }

            override fun stopView(id: String) {
                Timber.d("$TAG: stopView $id")
                emitters.remove(id)?.cancel()
            }

            override fun startMap(id: String, handler: IHandler) {
                val emitter = Emitter.create<MapEffect>(packageName, handler)
                emitters[id] = emitter
                Timber.d("$TAG: startMap $id")
                startMap(emitter)
            }

            override fun stopMap(id: String) {
                Timber.d("$TAG: stopMap $id")
                emitters.remove(id)?.cancel()
            }

            override fun startFit(id: String, handler: IHandler) {
                val emitter = Emitter.create<FitEffect>(packageName, handler)
                emitters[id] = emitter
                Timber.d("$TAG: startFit $id")
                startFit(emitter)
            }

            override fun stopFit(id: String) {
                Timber.d("$TAG: stopFit $id")
                emitters.remove(id)?.cancel()
            }
        }
    }

    /**
     * Provide a list of pre-defined static data type implementations.
     *
     * This list should match the [DataType.typeId]'s listed in the extension manifest data-type elements.
     */
    open val types: List<DataTypeImpl> = emptyList()

    /**
     * Start to scan for devices.
     *
     * This will only be called if [ExtensionInfo.scansDevices] is true in the extension manifest.
     *
     * @see [Device]
     */
    open fun startScan(emitter: Emitter<Device>) {}

    /**
     * Connect to a device by uid that was previously emitted by [startScan].
     *
     * @see [DeviceEvent]
     */
    open fun connectDevice(uid: String, emitter: Emitter<DeviceEvent>) {}

    /**
     * Start providing effects for the map layer
     *
     * This will be called only if [ExtensionInfo] has `mapLayer` set to true
     *
     * @see [MapEffect]
     * @since 1.1.3
     */
    open fun startMap(emitter: Emitter<MapEffect>) {}

    /**
     * Start providing effects for FIT file writing
     *
     * This will be called only if [ExtensionInfo] has `fitFile` set to true
     *
     * @see [FitEffect]
     */
    open fun startFit(emitter: Emitter<FitEffect>) {}

    /**
     * @suppress
     */
    companion object {
        private const val TAG = "KarooExtension"
    }
}
