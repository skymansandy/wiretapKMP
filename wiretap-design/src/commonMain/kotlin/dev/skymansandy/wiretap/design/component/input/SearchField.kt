/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.design.component.input

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.design.component.button.WiretapIconButton
import dev.skymansandy.wiretap.design.component.icon.WiretapIcons
import dev.skymansandy.wiretap.design.theme.WiretapDesign

@Composable
fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search…",
) {
    val c = WiretapDesign.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(c.surface2, RoundedCornerShape(8.dp))
            .border(1.dp, c.border2, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(WiretapIcons.Search, contentDescription = null, tint = c.fg3, modifier = Modifier.size(16.dp))
        Box(modifier = Modifier.weight(1f)) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = WiretapDesign.typography.body.copy(color = c.fg1),
                cursorBrush = SolidColor(c.accent),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { inner ->
                    if (value.isEmpty()) {
                        Text(text = placeholder, style = WiretapDesign.typography.body, color = c.fg3)
                    }
                    inner()
                },
            )
        }
        if (value.isNotEmpty()) {
            WiretapIconButton(onClick = { onValueChange("") }, modifier = Modifier.size(24.dp)) {
                Icon(WiretapIcons.Close, contentDescription = "Clear", tint = c.fg2, modifier = Modifier.size(12.dp))
            }
        }
    }
}
