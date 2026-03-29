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
internal fun CopyBodyButton(
    modifier: Modifier = Modifier,
    body: String,
    snackbarMessage: String = "Body copied to clipboard",
    snackbarHostState: SnackbarHostState? = LocalSnackbarHostState.current,
) {
    val scope = rememberCoroutineScope()
    TextButton(
        modifier = modifier,
        onClick = {
            copyToClipboard(body)
            snackbarHostState?.let { host ->
                scope.launch { host.showSnackbar(snackbarMessage) }
            }
        },
    ) {
        Icon(
            imageVector = Icons.Filled.ContentCopy,
            contentDescription = "Copy body",
            modifier = Modifier.size(14.dp),
        )

        Spacer(
            modifier = Modifier.width(4.dp),
        )

        Text(
            text = "Copy",
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Preview
@Composable
private fun Preview_CopyBodyButton() {
    MaterialTheme {
        CopyBodyButton(
            body = """{"name":"John"}""",
        )
    }
}
