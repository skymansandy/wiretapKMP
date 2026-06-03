/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.design.theme

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf

private val LocalWiretapColors = compositionLocalOf<WiretapColors> {
    error("WiretapDesignTheme not provided")
}
private val LocalWiretapTypography = compositionLocalOf<WiretapTypography> {
    error("WiretapDesignTheme not provided")
}
private val LocalWiretapShapes = staticCompositionLocalOf { DefaultWiretapShapes }
private val LocalWiretapElevation = staticCompositionLocalOf { DefaultWiretapElevation }
private val LocalWiretapAccent = staticCompositionLocalOf { WiretapAccent.Cyan }
private val LocalWiretapDensity = staticCompositionLocalOf { WiretapDensity.Regular }

/**
 * Apply the Wiretap design system to a subtree. Swapping [accent] or [density]
 * triggers a recomposition so the entire surface re-themes in place — the
 * showcase relies on this for its live accent / density switchers.
 *
 * A minimal Material3 [MaterialTheme] is layered underneath so that any
 * Material primitive used inside components (ripple, Text defaults, etc.)
 * picks up sensible dark colors instead of the M3 light defaults.
 */
@Composable
fun WiretapDesignTheme(
    accent: WiretapAccent = WiretapAccent.Cyan,
    density: WiretapDensity = WiretapDensity.Regular,
    content: @Composable () -> Unit,
) {
    val accentColors = accent.toColors()
    val colors = wiretapColors(accentColors)
    val typography = rememberWiretapTypography()

    val m3Scheme = darkColorScheme(
        primary = colors.accent,
        onPrimary = colors.accentForeground,
        background = colors.background,
        onBackground = colors.fg1,
        surface = colors.surface0,
        onSurface = colors.fg1,
        surfaceVariant = colors.surface2,
        onSurfaceVariant = colors.fg2,
        outline = colors.border2,
        outlineVariant = colors.border1,
        error = colors.status5xx,
        onError = colors.fg1,
    )

    CompositionLocalProvider(
        LocalWiretapColors provides colors,
        LocalWiretapTypography provides typography,
        LocalWiretapShapes provides DefaultWiretapShapes,
        LocalWiretapElevation provides DefaultWiretapElevation,
        LocalWiretapAccent provides accent,
        LocalWiretapDensity provides density,
        LocalContentColor provides colors.fg1,
        LocalTextStyle provides typography.body.copy(color = colors.fg1),
    ) {
        MaterialTheme(colorScheme = m3Scheme, content = content)
    }
}

/**
 * Accessor for tokens inside composables — mirrors the `MaterialTheme.colors`
 * pattern the rest of the codebase already uses (e.g.
 * `WiretapDesign.colors.surface1`).
 */
object WiretapDesign {
    val colors: WiretapColors
        @Composable @ReadOnlyComposable get() = LocalWiretapColors.current
    val typography: WiretapTypography
        @Composable @ReadOnlyComposable get() = LocalWiretapTypography.current
    val shapes: WiretapShapes
        @Composable @ReadOnlyComposable get() = LocalWiretapShapes.current
    val elevation: WiretapElevation
        @Composable @ReadOnlyComposable get() = LocalWiretapElevation.current
    val accent: WiretapAccent
        @Composable @ReadOnlyComposable get() = LocalWiretapAccent.current
    val density: WiretapDensity
        @Composable @ReadOnlyComposable get() = LocalWiretapDensity.current
}
