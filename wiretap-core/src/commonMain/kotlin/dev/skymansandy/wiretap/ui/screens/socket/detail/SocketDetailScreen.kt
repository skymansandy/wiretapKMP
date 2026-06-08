/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import dev.skymansandy.wiretap.ui.common.ScrollToBottomChip
import dev.skymansandy.wiretap.ui.common.SearchField
import dev.skymansandy.wiretap.ui.screens.socket.components.StatusChip
import dev.skymansandy.wiretap.ui.theme.WiretapColors
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Suppress("CyclomaticComplexMethod")
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

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    val searchFocusRequester = remember { FocusRequester() }

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            searchFocusRequester.requestFocus()
        }
    }

    val debouncedQuery by produceState(initialValue = "", key1 = searchQuery) {
        if (searchQuery.isEmpty()) {
            value = ""
        } else {
            delay(450)
            value = searchQuery
        }
    }

    val matches = remember(messages, debouncedQuery) {
        computeSocketMatches(messages, debouncedQuery)
    }

    var currentMatchIndex by remember { mutableStateOf(0) }
    LaunchedEffect(matches) {
        currentMatchIndex = 0
    }

    val headerOffset = 1 + (if (entry.historyCleared) 1 else 0)
    val autoScrollDisabled = isSearchActive && debouncedQuery.isNotEmpty()

    // Scroll to bottom on initial load
    LaunchedEffect(Unit) {
        if (messages.isNotEmpty()) {
            listState.scrollToItem(listState.layoutInfo.totalItemsCount - 1)
        }
    }

    // Auto-scroll to bottom when new messages arrive and already near bottom
    var prevMessageCount by remember { mutableStateOf(messages.size) }
    LaunchedEffect(messages.size) {
        if (!autoScrollDisabled && messages.size > prevMessageCount) {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            if (totalItems - lastVisible <= 3) {
                listState.animateScrollToItem(totalItems - 1)
            }
        }
        prevMessageCount = messages.size
    }

    // Scroll to the active search match
    LaunchedEffect(currentMatchIndex, matches) {
        val match = matches.getOrNull(currentMatchIndex) ?: return@LaunchedEffect
        listState.animateScrollToItem(match.messageIndex + headerOffset)
    }

    val urlDisplay = remember(entry.url) {
        formatUrlDisplay(entry.url)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive) {
                        SearchField(
                            modifier = Modifier.focusRequester(searchFocusRequester),
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                        )
                    } else {
                        Column {
                            Text(
                                text = "WS $urlDisplay",
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (isSearchActive) {
                                isSearchActive = false
                                searchQuery = ""
                            } else {
                                navigator.pop()
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    if (isSearchActive) {
                        IconButton(
                            onClick = {
                                isSearchActive = false
                                searchQuery = ""
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close search",
                            )
                        }
                    } else {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                            )
                        }
                        StatusChip(status = entry.status)
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isSearchActive && debouncedQuery.isNotEmpty()) {
                SearchNavigatorBar(
                    matchCount = matches.size,
                    currentIndex = currentMatchIndex,
                    onPrevious = {
                        if (matches.isNotEmpty()) {
                            currentMatchIndex = (currentMatchIndex - 1 + matches.size) % matches.size
                        }
                    },
                    onNext = {
                        if (matches.isNotEmpty()) {
                            currentMatchIndex = (currentMatchIndex + 1) % matches.size
                        }
                    },
                )
                HorizontalDivider()
            }

            ScrollToBottomChip(
                listState = listState,
                modifier = Modifier.fillMaxSize(),
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
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
                    itemsIndexed(messages, key = { _, m -> m.id }) { index, message ->
                        val activeMatch = matches.getOrNull(currentMatchIndex)
                        val activeRange = if (activeMatch?.messageIndex == index) {
                            activeMatch.start..activeMatch.endInclusive
                        } else {
                            null
                        }
                        MessageBubble(
                            modifier = Modifier.fillMaxWidth(),
                            message = message,
                            searchQuery = debouncedQuery,
                            activeMatchRange = activeRange,
                        )
                    }

                    // Bottom spacer
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

private data class SocketMatchPosition(
    val messageIndex: Int,
    val start: Int,
    val endInclusive: Int,
)

private fun computeSocketMatches(
    messages: List<SocketMessage>,
    query: String,
): List<SocketMatchPosition> {
    if (query.isBlank()) return emptyList()
    val lowerQuery = query.lowercase()
    val results = mutableListOf<SocketMatchPosition>()
    messages.forEachIndexed { index, message ->
        if (!message.contentType.isSearchable()) return@forEachIndexed
        val lowerContent = message.content.lowercase()
        var cursor = 0
        while (true) {
            val hit = lowerContent.indexOf(lowerQuery, cursor)
            if (hit < 0) break
            results += SocketMatchPosition(
                messageIndex = index,
                start = hit,
                endInclusive = hit + query.length - 1,
            )
            cursor = hit + query.length
        }
    }
    return results
}

private fun SocketContentType.isSearchable(): Boolean = when (this) {
    SocketContentType.Text -> true
    SocketContentType.Binary,
    SocketContentType.Ping,
    SocketContentType.Pong,
    SocketContentType.Close,
    -> false
}

@Composable
private fun SearchNavigatorBar(
    matchCount: Int,
    currentIndex: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    val display = if (matchCount == 0) 0 else currentIndex + 1
    val enabled = matchCount > 0
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
    ) {
        Text(
            text = "$display / $matchCount",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        IconButton(onClick = onPrevious, enabled = enabled) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Previous match",
            )
        }
        IconButton(onClick = onNext, enabled = enabled) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Next match",
            )
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
                content = "[Binary: 1.0 KB]",
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
