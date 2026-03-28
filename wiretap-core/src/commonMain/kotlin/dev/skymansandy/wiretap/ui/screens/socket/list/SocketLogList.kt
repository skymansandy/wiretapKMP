package dev.skymansandy.wiretap.ui.screens.socket.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.skymansandy.wiretap.domain.model.SocketConnection
import dev.skymansandy.wiretap.domain.model.SocketStatus
import dev.skymansandy.wiretap.helper.util.formatTime
import dev.skymansandy.wiretap.helper.util.highlightText
import dev.skymansandy.wiretap.ui.common.ScrollToTopButton
import dev.skymansandy.wiretap.ui.screens.socket.components.StatusChip
import dev.skymansandy.wiretap.ui.theme.WiretapColors
import kotlinx.coroutines.launch

@Composable
internal fun SocketLogList(
    modifier: Modifier = Modifier,
    viewModel: SocketLogListViewModel,
    searchQuery: String,
    onDismissSearch: () -> Unit,
    onSocketClick: (SocketConnection) -> Unit,
) {
    val socketLogs by viewModel.socketLogs.collectAsStateWithLifecycle()

    if (socketLogs.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "No WebSocket connections yet",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    } else {
        val listState = rememberLazyListState()
        val scope = rememberCoroutineScope()
        var lastItemCount by remember { mutableIntStateOf(socketLogs.size) }
        val isAtTop by remember {
            derivedStateOf {
                listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
            }
        }

        LaunchedEffect(socketLogs.size) {
            if (socketLogs.size > lastItemCount && isAtTop) {
                scope.launch { listState.scrollToItem(0) }
            }
            lastItemCount = socketLogs.size
        }

        ScrollToTopButton(
            listState = listState,
            modifier = modifier,
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
            ) {
                items(
                    count = socketLogs.size,
                    key = { index -> socketLogs[index].id },
                ) { index ->
                    SocketLogItemContent(
                        socket = socketLogs[index],
                        searchQuery = searchQuery,
                        onClick = {
                            onDismissSearch()
                            onSocketClick(socketLogs[index])
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun SocketLogItemContent(
    socket: SocketConnection,
    searchQuery: String,
    onClick: () -> Unit,
) {
    val isSecure = socket.url.startsWith("wss://", ignoreCase = true)
    val withoutScheme = socket.url.substringAfter("://")
    val host = withoutScheme.substringBefore("/").substringBefore("?")
    val path = withoutScheme.removePrefix(host).ifEmpty { "/" }

    Column(
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                modifier = Modifier.width(44.dp),
                text = "WS",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = socket.statusColor,
            )

            Column(
                modifier = Modifier.weight(1f),
            ) {
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
                        text = formatTime(socket.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (socket.messageCount > 0) {
                        Text(
                            text = "${socket.messageCount} msgs",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    StatusChip(status = socket.status)
                }
            }
        }

        HorizontalDivider()
    }
}

@Preview
@Composable
private fun Preview_SocketLogItemOpen() {
    MaterialTheme {
        SocketLogItemContent(
            socket = SocketConnection(
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
            socket = SocketConnection(
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
            socket = SocketConnection(
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
            socket = SocketConnection(
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
