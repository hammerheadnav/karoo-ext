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

package io.hammerhead.karooext.internal

import android.os.Bundle
import io.hammerhead.karooext.BUNDLE_VALUE
import io.hammerhead.karooext.aidl.IHandler
import timber.log.Timber

/**
 * @suppress
 */
inline fun <reified T> Bundle.serializableFromBundle(): T? {
    return getString(BUNDLE_VALUE)?.let {
        DefaultJson.decodeFromString(it)
    }
}

/**
 * @suppress
 */
inline fun <reified T> createConsumer(
    crossinline onNextCallback: (T) -> Unit,
    noinline onErrorCallback: (String) -> Unit,
    noinline onCompleteCallback: () -> Unit,
): IHandler {
    return object : IHandler.Stub() {
        override fun onNext(bundle: Bundle) {
            bundle.serializableFromBundle<T>()?.let {
                onNextCallback(it)
            } ?: run {
                Timber.w("onNext got [${bundle.getString(BUNDLE_VALUE)}] in bundle but couldn't deserialize")
            }
        }

        override fun onError(msg: String) {
            onErrorCallback.invoke(msg)
        }

        override fun onComplete() {
            onCompleteCallback.invoke()
        }
    }
}
