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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import dev.skymansandy.wiretap.helper.util.highlightText
import dev.skymansandy.wiretap.helper.util.shareLogAsFile
import dev.skymansandy.wiretap.helper.util.shareLogTextOrFile
import dev.skymansandy.wiretap.navigation.compose.LocalWiretapNavigator
import dev.skymansandy.wiretap.ui.common.InfoLabel
import dev.skymansandy.wiretap.ui.common.LocalSnackbarHostState
import dev.skymansandy.wiretap.ui.common.MessageBubble
import dev.skymansandy.wiretap.ui.common.PlatformBackHandler
import dev.skymansandy.wiretap.ui.common.ScrollToBottomChip
import dev.skymansandy.wiretap.ui.common.SearchField
import dev.skymansandy.wiretap.ui.screens.socket.components.StatusChip
import dev.skymansandy.wiretap.ui.theme.WiretapColors
import kotlinx.coroutines.launch
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
    val isSearchActive by viewModel.isSearchActive.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val debouncedQuery by viewModel.debouncedQuery.collectAsStateWithLifecycle()
    val matches by viewModel.matches.collectAsStateWithLifecycle()
    val currentMatchIndex by viewModel.currentMatchIndex.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()
    val searchFocusRequester = remember { FocusRequester() }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            searchFocusRequester.requestFocus()
        }
    }

    // Back dismisses the search bar before it pops the screen.
    PlatformBackHandler(enabled = isSearchActive, onBack = viewModel::closeSearch)

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
        listState.animateScrollToItem(match.listItemIndex(headerOffset))
    }

    val urlDisplay = remember(entry.url) {
        formatUrlDisplay(entry.url)
    }

    CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
        Scaffold(
            modifier = modifier,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                SocketDetailTopBar(
                    urlDisplay = urlDisplay,
                    status = entry.status,
                    isSearchActive = isSearchActive,
                    searchQuery = searchQuery,
                    searchFocusRequester = searchFocusRequester,
                    onSearchQueryChange = viewModel::setSearchQuery,
                    onActivateSearch = viewModel::activateSearch,
                    onCloseSearch = viewModel::closeSearch,
                    onBack = { navigator.pop() },
                    onShareAsText = {
                        val message = shareLogTextOrFile(
                            subject = viewModel.shareSubject,
                            text = viewModel.buildShareText(),
                            fileName = viewModel.shareFileName,
                        )
                        message?.let { coroutineScope.launch { snackbarHostState.showSnackbar(it) } }
                    },
                    onShareAsFile = {
                        shareLogAsFile(
                            content = viewModel.buildShareText(),
                            fileName = viewModel.shareFileName,
                        )
                    },
                )
            },
        ) { padding ->
            SocketDetailContent(
                modifier = Modifier.fillMaxSize().padding(padding),
                entry = entry,
                messages = messages,
                listState = listState,
                showNavigator = isSearchActive && debouncedQuery.isNotEmpty(),
                debouncedQuery = debouncedQuery,
                matches = matches,
                currentMatchIndex = currentMatchIndex,
                onPreviousMatch = viewModel::goToPreviousMatch,
                onNextMatch = viewModel::goToNextMatch,
            )
        }
    }
}

@Composable
private fun SocketDetailContent(
    modifier: Modifier = Modifier,
    entry: SocketConnection,
    messages: List<SocketMessage>,
    listState: LazyListState,
    showNavigator: Boolean,
    debouncedQuery: String,
    matches: List<SocketMatchPosition>,
    currentMatchIndex: Int,
    onPreviousMatch: () -> Unit,
    onNextMatch: () -> Unit,
) {
    val activeMatch = matches.getOrNull(currentMatchIndex)

    Column(modifier = modifier) {
        if (showNavigator) {
            SearchNavigatorBar(
                matchCount = matches.size,
                currentIndex = currentMatchIndex,
                onPrevious = onPreviousMatch,
                onNext = onNextMatch,
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
                item(key = "header") {
                    ConnectionInfoHeader(
                        modifier = Modifier.fillMaxWidth(),
                        entry = entry,
                        searchQuery = debouncedQuery,
                        activeMatch = activeMatch,
                    )
                }

                if (entry.historyCleared) {
                    item(key = "history_cleared") {
                        HistoryClearedBanner()
                    }
                }

                itemsIndexed(messages, key = { _, m -> m.id }) { index, message ->
                    val messageMatch = activeMatch
                        ?.takeIf { it.field == SocketMatchField.Message && it.index == index }
                    MessageBubble(
                        modifier = Modifier.fillMaxWidth(),
                        message = message,
                        searchQuery = debouncedQuery,
                        activeMatchRange = messageMatch?.let { it.start..it.endInclusive },
                    )
                }

                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SocketDetailTopBar(
    urlDisplay: String,
    status: SocketStatus,
    isSearchActive: Boolean,
    searchQuery: String,
    searchFocusRequester: FocusRequester,
    onSearchQueryChange: (String) -> Unit,
    onActivateSearch: () -> Unit,
    onCloseSearch: () -> Unit,
    onBack: () -> Unit,
    onShareAsText: () -> Unit,
    onShareAsFile: () -> Unit,
) {
    var showShareMenu by remember { mutableStateOf(false) }
    TopAppBar(
        title = {
            if (isSearchActive) {
                SearchField(
                    modifier = Modifier.focusRequester(searchFocusRequester),
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange,
                )
            } else {
                Text(
                    text = "WS $urlDisplay",
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = if (isSearchActive) onCloseSearch else onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                )
            }
        },
        actions = {
            if (isSearchActive) {
                IconButton(onClick = onCloseSearch) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close search",
                    )
                }
            } else {
                IconButton(onClick = onActivateSearch) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                    )
                }
                Box {
                    IconButton(onClick = { showShareMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                        )
                    }
                    DropdownMenu(
                        expanded = showShareMenu,
                        onDismissRequest = { showShareMenu = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Share as text") },
                            onClick = {
                                showShareMenu = false
                                onShareAsText()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Share as file") },
                            onClick = {
                                showShareMenu = false
                                onShareAsFile()
                            },
                        )
                    }
                }
                StatusChip(status = status)
            }
        },
    )
}

@Composable
private fun SearchNavigatorBar(
    matchCount: Int,
    currentIndex: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    val display = if (matchCount == 0) 0 else currentIndex.coerceAtMost(matchCount - 1) + 1
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
    searchQuery: String = "",
    activeMatch: SocketMatchPosition? = null,
) {
    fun activeRangeFor(field: SocketMatchField, index: Int) = activeMatch
        ?.takeIf { it.field == field && it.index == index }
        ?.let { it.start..it.endInclusive }

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = highlightText(
                entry.url,
                searchQuery,
                activeRangeFor(SocketMatchField.Url, 0),
            ),
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
            entry.requestHeaders.entries.forEachIndexed { index, (key, value) ->
                Text(
                    text = highlightText(
                        "$key: $value",
                        searchQuery,
                        activeRangeFor(SocketMatchField.RequestHeader, index),
                    ),
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

/** Which LazyColumn item holds this match: the connection block, or a bubble. */
private fun SocketMatchPosition.listItemIndex(headerOffset: Int): Int = when (field) {
    SocketMatchField.Message -> index + headerOffset
    SocketMatchField.Url, SocketMatchField.RequestHeader -> 0
}
