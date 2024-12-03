package io.hammerhead.sampleext.extension

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.unit.ColorProvider
import io.hammerhead.karooext.models.ViewConfig
import io.hammerhead.sampleext.R

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 200, heightDp = 150)
@Composable
fun CustomSpeed(speed: Int, dataAlignment: ViewConfig.Alignment) {
    Box(
        modifier = GlanceModifier.fillMaxSize().padding(start = 10.dp),
        contentAlignment = Alignment(
            vertical = Alignment.Vertical.CenterVertically,
            // Align circle opposite of data
            horizontal = when (dataAlignment) {
                ViewConfig.Alignment.LEFT -> Alignment.Horizontal.End
                ViewConfig.Alignment.CENTER,
                ViewConfig.Alignment.RIGHT,
                -> Alignment.Horizontal.Start
            },
        ),
    ) {
        Image(
            colorFilter = ColorFilter.tint(
                ColorProvider(
                    when {
                        speed < 1 -> Color.Red
                        speed < 5 -> Color.Yellow
                        else -> Color.Green
                    },
                ),
            ),
            provider = ImageProvider(R.drawable.ic_circle),
            contentDescription = "",
            modifier = GlanceModifier.size(50.dp, 50.dp),
        )
    }
}
