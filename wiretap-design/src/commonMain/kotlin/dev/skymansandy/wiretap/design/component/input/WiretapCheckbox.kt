/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.design.component.input

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.design.theme.WiretapDesign

@Composable
fun WiretapCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String? = null,
    modifier: Modifier = Modifier,
    indeterminate: Boolean = false,
    enabled: Boolean = true,
) {
    val c = WiretapDesign.colors
    val shape = RoundedCornerShape(4.dp)
    val bg = when {
        indeterminate -> c.accentSoft
        checked -> c.accent
        else -> c.surface1
    }
    val borderColor = if (indeterminate || checked) c.accent else c.border3
    val checkColor = if (indeterminate) c.accent else c.accentForeground
    Row(
        modifier = modifier.clickable(enabled = enabled) { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            Modifier
                .size(18.dp)
                .background(bg, shape)
                .border(1.5.dp, borderColor, shape),
            contentAlignment = Alignment.Center,
        ) {
            when {
                indeterminate -> Icon(Icons.Filled.Remove, null, tint = checkColor, modifier = Modifier.size(12.dp))
                checked -> Icon(Icons.Filled.Check, null, tint = checkColor, modifier = Modifier.size(12.dp))
                else -> Box(Modifier.size(0.dp).background(Color.Transparent))
            }
        }
        if (label != null) Text(text = label, style = WiretapDesign.typography.body, color = c.fg1)
    }
}
