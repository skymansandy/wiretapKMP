/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.ui.screens.sse.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.unit.dp
import app.cash.paging.LoadStateNotLoading
import app.cash.paging.compose.collectAsLazyPagingItems
import app.cash.paging.compose.itemKey
import dev.skymansandy.wiretap.domain.model.SseConnection
import dev.skymansandy.wiretap.helper.util.formatTime
import dev.skymansandy.wiretap.helper.util.highlightText
import dev.skymansandy.wiretap.ui.common.EmptyStateSetupView
import dev.skymansandy.wiretap.ui.common.ScrollToTopButton
import dev.skymansandy.wiretap.ui.common.StatusText
import dev.skymansandy.wiretap.ui.common.WiretapConstants
import dev.skymansandy.wiretap.ui.screens.sse.components.SseStatusChip
import dev.skymansandy.wiretap.ui.theme.WiretapColors
import kotlinx.coroutines.launch

@Composable
internal fun SseLogList(
    modifier: Modifier = Modifier,
    viewModel: SseLogListViewModel,
    searchQuery: String,
    onDismissSearch: () -> Unit,
    onConnectionClick: (SseConnection) -> Unit,
) {
    val lazyItems = viewModel.sseLogs.collectAsLazyPagingItems()
    val isEmpty = lazyItems.itemCount == 0

    if (isEmpty && lazyItems.loadState.refresh is LoadStateNotLoading) {
        if (searchQuery.isNotBlank()) {
            StatusText(
                modifier = modifier,
                text = "No results found",
            )
        } else {
            EmptyStateSetupView(
                modifier = modifier,
                description = "This tab displays SSE (Server-Sent Events) connections captured by Wiretap." +
                    " Add a Wiretap SSE plugin to your client to start capturing traffic.",
                linkUrl = WiretapConstants.SETUP_URL,
            )
        }
    } else if (!isEmpty) {
        val listState = rememberLazyListState()
        val scope = rememberCoroutineScope()
        var lastItemCount by remember { mutableIntStateOf(lazyItems.itemCount) }
        val isAtTop by remember {
            derivedStateOf {
                listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
            }
        }

        LaunchedEffect(lazyItems.itemCount) {
            if (lazyItems.itemCount > lastItemCount && isAtTop) {
                scope.launch { listState.scrollToItem(0) }
            }
            lastItemCount = lazyItems.itemCount
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
                    count = lazyItems.itemCount,
                    key = lazyItems.itemKey { it.id },
                ) { index ->
                    val connection = lazyItems[index] ?: return@items
                    SseLogItemContent(
                        connection = connection,
                        searchQuery = searchQuery,
                        onClick = {
                            onDismissSearch()
                            onConnectionClick(connection)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun SseLogItemContent(
    connection: SseConnection,
    searchQuery: String,
    onClick: () -> Unit,
) {
    val isSecure = remember(connection.url) { connection.url.startsWith("https://", ignoreCase = true) }
    val withoutScheme = remember(connection.url) { connection.url.substringAfter("://") }
    val host = remember(withoutScheme) { withoutScheme.substringBefore("/").substringBefore("?") }
    val path = remember(withoutScheme, host) { withoutScheme.removePrefix(host).ifEmpty { "/" } }

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
                text = "SSE",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = connection.statusColor,
            )

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = remember(path, searchQuery) { highlightText(path, searchQuery) },
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
                        text = remember(host, searchQuery) { highlightText(host, searchQuery) },
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
                        text = formatTime(connection.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (connection.eventCount > 0) {
                        Text(
                            text = "${connection.eventCount} events",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    SseStatusChip(status = connection.status)
                }
            }
        }

        HorizontalDivider()
    }
}
