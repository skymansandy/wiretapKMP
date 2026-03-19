package dev.skymansandy.wiretap.ui.socket

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.data.db.entity.SocketLogEntry
import dev.skymansandy.wiretap.domain.model.SocketStatus
import dev.skymansandy.wiretap.ui.components.highlightText
import dev.skymansandy.wiretap.util.formatTime
import dev.skymansandy.wiretap.resources.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
internal fun SocketLogList(
    socketLogs: List<SocketLogEntry>,
    searchQuery: String,
    onSocketClick: (SocketLogEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (socketLogs.isEmpty()) {
        Box(modifier, contentAlignment = Alignment.Center) {
            Text(stringResource(Res.string.no_websocket_connections), style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        val listState = rememberLazyListState()
        val scope = rememberCoroutineScope()
        val isAtTop = remember(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
            listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
        }
        var lastItemCount by remember { mutableIntStateOf(socketLogs.size) }

        LaunchedEffect(socketLogs.size) {
            if (socketLogs.size > lastItemCount && isAtTop) {
                scope.launch { listState.scrollToItem(0) }
            }
            lastItemCount = socketLogs.size
        }

        LazyColumn(state = listState, modifier = modifier) {
            items(
                count = socketLogs.size,
                key = { index -> socketLogs[index].id },
            ) { index ->
                SocketLogItemContent(
                    entry = socketLogs[index],
                    searchQuery = searchQuery,
                    onClick = { onSocketClick(socketLogs[index]) },
                )
            }
        }
    }
}

@Composable
private fun SocketLogItemContent(
    entry: SocketLogEntry,
    searchQuery: String,
    onClick: () -> Unit,
) {
    val statusColor = when (entry.status) {
        SocketStatus.CONNECTING -> Color(0xFF42A5F5) // Blue
        SocketStatus.OPEN -> Color(0xFF4CAF50) // Green
        SocketStatus.CLOSING -> Color(0xFFFFA726) // Amber
        SocketStatus.CLOSED -> Color(0xFF9E9E9E) // Gray
        SocketStatus.FAILED -> Color(0xFFEF5350) // Red
    }

    val isSecure = entry.url.startsWith("wss://", ignoreCase = true)
    val withoutScheme = entry.url.substringAfter("://")
    val host = withoutScheme.substringBefore("/").substringBefore("?")
    val path = withoutScheme.removePrefix(host).ifEmpty { "/" }

    Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = stringResource(Res.string.ws_prefix),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = statusColor,
                modifier = Modifier.width(44.dp),
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = highlightText(path, searchQuery),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (isSecure) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = Color(0xFF26C6DA),
                        )
                    }
                    Text(
                        text = highlightText(host, searchQuery),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF26C6DA),
                    )
                }

                Spacer(Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = formatTime(entry.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (entry.messageCount > 0) {
                        Text(
                            text = stringResource(Res.string.msgs_count, entry.messageCount),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    SocketStatusChip(entry.status)
                }
            }
        }
        HorizontalDivider()
    }
}

@Composable
private fun SocketStatusChip(status: SocketStatus) {
    val bgColor = when (status) {
        SocketStatus.CONNECTING -> Color(0xFF42A5F5)
        SocketStatus.OPEN -> Color(0xFF4CAF50)
        SocketStatus.CLOSING -> Color(0xFFFFA726)
        SocketStatus.CLOSED -> Color(0xFF9E9E9E)
        SocketStatus.FAILED -> Color(0xFFEF5350)
    }
    val label = when (status) {
        SocketStatus.CONNECTING -> stringResource(Res.string.status_connecting)
        SocketStatus.OPEN -> stringResource(Res.string.status_open)
        SocketStatus.CLOSING -> stringResource(Res.string.status_closing)
        SocketStatus.CLOSED -> stringResource(Res.string.status_closed)
        SocketStatus.FAILED -> stringResource(Res.string.status_failed)
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = Color.White,
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 5.dp, vertical = 1.dp),
    )
}

@Preview
@Composable
private fun SocketLogItemOpenPreview() {
    MaterialTheme {
        SocketLogItemContent(
            entry = SocketLogEntry(
                id = 1,
                url = "wss://echo.websocket.org/chat",
                status = SocketStatus.OPEN,
                messageCount = 12,
                timestamp = 1710850000000,
            ),
            searchQuery = "",
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun SocketLogItemClosedPreview() {
    MaterialTheme {
        SocketLogItemContent(
            entry = SocketLogEntry(
                id = 2,
                url = "ws://localhost:8080/ws",
                status = SocketStatus.CLOSED,
                messageCount = 5,
                timestamp = 1710850000000,
                closedAt = 1710850060000,
                closeCode = 1000,
            ),
            searchQuery = "",
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun SocketLogItemFailedPreview() {
    MaterialTheme {
        SocketLogItemContent(
            entry = SocketLogEntry(
                id = 3,
                url = "wss://api.example.com/stream",
                status = SocketStatus.FAILED,
                messageCount = 0,
                timestamp = 1710850000000,
                failureMessage = "Connection refused",
            ),
            searchQuery = "",
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun SocketLogItemConnectingPreview() {
    MaterialTheme {
        SocketLogItemContent(
            entry = SocketLogEntry(
                id = 4,
                url = "wss://api.example.com/realtime",
                status = SocketStatus.CONNECTING,
                timestamp = 1710850000000,
            ),
            searchQuery = "",
            onClick = {},
        )
    }
}
