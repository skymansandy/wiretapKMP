/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.design.component.input

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.design.theme.WiretapDesign

@Composable
fun WiretapTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    mono: Boolean = false,
    minLines: Int = 1,
    enabled: Boolean = true,
) {
    val c = WiretapDesign.colors
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (label != null) {
            Text(
                text = label.uppercase(),
                style = WiretapDesign.typography.label,
                color = c.fg3,
            )
        }
        val border = if (focused) c.borderFocus else c.border2
        val bg = if (focused) c.surface2 else c.surface1
        val style = if (mono) WiretapDesign.typography.code else WiretapDesign.typography.body
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = if (minLines > 1) 80.dp else 0.dp)
                .background(bg, WiretapDesign.shapes.md)
                .border(1.dp, border, WiretapDesign.shapes.md)
                .padding(horizontal = 12.dp, vertical = 9.dp),
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                interactionSource = interaction,
                textStyle = style.copy(color = c.fg1, textDecoration = TextDecoration.None),
                cursorBrush = SolidColor(c.accent),
                minLines = minLines,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = style.lineHeight.value.dp),
                decorationBox = { inner ->
                    if (value.isEmpty() && placeholder != null) {
                        Text(text = placeholder, style = style, color = c.fg3)
                    }
                    inner()
                },
            )
        }
    }
}
