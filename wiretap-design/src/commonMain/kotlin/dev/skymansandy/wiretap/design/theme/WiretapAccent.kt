/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.design.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import dev.skymansandy.wiretap.design.util.oklch

/**
 * Single-accent color set. All four accents are intentionally matched at L 0.72–0.78
 * and C ≈ 0.13 so the UI brightness and saturation feel identical across them — only
 * the hue rotates. [foreground] is the high-contrast text color to render on top of
 * [base] (e.g. for primary button labels).
 */
@Immutable
data class WiretapAccentColors(
    val base: Color,
    val soft: Color,
    val line: Color,
    val foreground: Color,
)

enum class WiretapAccent(
    private val l: Float,
    private val c: Float,
    private val h: Float,
    private val fg: Color,
) {
    Cyan(0.74f, 0.135f, 230f, Color(0xFF06121D)),
    Violet(0.72f, 0.13f, 295f, Color(0xFF0D0A16)),
    Green(0.74f, 0.13f, 150f, Color(0xFF06140D)),
    Amber(0.78f, 0.13f, 80f, Color(0xFF1A1405)),
    ;

    fun toColors(): WiretapAccentColors {
        val base = oklch(l, c, h)
        return WiretapAccentColors(
            base = base,
            soft = base.copy(alpha = SOFT_ALPHA),
            line = base.copy(alpha = LINE_ALPHA),
            foreground = fg,
        )
    }

    companion object {
        const val SOFT_ALPHA = 0.14f
        const val LINE_ALPHA = 0.35f
        const val FOCUS_ALPHA = 0.5f
    }
}
