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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.design.theme.WiretapDesign

@Composable
fun NavRail(
    items: List<BottomNavItem>,
    selectedKey: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    brand: String = "WIRETAP",
    statusText: String? = null,
) {
    val c = WiretapDesign.colors
    Column(
        modifier = modifier
            .width(200.dp)
            .fillMaxHeight()
            .background(c.surface0)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.padding(start = 10.dp, end = 10.dp, top = 8.dp, bottom = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                Modifier
                    .size(24.dp)
                    .background(c.accent, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier
                        .size(10.dp)
                        .background(c.background, RoundedCornerShape(2.dp)),
                )
            }
            Text(text = brand, style = WiretapDesign.typography.wordmark, color = c.fg1)
        }

        items.forEach { item ->
            val isActive = item.key == selectedKey
            Row(
                modifier = Modifier
                    .padding(horizontal = 0.dp)
                    .background(if (isActive) c.accentSoft else Color.Transparent, RoundedCornerShape(10.dp))
                    .clickable { onSelect(item.key) }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(item.icon, contentDescription = null, tint = if (isActive) c.accent else c.fg2, modifier = Modifier.size(16.dp))
                Text(
                    text = item.label,
                    style = WiretapDesign.typography.body,
                    color = if (isActive) c.accent else c.fg2,
                )
            }
        }

        Spacer(Modifier.weight(1f))

        if (statusText != null) {
            HorizontalDivider(color = c.border1, thickness = 1.dp)
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(Modifier.size(6.dp).background(c.mock, CircleShape))
                Text(text = statusText, style = WiretapDesign.typography.monoMeta, color = c.fg4)
            }
        }
    }
}
