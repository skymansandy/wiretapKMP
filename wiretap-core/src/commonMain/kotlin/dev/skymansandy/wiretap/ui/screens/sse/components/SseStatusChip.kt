/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.ui.screens.sse.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.domain.model.SseStatus

@Composable
internal fun SseStatusChip(
    modifier: Modifier = Modifier,
    status: SseStatus,
) {
    Text(
        text = status.label,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = modifier
            .padding(end = 12.dp)
            .background(status.bgColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}
