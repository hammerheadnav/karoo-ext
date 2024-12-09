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

package io.hammerhead.sampleext

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.hammerhead.karooext.models.KarooEffect
import io.hammerhead.karooext.models.PerformHardwareAction
import io.hammerhead.karooext.models.ReleaseAnt
import io.hammerhead.karooext.models.RequestAnt
import io.hammerhead.karooext.models.RideState
import io.hammerhead.karooext.models.StreamState
import io.hammerhead.karooext.models.SystemNotification

@Composable
fun TabLayout(
    mainData: MainData,
    dispatchEffect: (KarooEffect) -> Unit,
    makeHttpRequest: (String) -> Unit,
    playBeeps: () -> Unit,
    toggleHomeBackground: () -> Unit,
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Controls", "Data", "Requests")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier.fillMaxWidth(),
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(text = title, fontSize = 12.sp) },
                )
            }
        }

        when (selectedTabIndex) {
            0 -> ControlsTab(
                homeBackgroundSet = mainData.homeBackgroundSet,
                dispatchEffect = dispatchEffect,
                playBeeps = playBeeps,
                toggleHomeBackground = toggleHomeBackground,
            )
            1 -> DataTab(mainData)
            2 -> RequestsTab(
                httpStatus = mainData.httpStatus,
                dispatchEffect = dispatchEffect,
                makeHttpRequest = makeHttpRequest,
            )
        }
    }
}

@Composable
fun ControlsTab(
    homeBackgroundSet: Boolean,
    dispatchEffect: (KarooEffect) -> Unit,
    playBeeps: () -> Unit,
    toggleHomeBackground: () -> Unit,
) {
    var antRequested by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(
            onClick = playBeeps,
            colors = ButtonDefaults.textButtonColors(containerColor = Color.Green, contentColor = Color.Black),
        ) {
            Text("Beep")
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = {
                dispatchEffect(PerformHardwareAction.ControlCenterComboPress)
            },
            colors = ButtonDefaults.textButtonColors(containerColor = Color.Red, contentColor = Color.White),
        ) {
            Text("Control Center")
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = toggleHomeBackground) {
            Text(if (homeBackgroundSet) "Clear Background" else "Set Background")
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = {
                val resource = "samp"
                dispatchEffect(if (antRequested) ReleaseAnt(resource) else RequestAnt(resource))
                antRequested = !antRequested
            },
            colors = ButtonDefaults.textButtonColors(containerColor = Color.Black, contentColor = Color.White),
        ) {
            Text(if (antRequested) "Release ANT" else "Request ANT")
        }
    }
}

@Composable
fun DataTab(mainData: MainData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(text = "Karoo System: " + if (mainData.connected) "Connected" else "Disconnected")
        Text(text = "Ride State: ${mainData.rideState}")
        Text(text = "Power: ${(mainData.power as? StreamState.Streaming)?.dataPoint?.singleValue ?: "--"}")
    }
}

@Composable
fun RequestsTab(
    httpStatus: String?,
    dispatchEffect: (KarooEffect) -> Unit,
    makeHttpRequest: (String) -> Unit,
) {
    val context = LocalContext.current
    var requestPayload by remember { mutableStateOf("Hello Karoo") }
    var notificationMessage by remember { mutableStateOf("You did it!") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TextField(requestPayload, { requestPayload = it }, singleLine = true)
        Button(onClick = {
            makeHttpRequest(requestPayload)
        }) {
            Text("HTTP request")
        }
        httpStatus?.let {
            LaunchedEffect(httpStatus) {
                Toast.makeText(context, httpStatus, Toast.LENGTH_SHORT).show()
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        TextField(notificationMessage, { notificationMessage = it }, singleLine = true)
        Button(
            onClick = {
                dispatchEffect(
                    SystemNotification(
                        "sample-clicked",
                        notificationMessage,
                        "You clicked the notify button in the sample.",
                    ),
                )
            },
        ) {
            Text("System Notification")
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Preview(name = "karoo", device = "spec:width=480px,height=800px,dpi=300")
@Composable
private fun PreviewTabLayout() {
    TabLayout(
        mainData = MainData(
            connected = true,
            power = StreamState.NotAvailable,
            rideState = RideState.Recording,
            homeBackgroundSet = false,
            httpStatus = "I made it!",
        ),
        dispatchEffect = {},
        makeHttpRequest = {},
        playBeeps = {},
        toggleHomeBackground = {},
    )
}
