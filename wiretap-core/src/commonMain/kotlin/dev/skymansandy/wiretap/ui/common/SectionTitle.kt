/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
internal fun SectionTitle(
    modifier: Modifier = Modifier,
    text: String,
    action: (@Composable () -> Unit)? = null,
    expanded: Boolean? = null,
    onToggleExpand: (() -> Unit)? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .then(
                if (onToggleExpand != null) Modifier.clickable(onClick = onToggleExpand)
                else Modifier,
            )
            .padding(start = 16.dp, end = 4.dp, top = 16.dp, bottom = 4.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )

        action?.invoke()

        if (expanded != null) {
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview
@Composable
private fun Preview_SectionTitle() {
    MaterialTheme {
        SectionTitle(
            modifier = Modifier.fillMaxWidth(),
            text = "Request Headers",
        )
    }
}

@Preview
@Composable
private fun Preview_SectionTitleWithAction() {
    MaterialTheme {
        SectionTitle(
            modifier = Modifier.fillMaxWidth(),
            text = "Response Body",
            action = {
                Text(
                    text = "Copy",
                    style = MaterialTheme.typography.labelSmall,
                )
            },
        )
    }
}
