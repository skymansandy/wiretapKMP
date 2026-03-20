package dev.skymansandy.wiretap.ui.http

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import app.cash.paging.LoadStateError
import app.cash.paging.LoadStateLoading
import app.cash.paging.LoadStateNotLoading
import app.cash.paging.compose.LazyPagingItems
import app.cash.paging.compose.itemKey
import dev.skymansandy.wiretap.data.db.entity.NetworkLogEntry
import dev.skymansandy.wiretap.domain.model.ResponseSource
import dev.skymansandy.wiretap.ui.components.highlightText
import dev.skymansandy.wiretap.util.formatOneDecimal
import dev.skymansandy.wiretap.util.formatTime
import dev.skymansandy.wiretap.resources.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
internal fun HttpLogList(
    lazyItems: LazyPagingItems<NetworkLogEntry>,
    searchQuery: String,
    onHttpClick: (NetworkLogEntry) -> Unit,
    onCreateRule: (NetworkLogEntry) -> Unit,
    onViewRule: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        lazyItems.loadState.refresh is LoadStateLoading -> {
            Box(modifier, contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        lazyItems.loadState.refresh is LoadStateNotLoading && lazyItems.itemCount == 0 -> {
            Box(modifier, contentAlignment = Alignment.Center) {
                Text(stringResource(Res.string.no_http_logs), style = MaterialTheme.typography.bodyLarge)
            }
        }

        lazyItems.loadState.refresh is LoadStateError -> {
            Box(modifier, contentAlignment = Alignment.Center) {
                Text(stringResource(Res.string.failed_to_load_logs), style = MaterialTheme.typography.bodyLarge)
            }
        }

        else -> {
            val listState = rememberLazyListState()
            val scope = rememberCoroutineScope()
            val isAtTop = remember(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
                listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
            }
            var lastItemCount by remember { mutableIntStateOf(lazyItems.itemCount) }
            var revealedItemId by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(lazyItems.itemCount) {
                if (lazyItems.itemCount > lastItemCount && isAtTop) {
                    scope.launch { listState.scrollToItem(0) }
                }
                lastItemCount = lazyItems.itemCount
            }

            LaunchedEffect(revealedItemId) {
                if (revealedItemId != null) {
                    delay(3000)
                    revealedItemId = null
                }
            }

            LazyColumn(state = listState, modifier = modifier) {
                items(
                    count = lazyItems.itemCount,
                    key = lazyItems.itemKey { it.id },
                ) { index ->
                    val entry = lazyItems[index] ?: return@items
                    val itemKey = "http_${entry.id}"
                    SwipeableNetworkLogItem(
                        entry = entry,
                        searchQuery = searchQuery,
                        isRevealed = revealedItemId == itemKey,
                        onReveal = { revealedItemId = itemKey },
                        onCollapse = { if (revealedItemId == itemKey) revealedItemId = null },
                        onClick = {
                            revealedItemId = null
                            onHttpClick(entry)
                        },
                        onCreateRule = {
                            revealedItemId = null
                            onCreateRule(entry)
                        },
                        onViewRule = {
                            revealedItemId = null
                            entry.matchedRuleId?.let(onViewRule)
                        },
                    )
                }
                if (lazyItems.loadState.append is LoadStateLoading) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center,
                        ) { CircularProgressIndicator(modifier = Modifier.size(24.dp)) }
                    }
                }
            }
        }
    }
}

private val RevealWidth = 64.dp

@Composable
private fun SwipeableNetworkLogItem(
    entry: NetworkLogEntry,
    searchQuery: String,
    isRevealed: Boolean,
    onReveal: () -> Unit,
    onCollapse: () -> Unit,
    onClick: () -> Unit,
    onCreateRule: () -> Unit,
    onViewRule: () -> Unit,
) {
    val hasMatchedRule = entry.source != ResponseSource.Network && entry.matchedRuleId != null
    val revealWidthPx = with(LocalDensity.current) { RevealWidth.toPx() }
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    // Sync external isRevealed -> animation
    LaunchedEffect(isRevealed) {
        val target = if (isRevealed) -revealWidthPx else 0f
        if (offsetX.value != target) {
            offsetX.animateTo(target)
        }
    }

    val bgColor = if (hasMatchedRule) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    val contentColor = if (hasMatchedRule) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        // Action revealed behind the item, pinned to end
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(bgColor)
                .clickable { if (hasMatchedRule) onViewRule() else onCreateRule() },
            contentAlignment = Alignment.CenterEnd,
        ) {
            Column(
                modifier = Modifier.width(RevealWidth),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = if (hasMatchedRule) Icons.Default.Visibility else Icons.Default.Add,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = if (hasMatchedRule) stringResource(Res.string.view_rule_swipe) else stringResource(Res.string.create_rule_swipe),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor,
                    textAlign = TextAlign.Center,
                )
            }
        }

        // Foreground content that slides
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.toInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                // Snap to revealed or closed based on how far user dragged
                                if (offsetX.value < -revealWidthPx / 2) {
                                    offsetX.animateTo(-revealWidthPx)
                                    onReveal()
                                } else {
                                    offsetX.animateTo(0f)
                                    onCollapse()
                                }
                            }
                        },
                        onDragCancel = {
                            scope.launch {
                                offsetX.animateTo(0f)
                                onCollapse()
                            }
                        },
                    ) { _, dragAmount ->
                        scope.launch {
                            val newValue = (offsetX.value + dragAmount)
                                .coerceIn(-revealWidthPx, 0f)
                            offsetX.snapTo(newValue)
                        }
                    }
                },
        ) {
            NetworkLogItemContent(
                entry = entry,
                searchQuery = searchQuery,
                onClick = onClick,
            )
        }
    }
}

@Composable
private fun NetworkLogItemContent(
    entry: NetworkLogEntry,
    searchQuery: String,
    onClick: () -> Unit,
) {
    val statusColor = when {
        entry.isInProgress -> Color(0xFF42A5F5) // Blue - in progress
        entry.responseCode in 200..299 -> Color(0xFF4CAF50) // Green - success
        entry.responseCode in 300..399 -> Color(0xFF42A5F5) // Blue - redirect
        entry.responseCode in 400..499 -> Color(0xFFFFA726) // Amber - client error
        entry.responseCode >= 500 -> Color(0xFFEF5350) // Red - server error
        else -> Color(0xFF9E9E9E) // Gray - timeout / cancelled / no response
    }

    val isHttps = entry.url.startsWith("https://", ignoreCase = true)
    val withoutScheme = entry.url.substringAfter("://")
    val host = withoutScheme.substringBefore("/").substringBefore("?")
    val path = withoutScheme.removePrefix(host).ifEmpty { "/" }

    val responseBytes = entry.responseBody?.encodeToByteArray()?.size ?: 0
    val formattedSize = when {
        responseBytes >= 1_048_576 -> "${formatOneDecimal(responseBytes / 1_048_576f)} MB"
        responseBytes >= 1_024 -> "${formatOneDecimal(responseBytes / 1_024f)} kB"
        responseBytes > 0 -> "$responseBytes B"
        else -> null
    }

    Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = when {
                    entry.isInProgress -> "..."
                    entry.responseCode > 0 -> entry.responseCode.toString()
                    entry.responseCode == -1 -> "!!!"
                    else -> "ERR"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = statusColor,
                modifier = Modifier.width(44.dp),
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = highlightText("${entry.method} $path", searchQuery),
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
                    if (isHttps) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = Color(0xFF26C6DA),
                        )
                    }
                    Text(
                        text = highlightText(host, searchQuery),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF26C6DA),
                    )
                }

                Spacer(Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = formatTime(entry.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "${entry.durationMs} ms",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (formattedSize != null) {
                        Text(
                            text = formattedSize,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (entry.source != ResponseSource.Network) {
                        SourceChip(entry.source)
                    }
                }
            }
        }
        HorizontalDivider()
    }
}

@Composable
private fun SourceChip(source: ResponseSource) {
    val bgColor = when (source) {
        ResponseSource.Mock -> MaterialTheme.colorScheme.secondaryContainer
        ResponseSource.Throttle -> MaterialTheme.colorScheme.tertiaryContainer
        ResponseSource.Network -> return
    }
    val textColor = when (source) {
        ResponseSource.Mock -> MaterialTheme.colorScheme.onSecondaryContainer
        ResponseSource.Throttle -> MaterialTheme.colorScheme.onTertiaryContainer
        ResponseSource.Network -> return
    }
    val label = when (source) {
        ResponseSource.Mock -> stringResource(Res.string.source_mock)
        ResponseSource.Throttle -> stringResource(Res.string.source_throttle)
        ResponseSource.Network -> return
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = textColor,
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 5.dp, vertical = 1.dp),
    )
}

@Preview
@Composable
private fun NetworkLogItemSuccessPreview() {
    MaterialTheme {
        NetworkLogItemContent(
            entry = NetworkLogEntry(
                id = 1,
                url = "https://api.example.com/users/123?include=profile",
                method = "GET",
                responseCode = 200,
                durationMs = 142,
                timestamp = 1710850000000,
                responseBody = """{"name":"John"}""",
            ),
            searchQuery = "",
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun NetworkLogItemErrorPreview() {
    MaterialTheme {
        NetworkLogItemContent(
            entry = NetworkLogEntry(
                id = 2,
                url = "https://api.example.com/auth/login",
                method = "POST",
                responseCode = 401,
                durationMs = 89,
                timestamp = 1710850000000,
            ),
            searchQuery = "",
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun NetworkLogItemInProgressPreview() {
    MaterialTheme {
        NetworkLogItemContent(
            entry = NetworkLogEntry(
                id = 3,
                url = "https://api.example.com/data/sync",
                method = "POST",
                responseCode = NetworkLogEntry.RESPONSE_CODE_IN_PROGRESS,
                timestamp = 1710850000000,
            ),
            searchQuery = "",
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun NetworkLogItemMockedPreview() {
    MaterialTheme {
        NetworkLogItemContent(
            entry = NetworkLogEntry(
                id = 4,
                url = "https://api.example.com/users",
                method = "GET",
                responseCode = 200,
                durationMs = 5,
                timestamp = 1710850000000,
                source = ResponseSource.Mock,
                matchedRuleId = 1,
            ),
            searchQuery = "",
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun NetworkLogItemServerErrorPreview() {
    MaterialTheme {
        NetworkLogItemContent(
            entry = NetworkLogEntry(
                id = 5,
                url = "http://api.example.com/internal/health",
                method = "GET",
                responseCode = 500,
                durationMs = 2034,
                timestamp = 1710850000000,
                responseBody = """{"error":"Internal Server Error"}""",
            ),
            searchQuery = "",
            onClick = {},
        )
    }
}
