/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.design.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import dev.skymansandy.wiretap.design.util.oklch

/**
 * Static color tokens that don't depend on the active accent. The accent-dependent
 * pieces (accent base/soft/line/fg + focus ring) live on [WiretapAccentColors] and
 * are merged at theme-construction time.
 */
@Immutable
data class WiretapColors(
    val pageBackdrop: Color,
    val background: Color,
    val surface0: Color,
    val surface1: Color,
    val surface2: Color,
    val surface3: Color,
    val surface4: Color,
    val border1: Color,
    val border2: Color,
    val border3: Color,
    val fg1: Color,
    val fg2: Color,
    val fg3: Color,
    val fg4: Color,
    val accent: Color,
    val accentSoft: Color,
    val accentLine: Color,
    val accentForeground: Color,
    val borderFocus: Color,
    val methodGet: Color,
    val methodPost: Color,
    val methodPut: Color,
    val methodPatch: Color,
    val methodDelete: Color,
    val methodAny: Color,
    val methodWs: Color,
    val methodSse: Color,
    val status1xx: Color,
    val status2xx: Color,
    val status3xx: Color,
    val status4xx: Color,
    val status5xx: Color,
    val mock: Color,
    val throttle: Color,
    val mockSoft: Color,
    val throttleSoft: Color,
)

internal fun wiretapColors(accent: WiretapAccentColors): WiretapColors = WiretapColors(
    pageBackdrop = Color(0xFF050608),
    background = Color(0xFF0A0C10),
    surface0 = Color(0xFF0E1117),
    surface1 = Color(0xFF12161D),
    surface2 = Color(0xFF181D26),
    surface3 = Color(0xFF1F2531),
    surface4 = Color(0xFF262D3A),
    border1 = Color(0xFF1D232D),
    border2 = Color(0xFF262D39),
    border3 = Color(0xFF353D4B),
    fg1 = Color(0xFFECF0F5),
    fg2 = Color(0xFFA4ADBC),
    fg3 = Color(0xFF6C7686),
    fg4 = Color(0xFF4A525F),
    accent = accent.base,
    accentSoft = accent.soft,
    accentLine = accent.line,
    accentForeground = accent.foreground,
    // CSS focus token is a fixed cyan-ish hue regardless of active accent,
    // so the focus ring stays calm even when the brand accent is warm.
    borderFocus = oklch(0.72f, 0.13f, 235f, WiretapAccent.FOCUS_ALPHA),
    methodGet = oklch(0.74f, 0.13f, 230f),
    methodPost = oklch(0.72f, 0.13f, 150f),
    methodPut = oklch(0.76f, 0.13f, 80f),
    methodPatch = oklch(0.72f, 0.13f, 295f),
    methodDelete = oklch(0.70f, 0.16f, 28f),
    methodAny = oklch(0.70f, 0.02f, 240f),
    methodWs = oklch(0.72f, 0.13f, 280f),
    methodSse = oklch(0.72f, 0.13f, 320f),
    status1xx = oklch(0.72f, 0.05f, 240f),
    status2xx = oklch(0.72f, 0.13f, 150f),
    status3xx = oklch(0.76f, 0.12f, 80f),
    status4xx = oklch(0.72f, 0.15f, 50f),
    status5xx = oklch(0.68f, 0.18f, 28f),
    mock = oklch(0.72f, 0.13f, 150f),
    throttle = oklch(0.76f, 0.13f, 70f),
    mockSoft = oklch(0.72f, 0.13f, 150f, alpha = 0.16f),
    throttleSoft = oklch(0.76f, 0.13f, 70f, alpha = 0.16f),
)
