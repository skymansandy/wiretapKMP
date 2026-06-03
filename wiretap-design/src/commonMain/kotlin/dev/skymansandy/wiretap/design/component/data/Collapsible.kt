/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.design.component.data

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.design.component.icon.WiretapIcons
import dev.skymansandy.wiretap.design.theme.WiretapDesign

@Composable
fun Collapsible(
    title: String,
    modifier: Modifier = Modifier,
    count: Int? = null,
    initiallyOpen: Boolean = true,
    content: @Composable () -> Unit,
) {
    var open by remember { mutableStateOf(initiallyOpen) }
    val angle by animateFloatAsState(if (open) 90f else 0f, label = "chev")
    val c = WiretapDesign.colors
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(c.surface0)
                .clickable { open = !open }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = WiretapIcons.Chevron,
                contentDescription = null,
                tint = c.fg3,
                modifier = Modifier.rotate(angle),
            )
            Text(
                text = title,
                style = WiretapDesign.typography.title,
                color = c.fg1,
                modifier = Modifier.weight(1f),
            )
            if (count != null) {
                Text(
                    text = count.toString(),
                    style = WiretapDesign.typography.monoMeta,
                    color = c.fg3,
                )
            }
        }
        if (open) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) { content() }
        }
        HorizontalDivider(color = c.border1, thickness = 1.dp)
    }
}
