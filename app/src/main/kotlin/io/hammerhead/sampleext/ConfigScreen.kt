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

package io.hammerhead.sampleext

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.sampleext.extension.AppConfig
import io.hammerhead.sampleext.extension.ButtonConfig
import io.hammerhead.sampleext.extension.ConnectionTestResult
import io.hammerhead.sampleext.extension.testHaConnection

@Composable
fun ConfigScreen() {
    val context = LocalContext.current
    val initial = remember { AppConfig.load(context) }

    var baseUrl by remember { mutableStateOf(initial.baseUrl) }
    var accessToken by remember { mutableStateOf(initial.accessToken) }
    var buttons by remember { mutableStateOf(initial.buttons.toMutableList()) }
    var customHeaderKey by remember { mutableStateOf(initial.customHeaderKey) }
    var customHeaderValue by remember { mutableStateOf(initial.customHeaderValue) }
    var headerValueVisible by remember { mutableStateOf(false) }
    var showPhoneSetup by remember { mutableStateOf(false) }
    var testing by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<ConnectionTestResult?>(null) }

    // Requests go through the Karoo system (same route as the buttons), so the test checks the real path.
    val karooSystem = remember { KarooSystemService(context) }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    var testConsumerId by remember { mutableStateOf<String?>(null) }
    DisposableEffect(Unit) {
        karooSystem.connect()
        onDispose {
            mainHandler.removeCallbacksAndMessages(null)
            karooSystem.disconnect()
        }
    }

    fun currentConfig() = AppConfig(
        baseUrl = baseUrl.trim(),
        accessToken = accessToken.trim(),
        customHeaderKey = customHeaderKey.trim(),
        customHeaderValue = customHeaderValue.trim(),
        buttons = buttons.toList(),
    )

    fun runConnectionTest() {
        testing = true
        testResult = null
        val finish: (ConnectionTestResult) -> Unit = { result ->
            mainHandler.post {
                if (testing) {
                    mainHandler.removeCallbacksAndMessages(null)
                    testConsumerId = null
                    testing = false
                    testResult = result
                }
            }
        }
        testConsumerId = testHaConnection(karooSystem, currentConfig(), finish)
        mainHandler.postDelayed({
            if (testing) {
                testConsumerId?.let { karooSystem.removeConsumer(it) }
                testConsumerId = null
                testing = false
                testResult = ConnectionTestResult(
                    false,
                    if (karooSystem.connected) {
                        "No response after 20 s"
                    } else {
                        "Karoo system not reachable — the test only works on a Karoo"
                    },
                )
            }
        }, 20_000)
    }
    var tokenVisible by remember { mutableStateOf(false) }
    var editingIndex by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Home Assistant Settings",
            style = MaterialTheme.typography.headlineSmall,
        )

        // Connection card
        SectionCard(title = "Connection") {
            OutlinedTextField(
                value = baseUrl,
                onValueChange = { baseUrl = it },
                label = { Text("HA Base URL") },
                placeholder = { Text("http://homeassistant.local:8123") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            )
            OutlinedTextField(
                value = accessToken,
                onValueChange = { accessToken = it },
                label = { Text("HA Token") },
                placeholder = { Text("Long-lived access token") },
                supportingText = { Text("HA profile → Security") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (tokenVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { tokenVisible = !tokenVisible }) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = if (tokenVisible) "Hide token" else "Show token",
                            tint = if (tokenVisible) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                },
            )
            OutlinedTextField(
                value = customHeaderKey,
                onValueChange = { customHeaderKey = it },
                label = { Text("Proxy Header Name") },
                placeholder = { Text("CF-Access-Client-Secret") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = customHeaderValue,
                onValueChange = { customHeaderValue = it },
                label = { Text("Proxy Header Value") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = customHeaderKey.isNotBlank(),
                visualTransformation = if (headerValueVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { headerValueVisible = !headerValueVisible }) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = if (headerValueVisible) "Hide header value" else "Show header value",
                            tint = if (headerValueVisible) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                },
            )
            OutlinedButton(
                onClick = { runConnectionTest() },
                enabled = !testing,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (testing) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text("Test connection")
                }
            }
            OutlinedButton(
                onClick = { showPhoneSetup = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Set up from phone")
            }
            testResult?.let { result ->
                Text(
                    text = (if (result.success) "✔ " else "✖ ") + result.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (result.success) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                )
            }
        }

        // Buttons card
        SectionCard(title = "Buttons") {
            buttons.forEachIndexed { index, btn ->
                if (index > 0) HorizontalDivider()
                ButtonRow(
                    slotNumber = index + 1,
                    button = btn,
                    onEditClick = { editingIndex = index },
                )
            }
        }

        // Save + Reset buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = {
                    AppConfig.save(context, currentConfig())
                    showToast(context, "Settings saved")
                },
                modifier = Modifier.weight(1f),
            ) {
                Text("Save")
            }
            TextButton(
                onClick = {
                    AppConfig.reset(context)
                    val defaults = AppConfig.load(context)
                    baseUrl = defaults.baseUrl
                    accessToken = defaults.accessToken
                    customHeaderKey = defaults.customHeaderKey
                    customHeaderValue = defaults.customHeaderValue
                    buttons = defaults.buttons.toMutableList()
                    showToast(context, "Reset to defaults")
                },
            ) {
                Text("Reset")
            }
        }
    }

    if (showPhoneSetup) {
        PhoneSetupDialog(
            prefillBaseUrl = baseUrl,
            prefillHeaderKey = customHeaderKey,
            onDismiss = { showPhoneSetup = false },
            onReceived = { values ->
                values.baseUrl?.let { baseUrl = it }
                values.accessToken?.let { accessToken = it }
                values.headerKey?.let { customHeaderKey = it }
                values.headerValue?.let { customHeaderValue = it }
                showPhoneSetup = false
                showToast(context, "Received from phone — review and tap Save")
            },
            onLockedOut = {
                showPhoneSetup = false
                showToast(context, "Too many wrong PINs — setup stopped")
            },
        )
    }

    // Edit button dialog
    editingIndex?.let { idx ->
        ButtonEditDialog(
            slotNumber = idx + 1,
            initial = buttons[idx],
            onDismiss = { editingIndex = null },
            onConfirm = { updated ->
                buttons = buttons.toMutableList().also { it[idx] = updated }
                editingIndex = null
            },
        )
    }
}

@Composable
private fun PhoneSetupDialog(
    prefillBaseUrl: String,
    prefillHeaderKey: String,
    onDismiss: () -> Unit,
    onReceived: (PhoneSetupServer.SetupValues) -> Unit,
    onLockedOut: () -> Unit,
) {
    val ip = remember { PhoneSetupServer.localIpv4() }
    var address by remember { mutableStateOf<String?>(null) }
    var pin by remember { mutableStateOf("") }
    var startError by remember { mutableStateOf<String?>(null) }

    if (ip != null) {
        DisposableEffect(Unit) {
            val mainHandler = Handler(Looper.getMainLooper())
            val server = PhoneSetupServer(
                prefillBaseUrl = prefillBaseUrl,
                prefillHeaderKey = prefillHeaderKey,
                onReceived = { values -> mainHandler.post { onReceived(values) } },
                onLockedOut = { mainHandler.post { onLockedOut() } },
            )
            try {
                val port = server.start()
                address = "http://$ip:$port"
                pin = server.pin
            } catch (e: Exception) {
                startError = "Could not start setup server: ${e.message}"
            }
            onDispose { server.stop() }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set up from phone") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                when {
                    ip == null -> Text("Connect the Karoo to WiFi first.")
                    startError != null -> Text(startError!!)
                    address != null -> {
                        Text("On a phone in the same WiFi, open:")
                        Text(
                            text = address!!,
                            style = MaterialTheme.typography.titleLarge,
                            fontFamily = FontFamily.Monospace,
                        )
                        Text("PIN:")
                        Text(
                            text = pin,
                            style = MaterialTheme.typography.headlineMedium,
                            fontFamily = FontFamily.Monospace,
                        )
                        Text(
                            text = "Empty fields on the phone keep their current value.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
    )
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
private fun ButtonRow(slotNumber: Int, button: ButtonConfig, onEditClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEditClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Slot $slotNumber: ${button.displayName}", style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "${button.domain}.${button.service}" + (button.entityId?.let { " · $it" } ?: ""),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = onEditClick) {
            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit slot $slotNumber")
        }
    }
}

@Composable
private fun ButtonEditDialog(
    slotNumber: Int,
    initial: ButtonConfig,
    onDismiss: () -> Unit,
    onConfirm: (ButtonConfig) -> Unit,
) {
    var displayName by remember { mutableStateOf(initial.displayName) }
    var domain by remember { mutableStateOf(initial.domain) }
    var service by remember { mutableStateOf(initial.service) }
    var entityId by remember { mutableStateOf(initial.entityId ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Slot $slotNumber") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Display Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = domain,
                    onValueChange = { domain = it },
                    label = { Text("Domain") },
                    placeholder = { Text("cover, light, switch, script…") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = service,
                    onValueChange = { service = it },
                    label = { Text("Service") },
                    placeholder = { Text("toggle, turn_on, turn_off…") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = entityId,
                    onValueChange = { entityId = it },
                    label = { Text("Entity ID (optional)") },
                    placeholder = { Text("cover.garage") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        initial.copy(
                            displayName = displayName.trim(),
                            domain = domain.trim(),
                            service = service.trim(),
                            entityId = entityId.trim().takeIf { it.isNotEmpty() },
                        ),
                    )
                },
                enabled = displayName.isNotBlank() && domain.isNotBlank() && service.isNotBlank(),
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

private fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}
