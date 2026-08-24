/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.ui.screens.sse.detail

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.skymansandy.wiretap.domain.model.SseConnection
import dev.skymansandy.wiretap.domain.model.SseEvent
import dev.skymansandy.wiretap.domain.model.SseStatus
import dev.skymansandy.wiretap.helper.util.formatTime
import dev.skymansandy.wiretap.helper.util.formatUrlDisplay
import dev.skymansandy.wiretap.helper.util.shareLogAsFile
import dev.skymansandy.wiretap.helper.util.shareLogTextOrFile
import dev.skymansandy.wiretap.navigation.compose.LocalWiretapNavigator
import dev.skymansandy.wiretap.ui.common.InfoLabel
import dev.skymansandy.wiretap.ui.common.LocalSnackbarHostState
import dev.skymansandy.wiretap.ui.common.ScrollToBottomChip
import dev.skymansandy.wiretap.ui.common.SearchField
import dev.skymansandy.wiretap.ui.screens.sse.components.SseEventBubble
import dev.skymansandy.wiretap.ui.screens.sse.components.SseStatusChip
import dev.skymansandy.wiretap.ui.theme.WiretapColors
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SseDetailScreenView(
    modifier: Modifier = Modifier,
    connectionId: Long,
    viewModel: SseDetailViewModel = koinViewModel { parametersOf(connectionId) },
) {
    val navigator = LocalWiretapNavigator.current
    val initialEntry by viewModel.initialEntry.collectAsStateWithLifecycle()
    val liveEntry by viewModel.liveEntry.collectAsStateWithLifecycle()
    val entry = liveEntry ?: initialEntry

    if (entry == null) {
        return
    }

    val events by viewModel.events.collectAsStateWithLifecycle()
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

    val headerOffset = 1 + (if (entry.historyCleared) 1 else 0)
    val autoScrollDisabled = isSearchActive && debouncedQuery.isNotEmpty()

    // Scroll to bottom on initial load
    LaunchedEffect(Unit) {
        if (events.isNotEmpty()) {
            listState.scrollToItem(listState.layoutInfo.totalItemsCount - 1)
        }
    }

    // Auto-scroll to bottom when new events arrive and already near bottom
    var prevEventCount by remember { mutableStateOf(events.size) }
    LaunchedEffect(events.size) {
        if (!autoScrollDisabled && events.size > prevEventCount) {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            if (totalItems - lastVisible <= 3) {
                listState.animateScrollToItem(totalItems - 1)
            }
        }
        prevEventCount = events.size
    }

    // Scroll to the active search match
    LaunchedEffect(currentMatchIndex, matches) {
        val match = matches.getOrNull(currentMatchIndex) ?: return@LaunchedEffect
        listState.animateScrollToItem(match.eventIndex + headerOffset)
    }

    val urlDisplay = remember(entry.url) {
        formatUrlDisplay(entry.url)
    }

    CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
        Scaffold(
            modifier = modifier,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                SseDetailTopBar(
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
            SseDetailContent(
                modifier = Modifier.fillMaxSize().padding(padding),
                entry = entry,
                events = events,
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
private fun SseDetailContent(
    modifier: Modifier = Modifier,
    entry: SseConnection,
    events: List<SseEvent>,
    listState: LazyListState,
    showNavigator: Boolean,
    debouncedQuery: String,
    matches: List<SseMatchPosition>,
    currentMatchIndex: Int,
    onPreviousMatch: () -> Unit,
    onNextMatch: () -> Unit,
) {
    Column(modifier = modifier) {
        if (showNavigator) {
            SseSearchNavigatorBar(
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
                    SseConnectionInfoHeader(
                        modifier = Modifier.fillMaxWidth(),
                        entry = entry,
                    )
                }

                if (entry.historyCleared) {
                    item(key = "history_cleared") {
                        SseHistoryClearedBanner()
                    }
                }

                itemsIndexed(events, key = { _, e -> e.id }) { index, event ->
                    val activeMatch = matches.getOrNull(currentMatchIndex)
                    val activeRange = if (activeMatch?.eventIndex == index) {
                        activeMatch.start..activeMatch.endInclusive
                    } else {
                        null
                    }
                    SseEventBubble(
                        modifier = Modifier.fillMaxWidth(),
                        event = event,
                        searchQuery = debouncedQuery,
                        activeMatchRange = activeRange,
                    )
                }

                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SseDetailTopBar(
    urlDisplay: String,
    status: SseStatus,
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
                    text = "SSE $urlDisplay",
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
                SseStatusChip(status = status)
            }
        },
    )
}

@Composable
private fun SseSearchNavigatorBar(
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
private fun SseConnectionInfoHeader(
    modifier: Modifier = Modifier,
    entry: SseConnection,
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
            InfoLabel(
                label = "Closed",
                value = formatTime(entry.closedAt),
            )
        }

        if (entry.failureMessage != null) {
            InfoLabel(
                label = "Error",
                value = entry.failureMessage,
            )
        }

        if (entry.lastEventId != null) {
            InfoLabel(
                label = "Last Event ID",
                value = entry.lastEventId,
            )
        }

        if (entry.retryMs != null) {
            InfoLabel(
                label = "Retry",
                value = "${entry.retryMs}ms",
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
private fun SseHistoryClearedBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(WiretapColors.HistoryClearedBackground)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "History was cleared \u2014 only showing new events",
            style = MaterialTheme.typography.labelMedium,
            color = WiretapColors.HistoryClearedText,
        )
    }
}
