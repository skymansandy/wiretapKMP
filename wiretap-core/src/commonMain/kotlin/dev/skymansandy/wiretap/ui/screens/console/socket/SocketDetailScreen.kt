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
import androidx.compose.runtime.produceState
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
import dev.skymansandy.wiretap.domain.orchestrator.WiretapOrchestrator
import dev.skymansandy.wiretap.helper.util.formatBytes
import dev.skymansandy.wiretap.helper.util.formatTime
import dev.skymansandy.wiretap.resources.Res
import dev.skymansandy.wiretap.resources.back
import dev.skymansandy.wiretap.resources.binary_message
import dev.skymansandy.wiretap.resources.history_cleared
import dev.skymansandy.wiretap.resources.label_closed
import dev.skymansandy.wiretap.resources.label_code
import dev.skymansandy.wiretap.resources.label_error
import dev.skymansandy.wiretap.resources.label_opened
import dev.skymansandy.wiretap.resources.label_protocol
import dev.skymansandy.wiretap.resources.label_reason
import dev.skymansandy.wiretap.resources.label_status
import dev.skymansandy.wiretap.resources.request_headers
import dev.skymansandy.wiretap.resources.status_closed
import dev.skymansandy.wiretap.resources.status_closing
import dev.skymansandy.wiretap.resources.status_connecting
import dev.skymansandy.wiretap.resources.status_failed
import dev.skymansandy.wiretap.resources.status_open
import dev.skymansandy.wiretap.resources.ws_title
import dev.skymansandy.wiretap.ui.theme.WiretapColors
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SocketDetailScreen(
    socketId: Long,
    orchestrator: WiretapOrchestrator,
    onBack: () -> Unit,
) {
    val initialEntry by produceState<SocketLogEntry?>(null, socketId) {
        value = orchestrator.getSocketById(socketId)
    }
    if (initialEntry == null) {
        return
    }

    val liveEntryOrNull by orchestrator.getSocketByIdFlow(socketId)
        .collectAsStateWithLifecycle(initialEntry)
    val liveEntry = liveEntryOrNull ?: run {
        onBack()
        return
    }

    val messages by orchestrator.getSocketMessages(socketId)
        .collectAsStateWithLifecycle(emptyList())
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

    val urlDisplay = liveEntry.url.substringAfter("://").let {
        val host = it.substringBefore("/").substringBefore("?")
        val path = it.removePrefix(host).ifEmpty { "/" }
        "$host$path"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(Res.string.ws_title, urlDisplay),
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
                            contentDescription = stringResource(Res.string.back)
                        )
                    }
                },
                actions = {
                    StatusChip(liveEntry.status)
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
                ConnectionInfoHeader(liveEntry)
            }

            // History cleared banner
            if (liveEntry.historyCleared) {
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
            InfoLabel(stringResource(Res.string.label_status), entry.status.name)
            InfoLabel(stringResource(Res.string.label_opened), formatTime(entry.timestamp))
        }

        if (entry.closedAt != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                InfoLabel(stringResource(Res.string.label_closed), formatTime(entry.closedAt))
                entry.closeCode?.let { InfoLabel(stringResource(Res.string.label_code), it.toString()) }
            }
            entry.closeReason?.let { InfoLabel(stringResource(Res.string.label_reason), it) }
        }

        if (entry.failureMessage != null) {
            InfoLabel(stringResource(Res.string.label_error), entry.failureMessage)
        }

        if (entry.protocol != null) {
            InfoLabel(stringResource(Res.string.label_protocol), entry.protocol)
        }

        if (entry.requestHeaders.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(Res.string.request_headers),
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
                SocketContentType.Binary -> stringResource(
                    Res.string.binary_message,
                    formatBytes(message.byteCount)
                )

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
            text = stringResource(Res.string.history_cleared),
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
        SocketStatus.Connecting -> stringResource(Res.string.status_connecting)
        SocketStatus.Open -> stringResource(Res.string.status_open)
        SocketStatus.Closing -> stringResource(Res.string.status_closing)
        SocketStatus.Closed -> stringResource(Res.string.status_closed)
        SocketStatus.Failed -> stringResource(Res.string.status_failed)
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
