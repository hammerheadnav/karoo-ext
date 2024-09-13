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

package io.hammerhead.karooext

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.RemoteException
import io.hammerhead.karooext.aidl.IKarooSystem
import io.hammerhead.karooext.internal.KarooSystemListener
import io.hammerhead.karooext.internal.bundleWithSerializable
import io.hammerhead.karooext.internal.createConsumer
import io.hammerhead.karooext.internal.serializableFromBundle
import io.hammerhead.karooext.models.HardwareType
import io.hammerhead.karooext.models.KarooEffect
import io.hammerhead.karooext.models.KarooEvent
import io.hammerhead.karooext.models.KarooEventParams
import io.hammerhead.karooext.models.KarooInfo
import io.hammerhead.karooext.models.Lap
import io.hammerhead.karooext.models.RideState
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

/**
 * Karoo System Service for interaction with Karoo-specific state and hardware.
 */
class KarooSystemService(private val context: Context) {
    private val listeners = ConcurrentHashMap<String, KarooSystemListener>()
    private var controller: IKarooSystem? = null
    private val handler = Handler(Looper.getMainLooper())

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val controller = IKarooSystem.Stub.asInterface(service)
            try {
                Timber.i("$TAG: connected with libVersion=${controller.libVersion()}")
            } catch (e: RemoteException) {
                Timber.w("$TAG: error connecting ${e.message}")
            }
            listeners.forEach { (_, listener) ->
                listener.register(controller)
            }
            this@KarooSystemService.controller = controller
        }

        override fun onServiceDisconnected(className: ComponentName) {
            unbindService()
            Timber.i("$TAG: disconnected")
            listeners.forEach { (_, listener) ->
                listener.register(null)
            }
            this@KarooSystemService.controller = null
            handler.postDelayed({
                bindService()
            }, 2000)
        }
    }

    private fun bindService() {
        val intent = Intent()
        intent.component = ComponentName.createRelative("io.hammerhead.appstore", ".service.AppStoreService")
        intent.action = "KarooSystem"
        intent.putExtra("caller", context.packageName)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun unbindService() {
        context.unbindService(serviceConnection)
    }

    /**
     * KarooSystem is connected and ready for calls.
     */
    val connected: Boolean
        get() = controller != null

    /**
     * Get the version of ext lib service is running.
     */
    val libVersion: String?
        get() = controller?.libVersion()

    /**
     * Get information about the connected Karoo System.
     *
     * @see [KarooInfo]
     */
    val info: KarooInfo?
        get() = controller?.info()?.serializableFromBundle<KarooInfo>()

    /**
     * Get the serial of the connected Karoo System.
     */
    val serial: String?
        get() = info?.serial

    /**
     * Get the hardware type of the connected Karoo System.
     */
    val hardwareType: HardwareType?
        get() = info?.hardwareType

    /**
     * Send a [KarooEffect] to the Karoo System service for handling.
     *
     * @return true if system was connected and ready to receive effect
     * @see [KarooEffect]
     */
    fun dispatch(effect: KarooEffect): Boolean {
        return controller?.let {
            it.dispatchEffect(effect.bundleWithSerializable())
            true
        } ?: false
    }

    /**
     * Register a listener with params to events or state changes as they happen.
     *
     * This can be registered before Karoo System connects and will persist through reconnects until unregistered.
     *
     * @return `consumerId` to be removed on teardown
     * @see [KarooEvent]
     * @see [KarooEventParams]
     * @see [removeConsumer]
     */
    inline fun <reified T : KarooEvent> addConsumer(
        params: KarooEventParams,
        noinline onError: ((String) -> Unit)? = null,
        noinline onComplete: (() -> Unit)? = null,
        noinline onEvent: (T) -> Unit,
    ): String {
        val consumer = createConsumer<T>(onEvent, onError, onComplete)
        return addConsumer(object : KarooSystemListener() {
            override fun register(controller: IKarooSystem?) {
                controller?.addEventConsumer(id, params.bundleWithSerializable(), consumer)
            }

            override fun unregister(controller: IKarooSystem?) {
                controller?.removeEventConsumer(id)
            }
        })
    }

    /**
     * Register a listener to events or state changes as they happen.
     *
     * This can be registered before Karoo System connects and will persist through reconnects until unregistered.
     *
     * @return `consumerId` to be removed on teardown
     * @see [KarooEvent]
     * @see [removeConsumer]
     */
    inline fun <reified T : KarooEvent> addConsumer(
        noinline onError: ((String) -> Unit)? = null,
        noinline onComplete: (() -> Unit)? = null,
        noinline onEvent: (T) -> Unit,
    ): String {
        val params: KarooEventParams = when (T::class) {
            RideState::class -> RideState.Params
            Lap::class -> Lap.Params
            else -> throw IllegalArgumentException("No default KarooEventParams for ${T::class}")
        }
        return addConsumer<T>(params, onError, onComplete, onEvent)
    }

    /**
     * Register to be called when the Karoo System connects.
     *
     * @return `consumerId` to be removed on teardown
     * @see [removeConsumer]
     */
    fun registerConnectionListener(onConnection: (Boolean) -> Unit): String {
        return addConsumer(object : KarooSystemListener() {
            override fun register(controller: IKarooSystem?) {
                onConnection(controller != null)
            }

            override fun unregister(controller: IKarooSystem?) {
            }
        })
    }

    /**
     * Unregister a consume from events.
     */
    fun removeConsumer(consumerId: String) {
        val listener = listeners.remove(consumerId)
        Timber.d("$TAG: removeConsumer $consumerId=$listener")
        listener?.unregister(controller)
        if (listener != null && listeners.isEmpty()) {
            unbindService()
        }
    }

    /**
     * @suppress
     */
    fun addConsumer(listener: KarooSystemListener): String {
        Timber.d("$TAG: addConsumer ${listener.id}=$listener")
        listeners[listener.id] = listener
        if (listeners.size == 1) {
            bindService()
        } else {
            controller?.let { listener.register(it) }
        }
        return listener.id
    }

    /**
     * @suppress
     */
    companion object {
        private const val TAG = "KarooSystem"
    }
}
