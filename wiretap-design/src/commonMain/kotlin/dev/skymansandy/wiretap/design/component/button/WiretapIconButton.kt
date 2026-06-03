/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.design.component.button

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.design.theme.WiretapDesign

/**
 * 32dp tap target with an optional small notification badge in the top-right
 * corner, drawn in the active accent. Matches `.icon-btn` + `.ico-badge`.
 */
@Composable
fun WiretapIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeCount: Int? = null,
    content: @Composable () -> Unit,
) {
    val c = WiretapDesign.colors
    Box(modifier = modifier.size(32.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            CompositionLocalProvider(LocalContentColor provides c.fg2) { content() }
        }
        if (badgeCount != null && badgeCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-4).dp, y = 4.dp)
                    .widthIn(min = 14.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(c.accent)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = badgeCount.toString(),
                    style = WiretapDesign.typography.micro,
                    color = c.accentForeground,
                )
            }
        }
    }
}
