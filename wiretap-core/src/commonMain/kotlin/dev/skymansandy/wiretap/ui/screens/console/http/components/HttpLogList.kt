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

    when {
        lazyItems.loadState.refresh is LoadStateLoading && lazyItems.itemCount == 0 -> {
            Box(
                modifier = modifier,
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        lazyItems.loadState.refresh is LoadStateNotLoading && lazyItems.itemCount == 0 -> {
            Box(
                modifier = modifier,
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No HTTP logs yet",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        lazyItems.loadState.refresh is LoadStateError && lazyItems.itemCount == 0 -> {
            Box(
                modifier = modifier,
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Failed to load logs",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        else -> {
            LazyColumn(
                modifier = modifier,
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
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
