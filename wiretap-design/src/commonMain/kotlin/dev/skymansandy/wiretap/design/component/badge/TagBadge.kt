/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.design.component.badge

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.design.theme.WiretapDesign

enum class TagKind { Default, Mock, Throttle, Accent }

@Composable
fun TagBadge(
    label: String,
    kind: TagKind = TagKind.Default,
    modifier: Modifier = Modifier,
) {
    val c = WiretapDesign.colors
    val (fg, border, bg) = when (kind) {
        TagKind.Default -> Triple(c.fg2, c.border3, Color.Transparent)
        TagKind.Mock -> Triple(c.mock, c.mock.copy(alpha = 0.40f), c.mockSoft)
        TagKind.Throttle -> Triple(c.throttle, c.throttle.copy(alpha = 0.40f), c.throttleSoft)
        TagKind.Accent -> Triple(c.accent, c.accentLine, c.accentSoft)
    }
    val shape = RoundedCornerShape(3.dp)
    Box(
        modifier = modifier
            .background(bg, shape)
            .border(1.dp, border, shape)
            .padding(horizontal = 5.dp, vertical = 2.dp),
    ) {
        Text(text = label, style = WiretapDesign.typography.micro, color = fg)
    }
}
