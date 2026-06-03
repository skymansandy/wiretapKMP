/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.design.component.badge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.design.foundation.color
import dev.skymansandy.wiretap.design.foundation.statusClassOf
import dev.skymansandy.wiretap.design.theme.WiretapDesign

@Composable
fun StatusBadge(
    status: Int,
    reason: String? = null,
    modifier: Modifier = Modifier,
) {
    val color = statusClassOf(status).color()
    val label = if (reason.isNullOrBlank()) "$status" else "$status $reason"
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.14f), WiretapDesign.shapes.xs)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(text = label, style = WiretapDesign.typography.monoMeta, color = color)
    }
}
