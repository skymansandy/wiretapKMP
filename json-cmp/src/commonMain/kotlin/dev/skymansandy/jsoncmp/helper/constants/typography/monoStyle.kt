package dev.skymansandy.jsoncmp.helper.constants.typography

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp

// Explicit lineHeight locks all cells to the same height regardless of glyph metrics
// (e.g. unicode ▶/▼ vs ASCII characters have different font ascent/descent values).
internal val monoStyle = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontSize = 12.sp,
    lineHeight = 18.sp,
)
