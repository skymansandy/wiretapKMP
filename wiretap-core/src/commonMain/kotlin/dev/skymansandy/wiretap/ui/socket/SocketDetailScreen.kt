package dev.skymansandy.wiretap.ui.socket

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
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.data.db.entity.SocketLogEntry
import dev.skymansandy.wiretap.data.db.entity.SocketMessage
import dev.skymansandy.wiretap.domain.model.SocketContentType
import dev.skymansandy.wiretap.domain.model.SocketMessageDirection
import dev.skymansandy.wiretap.domain.model.SocketStatus
import dev.skymansandy.wiretap.domain.orchestrator.WiretapOrchestrator
import dev.skymansandy.wiretap.util.formatBytes
import dev.skymansandy.wiretap.util.formatTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocketDetailScreen(
    socketId: Long,
    orchestrator: WiretapOrchestrator,
    onBack: () -> Unit,
) {
    val initialEntry = remember(socketId) { orchestrator.getSocketById(socketId) }
    if (initialEntry == null) {
        onBack()
        return
    }

    val liveEntryOrNull by orchestrator.getSocketByIdFlow(socketId).collectAsState(initialEntry)
    val liveEntry = liveEntryOrNull ?: run {
        onBack()
        return
    }

    val messages by orchestrator.getSocketMessages(socketId).collectAsState(emptyList())
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
                            text = "WS $urlDisplay",
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
private fun InfoLabel(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
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
private fun MessageBubble(message: SocketMessage) {
    val isSent = message.direction == SocketMessageDirection.SENT
    val alignment = if (isSent) Alignment.CenterEnd else Alignment.CenterStart

    val bgColor = if (isSent) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = if (isSent) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
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
            val displayText = if (message.contentType == SocketContentType.BINARY) {
                "[Binary: ${formatBytes(message.byteCount)}]"
            } else {
                message.content
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
            .background(Color(0xFFFFF3E0))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "History was cleared — only showing new messages",
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFFE65100),
        )
    }
}

@Composable
private fun StatusChip(status: SocketStatus) {
    val (bgColor, label) = when (status) {
        SocketStatus.CONNECTING -> Color(0xFF42A5F5) to "Connecting"
        SocketStatus.OPEN -> Color(0xFF4CAF50) to "Open"
        SocketStatus.CLOSING -> Color(0xFFFFA726) to "Closing"
        SocketStatus.CLOSED -> Color(0xFF9E9E9E) to "Closed"
        SocketStatus.FAILED -> Color(0xFFEF5350) to "Failed"
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

