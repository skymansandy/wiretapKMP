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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.skymansandy.wiretap.domain.model.SseConnection
import dev.skymansandy.wiretap.helper.util.formatTime
import dev.skymansandy.wiretap.helper.util.formatUrlDisplay
import dev.skymansandy.wiretap.navigation.compose.LocalWiretapNavigator
import dev.skymansandy.wiretap.ui.common.InfoLabel
import dev.skymansandy.wiretap.ui.common.ScrollToBottomChip
import dev.skymansandy.wiretap.ui.screens.sse.components.SseEventBubble
import dev.skymansandy.wiretap.ui.screens.sse.components.SseStatusChip
import dev.skymansandy.wiretap.ui.theme.WiretapColors
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
    val listState = rememberLazyListState()

    // Scroll to bottom on initial load
    LaunchedEffect(Unit) {
        if (events.isNotEmpty()) {
            listState.scrollToItem(listState.layoutInfo.totalItemsCount - 1)
        }
    }

    // Auto-scroll to bottom when new events arrive and already near bottom
    var prevEventCount by remember { mutableStateOf(events.size) }
    LaunchedEffect(events.size) {
        if (events.size > prevEventCount) {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            if (totalItems - lastVisible <= 3) {
                listState.animateScrollToItem(totalItems - 1)
            }
        }
        prevEventCount = events.size
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
                            text = "SSE $urlDisplay",
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
                    SseStatusChip(status = entry.status)
                },
            )
        },
    ) { padding ->
        ScrollToBottomChip(
            listState = listState,
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
            ) {
                // Connection info header
                item(key = "header") {
                    SseConnectionInfoHeader(
                        modifier = Modifier.fillMaxWidth(),
                        entry = entry,
                    )
                }

                // History cleared banner
                if (entry.historyCleared) {
                    item(key = "history_cleared") {
                        SseHistoryClearedBanner()
                    }
                }

                // Events
                items(events, key = { it.id }) { event ->
                    SseEventBubble(
                        modifier = Modifier.fillMaxWidth(),
                        event = event,
                    )
                }

                // Bottom spacer
                item { Spacer(Modifier.height(16.dp)) }
            }
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
