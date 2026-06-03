/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.design.component.nav

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import dev.skymansandy.wiretap.design.theme.WiretapDesign

/**
 * Underline tab row used at top-level (Logs/Rules) and inside the detail pane
 * (Overview/Request/Response). Variant [DetailTabs] uppercases its labels.
 */
@Composable
fun SubTabs(
    items: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    uppercase: Boolean = false,
) {
    val c = WiretapDesign.colors
    Column(modifier = modifier.fillMaxWidth().background(c.surface0)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items.forEachIndexed { index, label ->
                val isActive = index == selected
                Column(
                    Modifier
                        .clickable { onSelect(index) }
                        .padding(horizontal = if (uppercase) 14.dp else 16.dp, vertical = 12.dp),
                ) {
                    Text(
                        text = if (uppercase) label.uppercase() else label,
                        style = WiretapDesign.typography.label.copy(
                            letterSpacing = if (uppercase) 0.06.em else 0.04.em,
                        ),
                        color = if (isActive) c.fg1 else c.fg3,
                    )
                    Box(
                        Modifier
                            .padding(top = 10.dp)
                            .height(2.dp)
                            .background(if (isActive) c.accent else Color.Transparent)
                            .fillMaxWidth(),
                    )
                }
            }
        }
        HorizontalDivider(color = c.border1, thickness = 1.dp)
    }
}

@Composable
fun DetailTabs(
    items: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) = SubTabs(items, selected, onSelect, modifier, uppercase = true)
