package dev.skymansandy.wiretap.ui.screens.console.http.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.dp
import app.cash.paging.LoadStateError
import app.cash.paging.LoadStateLoading
import app.cash.paging.LoadStateNotLoading
import app.cash.paging.compose.LazyPagingItems
import app.cash.paging.compose.itemKey
import dev.skymansandy.wiretap.data.db.entity.HttpLogEntry
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
internal fun HttpLogList(
    modifier: Modifier = Modifier,
    lazyItems: LazyPagingItems<HttpLogEntry>,
    searchQuery: String,
    onHttpClick: (HttpLogEntry) -> Unit,
    onCreateRule: (HttpLogEntry) -> Unit,
    onViewRule: (Long) -> Unit,
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val isAtTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
        }
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

    val isEmpty = lazyItems.itemCount == 0

    when (lazyItems.loadState.refresh) {
        is LoadStateLoading if isEmpty -> {
            CenteredBox(modifier) { CircularProgressIndicator() }
        }

        is LoadStateNotLoading if isEmpty -> {
            CenteredBox(modifier) { StatusText("No HTTP logs yet") }
        }

        is LoadStateError if isEmpty -> {
            CenteredBox(modifier) { StatusText("Failed to load logs") }
        }

        else -> {
            HttpLogColumn(
                modifier = modifier,
                listState = listState,
                lazyItems = lazyItems,
                searchQuery = searchQuery,
                revealedItemId = revealedItemId,
                onRevealedItemIdChange = { revealedItemId = it },
                onHttpClick = onHttpClick,
                onCreateRule = onCreateRule,
                onViewRule = onViewRule,
            )
        }
    }
}

@Composable
private fun CenteredBox(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
        content = { content() },
    )
}

@Composable
private fun StatusText(text: String) {

    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
    )
}

@Composable
private fun HttpLogColumn(
    modifier: Modifier,
    listState: androidx.compose.foundation.lazy.LazyListState,
    lazyItems: LazyPagingItems<HttpLogEntry>,
    searchQuery: String,
    revealedItemId: String?,
    onRevealedItemIdChange: (String?) -> Unit,
    onHttpClick: (HttpLogEntry) -> Unit,
    onCreateRule: (HttpLogEntry) -> Unit,
    onViewRule: (Long) -> Unit,
) {
    LazyColumn(
        modifier = modifier.clipToBounds(),
        state = listState,
    ) {
        items(
            count = lazyItems.itemCount,
            key = lazyItems.itemKey { it.id },
        ) { index ->
            val entry = lazyItems[index] ?: return@items
            val itemKey = "http_${entry.id}"
            SwipeableHttpLogItem(
                entry = entry,
                searchQuery = searchQuery,
                isRevealed = revealedItemId == itemKey,
                onReveal = { onRevealedItemIdChange(itemKey) },
                onCollapse = { if (revealedItemId == itemKey) onRevealedItemIdChange(null) },
                onClick = {
                    onRevealedItemIdChange(null)
                    onHttpClick(entry)
                },
                onCreateRule = {
                    onRevealedItemIdChange(null)
                    onCreateRule(entry)
                },
                onViewRule = {
                    onRevealedItemIdChange(null)
                    entry.matchedRuleId?.let(onViewRule)
                },
            )
        }

        if (lazyItems.loadState.append is LoadStateLoading) {
            item {
                CenteredBox(Modifier.fillMaxWidth().padding(16.dp)) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
    }
}
