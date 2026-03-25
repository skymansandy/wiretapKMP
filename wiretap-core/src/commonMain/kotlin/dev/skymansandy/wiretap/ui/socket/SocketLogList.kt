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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.data.db.entity.SocketEntry
import dev.skymansandy.wiretap.domain.model.SocketStatus
import dev.skymansandy.wiretap.helper.util.formatTime
import dev.skymansandy.wiretap.ui.common.highlightText
import dev.skymansandy.wiretap.ui.theme.WiretapColors
import kotlinx.coroutines.launch

@Composable
internal fun SocketLogList(
    modifier: Modifier = Modifier,
    socketLogs: List<SocketEntry>,
    searchQuery: String,
    onSocketClick: (SocketEntry) -> Unit,
) {
    if (socketLogs.isEmpty()) {
        Box(modifier, contentAlignment = Alignment.Center) {
            Text("No WebSocket connections yet", style = MaterialTheme.typography.bodyLarge)
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
    entry: SocketEntry,
    searchQuery: String,
    onClick: () -> Unit,
) {
    val statusColor = when (entry.status) {
        SocketStatus.Connecting -> WiretapColors.StatusBlue
        SocketStatus.Open -> WiretapColors.StatusGreen
        SocketStatus.Closing -> WiretapColors.StatusAmber
        SocketStatus.Closed -> WiretapColors.StatusGray
        SocketStatus.Failed -> WiretapColors.StatusRed
    }

    val isSecure = entry.url.startsWith("wss://", ignoreCase = true)
    val withoutScheme = entry.url.substringAfter("://")
    val host = withoutScheme.substringBefore("/").substringBefore("?")
    val path = withoutScheme.removePrefix(host).ifEmpty { "/" }

    Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = "WS",
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
                            tint = WiretapColors.SecureHost,
                        )
                    }
                    Text(
                        text = highlightText(host, searchQuery),
                        style = MaterialTheme.typography.bodySmall,
                        color = WiretapColors.SecureHost,
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
                            text = "${entry.messageCount} msgs",
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
        SocketStatus.Connecting -> WiretapColors.StatusBlue
        SocketStatus.Open -> WiretapColors.StatusGreen
        SocketStatus.Closing -> WiretapColors.StatusAmber
        SocketStatus.Closed -> WiretapColors.StatusGray
        SocketStatus.Failed -> WiretapColors.StatusRed
    }
    val label = when (status) {
        SocketStatus.Connecting -> "Connecting"
        SocketStatus.Open -> "Open"
        SocketStatus.Closing -> "Closing"
        SocketStatus.Closed -> "Closed"
        SocketStatus.Failed -> "Failed"
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
private fun Preview_SocketLogItemOpen() {
    MaterialTheme {
        SocketLogItemContent(
            entry = SocketEntry(
                id = 1,
                url = "wss://echo.websocket.org/chat",
                status = SocketStatus.Open,
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
private fun Preview_SocketLogItemClosed() {
    MaterialTheme {
        SocketLogItemContent(
            entry = SocketEntry(
                id = 2,
                url = "ws://localhost:8080/ws",
                status = SocketStatus.Closed,
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
private fun Preview_SocketLogItemFailed() {
    MaterialTheme {
        SocketLogItemContent(
            entry = SocketEntry(
                id = 3,
                url = "wss://api.example.com/stream",
                status = SocketStatus.Failed,
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
private fun Preview_SocketLogItemConnecting() {
    MaterialTheme {
        SocketLogItemContent(
            entry = SocketEntry(
                id = 4,
                url = "wss://api.example.com/realtime",
                status = SocketStatus.Connecting,
                timestamp = 1710850000000,
            ),
            searchQuery = "",
            onClick = {},
        )
    }
}
