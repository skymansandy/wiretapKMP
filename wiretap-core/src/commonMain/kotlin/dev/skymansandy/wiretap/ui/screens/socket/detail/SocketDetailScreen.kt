package dev.skymansandy.wiretap.ui.screens.socket.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.skymansandy.wiretap.domain.model.SocketConnection
import dev.skymansandy.wiretap.domain.model.SocketContentType
import dev.skymansandy.wiretap.domain.model.SocketMessage
import dev.skymansandy.wiretap.domain.model.SocketMessageType
import dev.skymansandy.wiretap.domain.model.SocketStatus
import dev.skymansandy.wiretap.helper.util.formatTime
import dev.skymansandy.wiretap.helper.util.formatUrlDisplay
import dev.skymansandy.wiretap.navigation.compose.LocalWiretapNavigator
import dev.skymansandy.wiretap.ui.common.InfoLabel
import dev.skymansandy.wiretap.ui.common.MessageBubble
import dev.skymansandy.wiretap.ui.screens.socket.components.StatusChip
import dev.skymansandy.wiretap.ui.theme.WiretapColors
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SocketDetailScreenView(
    modifier: Modifier = Modifier,
    socketId: Long,
    viewModel: SocketDetailViewModel = koinViewModel { parametersOf(socketId) },
) {
    val navigator = LocalWiretapNavigator.current
    val initialEntry by viewModel.initialEntry.collectAsStateWithLifecycle()
    val liveEntry by viewModel.liveEntry.collectAsStateWithLifecycle()
    val entry = liveEntry ?: initialEntry

    if (entry == null) {
        return
    }

    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // Auto-scroll to bottom when new messages arrive and already near bottom
    var prevMessageCount by remember { mutableStateOf(messages.size) }
    LaunchedEffect(messages.size) {
        if (messages.size > prevMessageCount) {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            // header takes index 0, messages start at 1
            val totalItems = listState.layoutInfo.totalItemsCount
            if (totalItems - lastVisible <= 3) {
                listState.animateScrollToItem(totalItems - 1)
            }
        }
        prevMessageCount = messages.size
    }

    val urlDisplay = remember(entry.url) {
        formatUrlDisplay(entry.url)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "WS $urlDisplay",
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    StatusChip(status = entry.status)
                },
            )
        },
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            // Connection info header
            item(key = "header") {
                ConnectionInfoHeader(
                    modifier = Modifier.fillMaxWidth(),
                    entry = entry,
                )
            }

            // History cleared banner
            if (entry.historyCleared) {
                item(key = "history_cleared") {
                    HistoryClearedBanner()
                }
            }

            // Messages
            items(messages, key = { it.id }) { message ->
                MessageBubble(
                    modifier = Modifier.fillMaxWidth(),
                    message = message,
                )
            }

            // Bottom spacer
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun ConnectionInfoHeader(
    modifier: Modifier = Modifier,
    entry: SocketConnection,
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = entry.url,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            InfoLabel(
                label = "Status",
                value = entry.status.name,
            )
            InfoLabel(
                label = "Opened",
                value = formatTime(entry.timestamp),
            )
        }

        if (entry.closedAt != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                InfoLabel(
                    label = "Closed",
                    value = formatTime(entry.closedAt),
                )
                entry.closeCode?.let {
                    InfoLabel(
                        label = "Code",
                        value = it.toString(),
                    )
                }
            }

            entry.closeReason?.let {
                InfoLabel(
                    label = "Reason",
                    value = it,
                )
            }
        }

        if (entry.failureMessage != null) {
            InfoLabel(
                label = "Error",
                value = entry.failureMessage,
            )
        }

        if (entry.protocol != null) {
            InfoLabel(
                label = "Protocol", value = entry.protocol,
            )
        }

        if (entry.requestHeaders.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Request Headers",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            entry.requestHeaders.forEach { (key, value) ->
                Text(
                    text = "$key: $value",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
    HorizontalDivider()
}

@Composable
private fun HistoryClearedBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(WiretapColors.HistoryClearedBackground)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "History was cleared \u2014 only showing new messages",
            style = MaterialTheme.typography.labelMedium,
            color = WiretapColors.HistoryClearedText,
        )
    }
}

@Preview
@Composable
private fun Preview_ConnectionInfoHeader() {
    MaterialTheme {
        ConnectionInfoHeader(
            entry = SocketConnection(
                id = 1,
                url = "wss://echo.websocket.org/chat",
                status = SocketStatus.Open,
                timestamp = 1710850000000,
                requestHeaders = mapOf(
                    "Sec-WebSocket-Key" to "dGhlIHNhbXBsZSBub25jZQ==",
                    "Sec-WebSocket-Version" to "13",
                ),
                protocol = "chat",
            ),
        )
    }
}

@Preview
@Composable
private fun Preview_ConnectionInfoHeaderClosed() {
    MaterialTheme {
        ConnectionInfoHeader(
            entry = SocketConnection(
                id = 2,
                url = "wss://api.example.com/stream",
                status = SocketStatus.Closed,
                timestamp = 1710850000000,
                closedAt = 1710850120000,
                closeCode = 1000,
                closeReason = "Normal closure",
            ),
        )
    }
}

@Preview
@Composable
private fun Preview_MessageBubbleSent() {
    MaterialTheme {
        MessageBubble(
            message = SocketMessage(
                id = 1,
                socketId = 1,
                direction = SocketMessageType.Sent,
                contentType = SocketContentType.Text,
                content = """{"type":"ping","id":42}""",
                byteCount = 23,
                timestamp = 1710850000000,
            ),
        )
    }
}

@Preview
@Composable
private fun Preview_MessageBubbleReceived() {
    MaterialTheme {
        MessageBubble(
            message = SocketMessage(
                id = 2,
                socketId = 1,
                direction = SocketMessageType.Received,
                contentType = SocketContentType.Text,
                content = """{"type":"pong","id":42,"data":{"status":"ok"}}""",
                byteCount = 46,
                timestamp = 1710850001000,
            ),
        )
    }
}

@Preview
@Composable
private fun Preview_MessageBubbleBinary() {
    MaterialTheme {
        MessageBubble(
            message = SocketMessage(
                id = 3,
                socketId = 1,
                direction = SocketMessageType.Received,
                contentType = SocketContentType.Binary,
                content = "",
                byteCount = 1024,
                timestamp = 1710850002000,
            ),
        )
    }
}

@Preview
@Composable
private fun Preview_HistoryClearedBanner() {
    MaterialTheme {
        HistoryClearedBanner()
    }
}
