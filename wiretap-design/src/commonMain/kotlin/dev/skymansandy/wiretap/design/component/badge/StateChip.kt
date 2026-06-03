/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.design.component.badge

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import dev.skymansandy.wiretap.design.theme.WiretapDesign

enum class ConnectionState { Open, Connecting, Closed, Failed }

@Composable
fun StateChip(
    state: ConnectionState,
    modifier: Modifier = Modifier,
) {
    val c = WiretapDesign.colors
    val (fg, ring, bg, pulseMs) = when (state) {
        ConnectionState.Open -> Quad(c.status2xx, c.status2xx.copy(alpha = 0.40f), c.status2xx.copy(alpha = 0.12f), 1600)
        ConnectionState.Connecting -> Quad(c.status3xx, c.status3xx.copy(alpha = 0.40f), c.status3xx.copy(alpha = 0.12f), 1000)
        ConnectionState.Closed -> Quad(c.fg3, c.border3, Color.Transparent, 0)
        ConnectionState.Failed -> Quad(c.status5xx, c.status5xx.copy(alpha = 0.40f), c.status5xx.copy(alpha = 0.12f), 0)
    }
    Row(
        modifier = modifier
            .background(bg, WiretapDesign.shapes.pill)
            .border(1.dp, ring, WiretapDesign.shapes.pill)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        val pulseAlpha = if (pulseMs > 0) {
            val transition = rememberInfiniteTransition(label = "state-pulse")
            transition.animateFloat(
                initialValue = 1f,
                targetValue = 0.45f,
                animationSpec = infiniteRepeatable(
                    animation = tween(pulseMs / 2),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "alpha",
            ).value
        } else 1f
        Box(
            Modifier
                .size(6.dp)
                .alpha(pulseAlpha)
                .background(fg, CircleShape),
        )
        Text(
            text = state.name,
            style = WiretapDesign.typography.label.copy(letterSpacing = 0.04.em),
            color = fg,
        )
    }
}

private data class Quad(val a: Color, val b: Color, val c: Color, val d: Int)
