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

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.ExperimentalGlanceRemoteViewsApi
import androidx.glance.appwidget.action.actionSendBroadcast
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import io.hammerhead.sampleext.R

private val countdownRingDrawables = mapOf(
    5 to R.drawable.countdown_ring_5,
    4 to R.drawable.countdown_ring_4,
    3 to R.drawable.countdown_ring_3,
    2 to R.drawable.countdown_ring_2,
    1 to R.drawable.countdown_ring_1,
)

/**
 * Glance composable for the visual countdown button.
 *
 * Shows a blue background with the button name when idle. During the 5-second
 * confirmation countdown shows a dark background with a circular ring draining
 * from full (yellow) to empty (red) one second at a time.
 *
 * @param buttonName Display name of the HA button (shown when idle).
 * @param receiverKey The key used in [HaButtonReceiver] for this data type (its typeId).
 * @param interactive false in preview/page-edit mode: no click handler so Karoo receives the long-press.
 * @param remainingSeconds null = idle; 1–5 = countdown in progress.
 */
@OptIn(ExperimentalGlanceRemoteViewsApi::class)
@Composable
fun CountdownVisualView(
    buttonName: String,
    receiverKey: String,
    remainingSeconds: Int?,
    interactive: Boolean = true,
) {
    val context = LocalContext.current
    val isConfirmation = remainingSeconds != null
    val ringDrawable = if (remainingSeconds != null) countdownRingDrawables[remainingSeconds] else null

    val clickIntent = Intent(ACTION_HA_BUTTON_CLICKED).apply {
        setClass(context, HaButtonReceiver::class.java)
        // Unique data per button + state: PendingIntent matching ignores extras, so without a
        // distinct Uri here all buttons' intents are filterEquals-equal and collapse onto one
        // PendingIntent — pressing one button would fire whichever was composed last.
        data = Uri.parse("ha-button://$receiverKey/${if (isConfirmation) "confirm" else "press"}")
        putExtra(EXTRA_BUTTON_ID, receiverKey)
        putExtra("confirmation", isConfirmation)
    }

    // Outer box: full area, black background (matches Karoo display), handles click
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Color.Black))
            .then(if (interactive) GlanceModifier.clickable(actionSendBroadcast(clickIntent)) else GlanceModifier)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (ringDrawable != null) {
            // Countdown active: "TAP AGAIN" text with a small ring below replacing the numeric countdown
            Column(
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                verticalAlignment = Alignment.Vertical.CenterVertically,
            ) {
                Text(
                    text = "TAP AGAIN",
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
                Image(
                    provider = ImageProvider(ringDrawable),
                    contentDescription = "$remainingSeconds seconds remaining",
                    modifier = GlanceModifier.size(32.dp).padding(top = 4.dp),
                )
            }
        } else {
            // Idle: pill-shaped blue button with rounded corners, like native Karoo buttons
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(Color(0xFF2196F3)))
                    .cornerRadius(12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = buttonName,
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }
        }
    }
}
