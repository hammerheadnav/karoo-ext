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

package io.hammerhead.karooext

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.RemoteException
import io.hammerhead.karooext.aidl.IKarooSystem
import io.hammerhead.karooext.internal.KarooSystemListener
import io.hammerhead.karooext.internal.bundleWithSerializable
import io.hammerhead.karooext.internal.createConsumer
import io.hammerhead.karooext.internal.serializableFromBundle
import io.hammerhead.karooext.models.ActiveRidePage
import io.hammerhead.karooext.models.ActiveRideProfile
import io.hammerhead.karooext.models.Bikes
import io.hammerhead.karooext.models.HardwareType
import io.hammerhead.karooext.models.KarooEffect
import io.hammerhead.karooext.models.KarooEvent
import io.hammerhead.karooext.models.KarooEventParams
import io.hammerhead.karooext.models.KarooInfo
import io.hammerhead.karooext.models.Lap
import io.hammerhead.karooext.models.OnGlobalPOIs
import io.hammerhead.karooext.models.OnLocationChanged
import io.hammerhead.karooext.models.OnMapZoomLevel
import io.hammerhead.karooext.models.OnNavigationState
import io.hammerhead.karooext.models.RideState
import io.hammerhead.karooext.models.SavedDevices
import io.hammerhead.karooext.models.UserProfile
import timber.log.Timber
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Karoo System Service for interaction with Karoo-specific state and hardware.
 *
 * @sample [karooSystemUsage]
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class KarooSystemService(private val context: Context) {
    private val listeners = ConcurrentHashMap<String, KarooSystemListener>()
    private var controller: IKarooSystem? = null

    /**
     * @suppress
     */
    val packageName: String by lazy { context.packageName }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val controller = IKarooSystem.Stub.asInterface(service)
            try {
                Timber.i("$TAG: connected with libVersion=${controller.libVersion()}")
            } catch (e: RemoteException) {
                Timber.w("$TAG: error connecting ${e.message}")
            }
            this@KarooSystemService.controller = controller
            listeners.forEach { (_, listener) ->
                listener.register(controller)
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Timber.i("$TAG: disconnected")
            clearController()
        }

        override fun onBindingDied(name: ComponentName) {
            Timber.w("$TAG: binding died")
            clearController()
            context.unbindService(this)
            connect()
        }

        private fun clearController() {
            this@KarooSystemService.controller = null
            listeners.forEach { (_, listener) ->
                listener.register(null)
            }
        }
    }

    /**
     * Connect to KarooSystem
     */
    fun connect(
        /**
         * Callback for when Karoo system connects and disconnects
         */
        onConnection: ((Boolean) -> Unit)? = null,
    ) {
        Timber.i("$TAG: binding to KarooSystem")
        val intent = Intent()
        intent.component = ComponentName.createRelative("io.hammerhead.appstore", ".service.AppStoreService")
        intent.action = "KarooSystem"
        intent.putExtra(BUNDLE_PACKAGE, packageName)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        onConnection?.let {
            addConsumer(object : KarooSystemListener("onConnection") {
                override fun register(controller: IKarooSystem?) {
                    onConnection(controller != null)
                }

                override fun unregister(controller: IKarooSystem?) {
                }
            })
        }
    }

    /**
     * Disconnect from KarooSystem and unregister all consumers
     */
    fun disconnect() {
        listeners.forEach { (id, _) ->
            removeConsumer(id)
        }
        context.unbindService(serviceConnection)
        controller = null
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
            it.dispatchEffect(effect.bundleWithSerializable(packageName))
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
        val consumerId = UUID.randomUUID().toString()
        val onErrorWrapper = { msg: String ->
            onError?.invoke(msg) ?: Timber.i("$TAG: Unhandled error: $msg")
            removeConsumer(consumerId)
        }
        val onCompleteWrapper = {
            onComplete?.invoke() ?: Timber.i("$TAG: Unhandled complete")
            removeConsumer(consumerId)
        }
        val consumer = createConsumer<T>(onEvent, onErrorWrapper, onCompleteWrapper)
        return addConsumer(object : KarooSystemListener(consumerId) {
            override fun register(controller: IKarooSystem?) {
                controller?.addEventConsumer(id, params.bundleWithSerializable(packageName), consumer)
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
            UserProfile::class -> UserProfile.Params
            OnLocationChanged::class -> OnLocationChanged.Params
            OnGlobalPOIs::class -> OnGlobalPOIs.Params
            OnNavigationState::class -> OnNavigationState.Params
            OnMapZoomLevel::class -> OnMapZoomLevel.Params
            SavedDevices::class -> SavedDevices.Params
            Bikes::class -> Bikes.Params
            ActiveRideProfile::class -> ActiveRideProfile.Params
            ActiveRidePage::class -> ActiveRidePage.Params
            else -> throw IllegalArgumentException("No default KarooEventParams for ${T::class}")
        }
        return addConsumer<T>(params, onError, onComplete, onEvent)
    }

    /**
     * Unregister a consume from events.
     */
    fun removeConsumer(consumerId: String) {
        val listener = listeners.remove(consumerId)
        Timber.d("$TAG: removeConsumer $consumerId=$listener")
        listener?.unregister(controller)
    }

    /**
     * @suppress
     */
    fun addConsumer(listener: KarooSystemListener): String {
        Timber.d("$TAG: addConsumer ${listener.id}=$listener")
        listeners[listener.id] = listener
        controller?.let { listener.register(it) }
        return listener.id
    }

    /**
     * @suppress
     */
    companion object {
        const val TAG = "KarooSystem"
    }
}
