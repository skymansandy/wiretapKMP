/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.design.component.badge

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.design.foundation.HttpMethod
import dev.skymansandy.wiretap.design.foundation.color
import dev.skymansandy.wiretap.design.foundation.label
import dev.skymansandy.wiretap.design.theme.WiretapDesign

/**
 * Tiny uppercase mono badge tagged with an HTTP method's semantic color.
 * Fill = method color at 18% alpha, border = method color at 30% alpha,
 * matching the `.method-badge` rule in styles.css.
 */
@Composable
fun MethodBadge(
    method: HttpMethod,
    modifier: Modifier = Modifier,
) {
    val color = method.color()
    val shape = WiretapDesign.shapes.xs
    Box(
        modifier = modifier
            .widthIn(min = 44.dp)
            .background(color.copy(alpha = 0.18f), shape)
            .border(1.dp, color.copy(alpha = 0.30f), shape)
            .padding(horizontal = 6.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = method.label(),
            style = WiretapDesign.typography.micro,
            color = color,
            textAlign = TextAlign.Center,
        )
    }
}
