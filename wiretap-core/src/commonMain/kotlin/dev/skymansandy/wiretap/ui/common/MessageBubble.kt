/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.domain.model.SocketContentType
import dev.skymansandy.wiretap.domain.model.SocketMessage
import dev.skymansandy.wiretap.domain.model.SocketMessageType
import dev.skymansandy.wiretap.domain.model.isTextSearchable
import dev.skymansandy.wiretap.helper.util.formatBytes
import dev.skymansandy.wiretap.helper.util.formatTime
import dev.skymansandy.wiretap.helper.util.highlightText

@Composable
internal fun MessageBubble(
    modifier: Modifier = Modifier,
    message: SocketMessage,
    searchQuery: String = "",
    activeMatchRange: IntRange? = null,
) {
    when (message.contentType) {
        SocketContentType.Ping,
        SocketContentType.Pong,
        SocketContentType.Close,
        -> ControlFrameLabel(modifier = modifier, message = message)

        // Only frames that search actually matches may render a highlight,
        // otherwise binary placeholders light up while the counter reads 0 / 0.
        else -> DataFrameBubble(
            modifier = modifier,
            message = message,
            searchQuery = searchQuery.takeIf { message.contentType.isTextSearchable() }.orEmpty(),
            activeMatchRange = activeMatchRange?.takeIf { message.contentType.isTextSearchable() },
        )
    }
}

@Composable
private fun ControlFrameLabel(
    modifier: Modifier = Modifier,
    message: SocketMessage,
) {
    val label = when (message.contentType) {
        SocketContentType.Ping -> "Ping"
        SocketContentType.Pong -> "Pong"
        SocketContentType.Close -> "Close${message.content.takeIf { it.isNotBlank() }?.let { " — $it" } ?: ""}"
        else -> return
    }

    Box(
        modifier = modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "$label · ${formatTime(message.timestamp)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        )
    }
}

@Composable
private fun DataFrameBubble(
    modifier: Modifier = Modifier,
    message: SocketMessage,
    searchQuery: String = "",
    activeMatchRange: IntRange? = null,
) {
    val isSent = message.direction == SocketMessageType.Sent
    val alignment = if (isSent) Alignment.CenterEnd else Alignment.CenterStart

    val bgColor = when {
        isSent -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = when {
        isSent -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier.padding(horizontal = 12.dp, vertical = 3.dp),
        contentAlignment = alignment,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .background(bgColor, RoundedCornerShape(12.dp))
                .padding(10.dp),
        ) {
            SelectionContainer {
                Text(
                    text = highlightText(message.content, searchQuery, activeMatchRange),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = textColor,
                )
            }

            Spacer(Modifier.height(2.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(
                    text = formatTime(message.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor.copy(alpha = 0.6f),
                )

                Text(
                    text = formatBytes(message.byteCount),
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor.copy(alpha = 0.6f),
                )

                CopyIconButton(
                    text = message.content,
                    contentDescription = "Copy message",
                    tint = textColor.copy(alpha = 0.6f),
                    snackbarMessage = "Message copied",
                )
            }
        }
    }
}
