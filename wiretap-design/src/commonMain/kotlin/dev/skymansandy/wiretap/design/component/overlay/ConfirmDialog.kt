/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.design.component.overlay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.design.component.button.WiretapButton
import dev.skymansandy.wiretap.design.component.button.WiretapButtonStyle
import dev.skymansandy.wiretap.design.theme.WiretapDesign

@Composable
fun ConfirmDialog(
    open: Boolean,
    title: String,
    description: String,
    confirmLabel: String,
    cancelLabel: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    danger: Boolean = false,
) {
    val c = WiretapDesign.colors
    AnimatedVisibility(visible = open, enter = fadeIn(tween(120)), exit = fadeOut(tween(100))) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(onClick = onCancel)
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 320.dp)
                    .shadow(WiretapDesign.elevation.dialog, WiretapDesign.shapes.lg)
                    .background(c.surface2, WiretapDesign.shapes.lg)
                    .border(1.dp, c.border2, WiretapDesign.shapes.lg)
                    .clickable(enabled = false, onClick = {})
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(text = title, style = WiretapDesign.typography.title, color = c.fg1)
                Text(text = description, style = WiretapDesign.typography.body, color = c.fg2)
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                ) {
                    WiretapButton(text = cancelLabel, style = WiretapButtonStyle.Ghost, onClick = onCancel)
                    WiretapButton(
                        text = confirmLabel,
                        style = if (danger) WiretapButtonStyle.Danger else WiretapButtonStyle.Primary,
                        onClick = onConfirm,
                    )
                }
            }
        }
    }
}
