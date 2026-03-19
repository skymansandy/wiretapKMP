package dev.skymansandy.wiretap.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.util.copyToClipboard

@Composable
internal fun CopyBodyButton(body: String) {
    TextButton(onClick = { copyToClipboard(body) }) {
        Icon(
            imageVector = Icons.Filled.ContentCopy,
            contentDescription = "Copy body",
            modifier = Modifier.size(14.dp),
        )
        Spacer(Modifier.width(4.dp))
        Text("Copy", style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
internal fun CopyHeadersButton(headers: Map<String, String>) {
    TextButton(
        onClick = {
            copyToClipboard(
                headers.entries.joinToString("\n") { "${it.key}: ${it.value}" },
            )
        },
    ) {
        Icon(
            imageVector = Icons.Filled.ContentCopy,
            contentDescription = "Copy headers",
            modifier = Modifier.size(14.dp),
        )
        Spacer(Modifier.width(4.dp))
        Text("Copy", style = MaterialTheme.typography.labelSmall)
    }
}
