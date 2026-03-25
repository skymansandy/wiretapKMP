package dev.skymansandy.wiretap.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ToolbarGrey = Color(0xFF2C2C2C)

private val WiretapDarkColorScheme = darkColorScheme(
    surface = ToolbarGrey,
    surfaceContainer = ToolbarGrey,
)

@Composable
internal fun WiretapTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = WiretapDarkColorScheme,
        content = content,
    )
}
