/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ColorPrimary = Color(0xFF90A4AE)
private val SurfaceDark = Color(0xFF1C1C1C)
private val BackgroundDark = Color(0xFF121212)

private val WiretapDarkColorScheme = darkColorScheme(
    primary = ColorPrimary,
    onPrimary = Color.Black,
    secondary = ColorPrimary,
    surface = SurfaceDark,
    surfaceContainer = SurfaceDark,
    background = BackgroundDark,
)

@Composable
internal fun WiretapTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = WiretapDarkColorScheme,
        content = content,
    )
}
