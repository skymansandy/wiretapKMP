/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.design.component.button

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.skymansandy.wiretap.design.theme.WiretapDesign

enum class WiretapButtonStyle { Primary, Secondary, Ghost, Danger }
enum class WiretapButtonSize { Default, Sm }

@Composable
fun WiretapButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: WiretapButtonStyle = WiretapButtonStyle.Primary,
    size: WiretapButtonSize = WiretapButtonSize.Default,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val c = WiretapDesign.colors
    val (bg, fg, border) = when (style) {
        WiretapButtonStyle.Primary -> Triple(c.accent, c.accentForeground, Color.Transparent)
        WiretapButtonStyle.Secondary -> Triple(c.surface2, c.fg1, c.border2)
        WiretapButtonStyle.Ghost -> Triple(Color.Transparent, c.fg2, Color.Transparent)
        WiretapButtonStyle.Danger -> Triple(c.status5xx, Color(0xFF1A0D0D), Color.Transparent)
    }
    val shape = when (size) {
        WiretapButtonSize.Default -> WiretapDesign.shapes.md
        WiretapButtonSize.Sm -> WiretapDesign.shapes.sm
    }
    val pad = when (size) {
        WiretapButtonSize.Default -> PaddingValues(horizontal = 14.dp, vertical = 10.dp)
        WiretapButtonSize.Sm -> PaddingValues(horizontal = 10.dp, vertical = 6.dp)
    }
    val textStyle = when (size) {
        WiretapButtonSize.Default -> WiretapDesign.typography.title.copy(fontSize = 13.sp, color = fg)
        WiretapButtonSize.Sm -> WiretapDesign.typography.label.copy(fontSize = 11.sp, color = fg)
    }
    Row(
        modifier = modifier
            .clip(shape)
            .background(bg, shape)
            .then(if (border != Color.Transparent) Modifier.border(1.dp, border, shape) else Modifier)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(pad)
            .alpha(if (enabled) 1f else 0.4f),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(LocalContentColor provides fg) {
            ProvideTextStyle(textStyle) { content() }
        }
    }
}

@Composable
fun WiretapButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: WiretapButtonStyle = WiretapButtonStyle.Primary,
    size: WiretapButtonSize = WiretapButtonSize.Default,
    enabled: Boolean = true,
) = WiretapButton(onClick, modifier, style, size, enabled) { Text(text) }
