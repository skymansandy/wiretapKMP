/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.design.component.button

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.design.component.icon.WiretapIcons
import dev.skymansandy.wiretap.design.theme.WiretapDesign

@Composable
fun WiretapFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = WiretapIcons.Plus,
    contentDescription: String? = null,
) {
    val c = WiretapDesign.colors
    Box(
        modifier = modifier
            .size(52.dp)
            .shadow(WiretapDesign.elevation.fab, CircleShape)
            .clip(CircleShape)
            .background(c.accent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = c.accentForeground,
        )
    }
}
