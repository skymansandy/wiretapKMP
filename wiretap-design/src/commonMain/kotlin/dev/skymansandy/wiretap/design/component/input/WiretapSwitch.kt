/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.design.component.input

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.design.theme.WiretapDesign

@Composable
fun WiretapSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val c = WiretapDesign.colors
    val track by animateColorAsState(
        if (checked) c.accent.copy(alpha = 0.28f) else c.surface3,
        label = "trackColor",
    )
    val border by animateColorAsState(
        if (checked) c.accentLine else c.border2,
        label = "trackBorder",
    )
    val thumb by animateColorAsState(if (checked) c.accent else c.fg2, label = "thumbColor")
    val offset by animateDpAsState(if (checked) 16.dp else 0.dp, label = "thumbOffset")

    Box(
        modifier = modifier
            .size(width = 36.dp, height = 20.dp)
            .background(track, RoundedCornerShape(50))
            .border(1.dp, border, RoundedCornerShape(50))
            .clickable(enabled = enabled) { onCheckedChange(!checked) },
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            Modifier
                .offset(x = 2.dp + offset)
                .size(14.dp)
                .background(thumb, CircleShape),
        )
    }
}
