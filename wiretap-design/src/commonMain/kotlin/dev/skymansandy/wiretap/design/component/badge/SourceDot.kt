/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.design.component.badge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.design.foundation.LogSource
import dev.skymansandy.wiretap.design.foundation.color

@Composable
fun SourceDot(source: LogSource, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(7.dp)
            .background(source.color(), CircleShape),
    )
}
