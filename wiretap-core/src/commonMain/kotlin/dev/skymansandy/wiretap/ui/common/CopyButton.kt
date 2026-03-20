package dev.skymansandy.wiretap.ui.common

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
import dev.skymansandy.wiretap.helper.util.copyToClipboard
import dev.skymansandy.wiretap.resources.Res
import dev.skymansandy.wiretap.resources.copy
import dev.skymansandy.wiretap.resources.copy_body
import dev.skymansandy.wiretap.resources.copy_headers
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CopyBodyButton(
    modifier: Modifier = Modifier,
    body: String,
) {
    TextButton(
        modifier = modifier,
        onClick = {
            copyToClipboard(body)
        },
    ) {
        Icon(
            imageVector = Icons.Filled.ContentCopy,
            contentDescription = stringResource(Res.string.copy_body),
            modifier = Modifier.size(14.dp),
        )

        Spacer(
            modifier = Modifier.width(4.dp),
        )

        Text(
            text = stringResource(Res.string.copy),
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
internal fun CopyHeadersButton(
    modifier: Modifier = Modifier,
    headers: Map<String, String>,
) {
    TextButton(
        modifier = modifier,
        onClick = {
            copyToClipboard(
                headers.entries.joinToString("\n") { "${it.key}: ${it.value}" },
            )
        },
    ) {
        Icon(
            imageVector = Icons.Filled.ContentCopy,
            contentDescription = stringResource(Res.string.copy_headers),
            modifier = Modifier.size(14.dp),
        )

        Spacer(Modifier.width(4.dp))

        Text(stringResource(Res.string.copy), style = MaterialTheme.typography.labelSmall)
    }
}
