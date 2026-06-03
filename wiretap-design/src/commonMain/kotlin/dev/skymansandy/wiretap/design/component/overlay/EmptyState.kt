/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.design.component.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.design.component.icon.WiretapIcons
import dev.skymansandy.wiretap.design.theme.WiretapDesign

@Composable
fun EmptyState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = WiretapIcons.EmptyNet,
    setupCommand: String? = null,
) {
    val c = WiretapDesign.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(c.surface1, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = c.fg3, modifier = Modifier.size(28.dp))
        }
        Text(text = title, style = WiretapDesign.typography.title, color = c.fg1, textAlign = TextAlign.Center)
        Text(
            text = description,
            style = WiretapDesign.typography.body,
            color = c.fg3,
            textAlign = TextAlign.Center,
        )
        if (setupCommand != null) {
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .background(c.surface1, WiretapDesign.shapes.md)
                    .border(1.dp, c.border2, WiretapDesign.shapes.md)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                Text(
                    text = setupCommand,
                    style = WiretapDesign.typography.monoMeta,
                    color = c.fg2,
                )
            }
        }
    }
}
