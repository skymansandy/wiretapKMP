/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.ui.common

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.helper.util.copyToClipboard
import kotlinx.coroutines.launch

/**
 * Compact icon-only counterpart to [CopyButton], sized to sit inline with the
 * timestamp/byte-count footer of a message bubble.
 */
@Composable
internal fun CopyIconButton(
    modifier: Modifier = Modifier,
    text: String,
    contentDescription: String = "Copy",
    tint: Color = LocalContentColor.current.copy(alpha = 0.6f),
    snackbarMessage: String = "Copied to clipboard",
    snackbarHostState: SnackbarHostState? = LocalSnackbarHostState.current,
) {
    val scope = rememberCoroutineScope()
    IconButton(
        modifier = modifier.size(20.dp),
        onClick = {
            copyToClipboard(text)
            snackbarHostState?.let { host ->
                scope.launch { host.showSnackbar(snackbarMessage) }
            }
        },
    ) {
        Icon(
            imageVector = Icons.Filled.ContentCopy,
            contentDescription = contentDescription,
            modifier = Modifier.size(12.dp),
            tint = tint,
        )
    }
}

@Preview
@Composable
private fun Preview_CopyIconButton() {
    MaterialTheme {
        CopyIconButton(
            text = """{"name":"John"}""",
        )
    }
}
