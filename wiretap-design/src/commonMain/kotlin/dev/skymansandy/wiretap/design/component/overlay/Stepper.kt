/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.design.component.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.design.theme.WiretapDesign

@Composable
fun Stepper(
    labels: List<String>,
    current: Int,
    modifier: Modifier = Modifier,
) {
    val c = WiretapDesign.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(c.surface0)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        labels.forEachIndexed { index, label ->
            val isActive = index == current
            val isDone = index < current
            val (numBg, numFg, numBorder) = when {
                isActive -> Triple(c.accent, c.accentForeground, Color.Transparent)
                isDone -> Triple(c.accentSoft, c.accent, c.accentLine)
                else -> Triple(c.surface3, c.fg3, c.border2)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    Modifier
                        .size(20.dp)
                        .background(numBg, CircleShape)
                        .border(1.dp, numBorder, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (isDone) "✓" else (index + 1).toString(),
                        style = WiretapDesign.typography.monoMeta,
                        color = numFg,
                    )
                }
                Text(
                    text = label,
                    style = WiretapDesign.typography.label,
                    color = if (isActive) c.fg1 else if (isDone) c.fg2 else c.fg3,
                )
            }
            if (index < labels.size - 1) {
                Box(
                    Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(c.border2),
                )
            }
        }
    }
}
