package dev.skymansandy.wiretap.ui.screens.console.socket

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.skymansandy.wiretap.data.db.entity.SocketLogEntry
import dev.skymansandy.wiretap.data.db.entity.SocketMessage
import dev.skymansandy.wiretap.domain.model.SocketContentType
import dev.skymansandy.wiretap.domain.model.SocketMessageDirection
import dev.skymansandy.wiretap.domain.model.SocketStatus
import dev.skymansandy.wiretap.helper.util.formatBytes
import dev.skymansandy.wiretap.helper.util.formatTime
import dev.skymansandy.wiretap.ui.theme.WiretapColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SocketDetailScreen(
    viewModel: SocketDetailViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
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

    val urlDisplay = viewModel.urlDisplay(entry.url)

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
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    StatusChip(entry.status)
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
                ConnectionInfoHeader(entry)
            }

            // History cleared banner
            if (entry.historyCleared) {
                item(key = "history_cleared") {
                    HistoryClearedBanner()
                }
            }

            // Messages
            items(messages, key = { it.id }) { message ->
                MessageBubble(message)
            }

            // Bottom spacer
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun ConnectionInfoHeader(entry: SocketLogEntry) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = entry.url,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            InfoLabel("Status", entry.status.name)
            InfoLabel("Opened", formatTime(entry.timestamp))
        }

        if (entry.closedAt != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                InfoLabel("Closed", formatTime(entry.closedAt))
                entry.closeCode?.let { InfoLabel("Code", it.toString()) }
            }
            entry.closeReason?.let { InfoLabel("Reason", it) }
        }

        if (entry.failureMessage != null) {
            InfoLabel("Error", entry.failureMessage)
        }

        if (entry.protocol != null) {
            InfoLabel("Protocol", entry.protocol)
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
private fun InfoLabel(
    label: String,
    value: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun MessageBubble(
    message: SocketMessage,
) {
    val isSent = message.direction == SocketMessageDirection.Sent
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp),
        contentAlignment = alignment,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .background(bgColor, RoundedCornerShape(12.dp))
                .padding(10.dp),
        ) {
            val displayText = when (message.contentType) {
                SocketContentType.Binary -> "[Binary: ${formatBytes(message.byteCount)}]"

                else -> message.content
            }

            Text(
                text = displayText,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = textColor,
            )

            Spacer(Modifier.height(2.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
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
            }
        }
    }
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

@Composable
private fun StatusChip(status: SocketStatus) {
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
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = Modifier
            .padding(end = 12.dp)
            .background(bgColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

@Preview
@Composable
private fun Preview_ConnectionInfoHeader() {
    MaterialTheme {
        ConnectionInfoHeader(
            entry = SocketLogEntry(
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
            entry = SocketLogEntry(
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
                direction = SocketMessageDirection.Sent,
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
                direction = SocketMessageDirection.Received,
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
                direction = SocketMessageDirection.Received,
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

@Preview
@Composable
private fun Preview_StatusChipOpen() {
    MaterialTheme {
        StatusChip(SocketStatus.Open)
    }
}

@Preview
@Composable
private fun Preview_StatusChipFailed() {
    MaterialTheme {
        StatusChip(SocketStatus.Failed)
    }
}
