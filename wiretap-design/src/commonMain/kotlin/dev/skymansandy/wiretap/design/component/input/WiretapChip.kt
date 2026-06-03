/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.design.component.input

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.design.component.icon.WiretapIcons
import dev.skymansandy.wiretap.design.theme.WiretapDesign

@Composable
fun WiretapChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onRemove: (() -> Unit)? = null,
) {
    val c = WiretapDesign.colors
    val shape = WiretapDesign.shapes.pill
    val (bg, border, fg) = if (selected) {
        Triple(c.accentSoft, c.accentLine, c.accent)
    } else {
        Triple(c.surface1, c.border2, c.fg2)
    }
    Row(
        modifier = modifier
            .background(bg, shape)
            .border(1.dp, border, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(text = label, style = WiretapDesign.typography.monoMeta.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold), color = fg)
        if (onRemove != null) {
            Icon(
                imageVector = WiretapIcons.Close,
                contentDescription = "Remove $label",
                tint = fg,
                modifier = Modifier
                    .size(10.dp)
                    .clickable(onClick = onRemove),
            )
        }
    }
}
