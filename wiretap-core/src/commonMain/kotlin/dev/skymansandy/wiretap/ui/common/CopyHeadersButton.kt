/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.ui.common

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.helper.util.copyToClipboard
import kotlinx.coroutines.launch

@Composable
internal fun CopyHeadersButton(
    modifier: Modifier = Modifier,
    headers: Map<String, String>,
    snackbarMessage: String = "Headers copied to clipboard",
    snackbarHostState: SnackbarHostState? = LocalSnackbarHostState.current,
) {
    val scope = rememberCoroutineScope()
    TextButton(
        modifier = modifier,
        onClick = {
            copyToClipboard(
                headers.entries.joinToString("\n") {
                    "${it.key}: ${it.value}"
                },
            )
            snackbarHostState?.let { host ->
                scope.launch { host.showSnackbar(snackbarMessage) }
            }
        },
    ) {
        Icon(
            modifier = Modifier.size(14.dp),
            imageVector = Icons.Filled.ContentCopy,
            contentDescription = "Copy headers",
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = "Copy",
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Preview
@Composable
private fun Preview_CopyHeadersButton() {
    MaterialTheme {
        CopyHeadersButton(
            headers = mapOf(
                "Content-Type" to "application/json",
                "Authorization" to "Bearer token",
            ),
        )
    }
}
