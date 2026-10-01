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

package io.hammerhead.karooext.models

import io.hammerhead.karooext.internal.DefaultJson
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pins the exact JSON wire format of the models shared over the Karoo System AIDL boundary.
 */
class SerializationRoundTripTest {

    @Test
    fun streamStateStreamingWireFormat() {
        val value = StreamState.Streaming(
            DataPoint(
                dataTypeId = "TYPE_SPEED_ID",
                values = mapOf("FIELD_SPEED_ID" to 12.5),
                sourceId = "sensor-1",
            ),
        )
        assertEquals(STREAMING_JSON, DefaultJson.encodeToString<StreamState>(value))
        assertEquals(value, DefaultJson.decodeFromString<StreamState>(STREAMING_JSON))
    }

    @Test
    fun streamStateSearchingWireFormat() {
        assertEquals(SEARCHING_JSON, DefaultJson.encodeToString<StreamState>(StreamState.Searching))
        assertEquals(StreamState.Searching, DefaultJson.decodeFromString<StreamState>(SEARCHING_JSON))
    }

    @Test
    fun karooEffectWireFormat() {
        val notification = SystemNotification(
            id = "notif-1",
            message = "Ride paused",
            subText = "Tap to resume",
            header = "Karoo",
            style = SystemNotification.Style.EVENT,
            action = "Resume",
            actionIntent = null,
        )
        assertEquals(SYSTEM_NOTIFICATION_JSON, DefaultJson.encodeToString<KarooEffect>(notification))
        assertEquals(notification, DefaultJson.decodeFromString<KarooEffect>(SYSTEM_NOTIFICATION_JSON))

        val alert = InRideAlert(
            id = "alert-1",
            icon = 1,
            title = "Speed",
            detail = "Slow down",
            autoDismissMs = 5_000L,
            backgroundColor = 2,
            textColor = 3,
        )
        assertEquals(IN_RIDE_ALERT_JSON, DefaultJson.encodeToString<KarooEffect>(alert))
        assertEquals(alert, DefaultJson.decodeFromString<KarooEffect>(IN_RIDE_ALERT_JSON))
    }

    @Test
    fun karooEventParamsWireFormat() {
        val streaming = OnStreamState.StartStreaming(dataTypeId = "TYPE_SPEED_ID")
        assertEquals(START_STREAMING_JSON, DefaultJson.encodeToString<KarooEventParams>(streaming))
        assertEquals(streaming, DefaultJson.decodeFromString<KarooEventParams>(START_STREAMING_JSON))

        assertEquals(RIDE_STATE_PARAMS_JSON, DefaultJson.encodeToString<KarooEventParams>(RideState.Params))
        assertEquals(RideState.Params, DefaultJson.decodeFromString<KarooEventParams>(RIDE_STATE_PARAMS_JSON))
    }

    @Test
    fun deviceEventWireFormat() {
        val dataPoint = OnDataPoint(
            DataPoint(
                dataTypeId = "TYPE_POWER_ID",
                values = mapOf("FIELD_POWER_ID" to 250.0),
                sourceId = "sensor-2",
            ),
        )
        assertEquals(ON_DATA_POINT_JSON, DefaultJson.encodeToString<DeviceEvent>(dataPoint))
        assertEquals(dataPoint, DefaultJson.decodeFromString<DeviceEvent>(ON_DATA_POINT_JSON))

        val connection = OnConnectionStatus(ConnectionStatus.CONNECTED)
        assertEquals(ON_CONNECTION_STATUS_JSON, DefaultJson.encodeToString<DeviceEvent>(connection))
        assertEquals(connection, DefaultJson.decodeFromString<DeviceEvent>(ON_CONNECTION_STATUS_JSON))
    }

    @Test
    fun fitEffectWireFormat() {
        val value = WriteToRecordMesg(values = listOf(FieldValue(fieldNum = 1, value = 250.0)))
        assertEquals(FIT_EFFECT_JSON, DefaultJson.encodeToString<FitEffect>(value))
        assertEquals(value, DefaultJson.decodeFromString<FitEffect>(FIT_EFFECT_JSON))
    }

    @Test
    fun mapEffectWireFormat() {
        val value = ShowSymbols(
            symbols = listOf(
                Symbol.POI(
                    id = "poi-1",
                    lat = 60.2,
                    lng = 24.9,
                    type = Symbol.POI.Types.COFFEE,
                    name = "Cafe",
                    distancesAlongRoute = listOf(100.0, 250.0),
                ),
            ),
        )
        assertEquals(SHOW_SYMBOLS_JSON, DefaultJson.encodeToString<MapEffect>(value))
        assertEquals(value, DefaultJson.decodeFromString<MapEffect>(SHOW_SYMBOLS_JSON))
    }

    @Test
    fun viewEventWireFormat() {
        val value = UpdateGraphicConfig(showHeader = true, formatDataTypeId = "TYPE_SPEED_ID")
        assertEquals(UPDATE_GRAPHIC_CONFIG_JSON, DefaultJson.encodeToString<ViewEvent>(value))
        assertEquals(value, DefaultJson.decodeFromString<ViewEvent>(UPDATE_GRAPHIC_CONFIG_JSON))
    }

    @Test
    fun viewConfigWireFormat() {
        val value = ViewConfig(
            gridSize = Pair(60, 15),
            viewSize = Pair(300, 200),
            textSize = 24,
            alignment = ViewConfig.Alignment.CENTER,
            boundariesEnabled = true,
            preview = false,
        )
        assertEquals(VIEW_CONFIG_JSON, DefaultJson.encodeToString(value))
        assertEquals(value, DefaultJson.decodeFromString<ViewConfig>(VIEW_CONFIG_JSON))
    }

    @Test
    fun crossTypeDecodeMatchesBaseEncodedPayload() {
        val location = OnLocationChanged(lat = 60.2, lng = 24.9, orientation = 90.0)
        assertEquals(
            location,
            DefaultJson.decodeFromString<OnLocationChanged>(DefaultJson.encodeToString<KarooEvent>(location)),
        )

        val rideState = RideState.Paused(auto = true)
        assertEquals(
            rideState,
            DefaultJson.decodeFromString<RideState>(DefaultJson.encodeToString<KarooEvent>(rideState)),
        )
    }

    @Test
    fun httpResponseStateCompleteKeepsBody() {
        val body = byteArrayOf(1, 2, 3)
        val value = HttpResponseState.Complete(
            statusCode = 200,
            headers = mapOf("Content-Type" to "application/json"),
            body = body,
            error = null,
        )
        val decoded = DefaultJson.decodeFromString<HttpResponseState>(
            DefaultJson.encodeToString<HttpResponseState>(value),
        ) as HttpResponseState.Complete
        assertEquals(value.statusCode, decoded.statusCode)
        assertEquals(value.headers, decoded.headers)
        assertTrue(body.contentEquals(decoded.body))
        assertNull(decoded.error)
    }

    @Test
    fun decodeToleratesOptionalAndUnknownFields() {
        val withoutSourceId = """{"dataTypeId":"TYPE_SPEED_ID","values":{"FIELD_SPEED_ID":12.5}}"""
        assertEquals(
            DataPoint(dataTypeId = "TYPE_SPEED_ID", values = mapOf("FIELD_SPEED_ID" to 12.5)),
            DefaultJson.decodeFromString<DataPoint>(withoutSourceId),
        )

        val unknownKey = """{"dataTypeId":"TYPE_SPEED_ID","values":{},"sourceId":"sensor-1","unknown":"ignored"}"""
        assertEquals(
            DataPoint(dataTypeId = "TYPE_SPEED_ID", sourceId = "sensor-1"),
            DefaultJson.decodeFromString<DataPoint>(unknownKey),
        )
    }

    private companion object {
        const val STREAMING_JSON = """{"type":"io.hammerhead.karooext.models.StreamState.Streaming","dataPoint":{"dataTypeId":"TYPE_SPEED_ID","values":{"FIELD_SPEED_ID":12.5},"sourceId":"sensor-1"}}"""
        const val SEARCHING_JSON = """{"type":"io.hammerhead.karooext.models.StreamState.Searching"}"""
        const val SYSTEM_NOTIFICATION_JSON = """{"type":"io.hammerhead.karooext.models.SystemNotification","id":"notif-1","message":"Ride paused","subText":"Tap to resume","header":"Karoo","style":"EVENT","action":"Resume"}"""
        const val IN_RIDE_ALERT_JSON = """{"type":"io.hammerhead.karooext.models.InRideAlert","id":"alert-1","icon":1,"title":"Speed","detail":"Slow down","autoDismissMs":5000,"backgroundColor":2,"textColor":3}"""
        const val START_STREAMING_JSON = """{"type":"io.hammerhead.karooext.models.OnStreamState.StartStreaming","dataTypeId":"TYPE_SPEED_ID"}"""
        const val RIDE_STATE_PARAMS_JSON = """{"type":"io.hammerhead.karooext.models.RideState.Params"}"""
        const val ON_DATA_POINT_JSON = """{"type":"io.hammerhead.karooext.models.OnDataPoint","dataPoint":{"dataTypeId":"TYPE_POWER_ID","values":{"FIELD_POWER_ID":250.0},"sourceId":"sensor-2"}}"""
        const val ON_CONNECTION_STATUS_JSON = """{"type":"io.hammerhead.karooext.models.OnConnectionStatus","status":"CONNECTED"}"""
        const val FIT_EFFECT_JSON = """{"type":"io.hammerhead.karooext.models.WriteToRecordMesg","values":[{"fieldNum":1,"value":250.0}]}"""
        const val SHOW_SYMBOLS_JSON = """{"type":"io.hammerhead.karooext.models.ShowSymbols","symbols":[{"type":"io.hammerhead.karooext.models.Symbol.POI","id":"poi-1","lat":60.2,"lng":24.9,"poiType":"coffee","name":"Cafe","distancesAlongRoute":[100.0,250.0]}]}"""
        const val UPDATE_GRAPHIC_CONFIG_JSON = """{"type":"io.hammerhead.karooext.models.UpdateGraphicConfig","showHeader":true,"formatDataTypeId":"TYPE_SPEED_ID"}"""
        const val VIEW_CONFIG_JSON = """{"gridSize":{"first":60,"second":15},"viewSize":{"first":300,"second":200},"textSize":24,"alignment":"CENTER","boundariesEnabled":true,"preview":false}"""
    }
}
