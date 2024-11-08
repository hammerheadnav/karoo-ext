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

package io.hammerhead.karooext.models

import kotlinx.serialization.Serializable

/**
 * HTTP response state.
 *
 * @see [OnHttpResponse]
 */
@Serializable
sealed class HttpResponseState {
    @Serializable
    data object Queued : HttpResponseState()

    @Serializable
    data object InProgress : HttpResponseState()

    @Serializable
    data class Complete(
        val statusCode: Int,
        val headers: Map<String, String>,
        val body: ByteArray?,
        val error: String?,
    ) : HttpResponseState()
}
