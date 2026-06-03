/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.design.component.overlay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.design.component.button.WiretapIconButton
import dev.skymansandy.wiretap.design.component.icon.WiretapIcons
import dev.skymansandy.wiretap.design.theme.WiretapDesign

@Composable
fun WiretapBottomSheet(
    open: Boolean,
    onDismiss: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    footer: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val c = WiretapDesign.colors
    AnimatedVisibility(
        visible = open,
        enter = fadeIn(tween(150)),
        exit = fadeOut(tween(120)),
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter,
        ) {
            AnimatedVisibility(
                visible = open,
                enter = slideInVertically(tween(180), initialOffsetY = { it }),
                exit = slideOutVertically(tween(150), targetOffsetY = { it }),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.8f)
                        .background(c.surface1, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .border(1.dp, c.border2, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .clickable(enabled = false, onClick = {}),
                ) {
                    Box(
                        Modifier
                            .padding(top = 10.dp)
                            .size(width = 36.dp, height = 4.dp)
                            .background(c.border3, RoundedCornerShape(2.dp))
                            .align(Alignment.CenterHorizontally),
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = title,
                            style = WiretapDesign.typography.title,
                            color = c.fg1,
                            modifier = Modifier.weight(1f),
                        )
                        WiretapIconButton(onClick = onDismiss) {
                            Icon(WiretapIcons.Close, contentDescription = "Close", tint = c.fg2)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                    ) { content() }
                    if (footer != null) {
                        HorizontalDivider(color = c.border1, thickness = 1.dp)
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) { footer() }
                    }
                }
            }
        }
    }
}
