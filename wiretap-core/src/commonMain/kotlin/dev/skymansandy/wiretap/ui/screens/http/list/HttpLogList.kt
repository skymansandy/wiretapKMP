package dev.skymansandy.wiretap.ui.screens.http.list

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.dp
import app.cash.paging.LoadStateError
import app.cash.paging.LoadStateLoading
import app.cash.paging.LoadStateNotLoading
import app.cash.paging.compose.LazyPagingItems
import app.cash.paging.compose.collectAsLazyPagingItems
import app.cash.paging.compose.itemKey
import dev.skymansandy.wiretap.domain.model.HttpLog
import dev.skymansandy.wiretap.navigation.api.WiretapScreen
import dev.skymansandy.wiretap.navigation.compose.LocalWiretapNavigator
import dev.skymansandy.wiretap.ui.common.LoaderView
import dev.skymansandy.wiretap.ui.common.ScrollToTopButton
import dev.skymansandy.wiretap.ui.common.StatusText
import dev.skymansandy.wiretap.ui.screens.http.components.SwipeableHttpLogItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@Composable
internal fun HttpLogList(
    modifier: Modifier = Modifier,
    viewModel: HttpLogListViewModel,
    searchQuery: String,
    onDismissSearch: () -> Unit,
) {
    val lazyItems = viewModel.pagedLogs.collectAsLazyPagingItems()
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
            delay(3.seconds)
            revealedItemId = null
        }
    }

    val isEmpty = lazyItems.itemCount == 0

    when (lazyItems.loadState.refresh) {
        is LoadStateLoading if isEmpty -> {
            LoaderView(modifier)
        }

        is LoadStateNotLoading if isEmpty -> {
            StatusText(
                modifier = modifier,
                text = when {
                    searchQuery.isBlank() -> "No results found"
                    else -> "No HTTP logs yet"
                },
            )
        }

        is LoadStateError if isEmpty -> {
            StatusText(
                modifier = modifier,
                text = "Failed to load logs",
            )
        }

        else -> ScrollToTopButton(
            listState = listState,
            modifier = modifier,
        ) {
            HttpLogListView(
                modifier = Modifier.fillMaxSize(),
                listState = listState,
                lazyItems = lazyItems,
                searchQuery = searchQuery,
                revealedItemId = revealedItemId,
                onRevealedItemIdChange = { revealedItemId = it },
                onDismissSearch = onDismissSearch,
            )
        }
    }
}

@Composable
private fun HttpLogListView(
    modifier: Modifier,
    listState: LazyListState,
    lazyItems: LazyPagingItems<HttpLog>,
    searchQuery: String,
    revealedItemId: String?,
    onRevealedItemIdChange: (String?) -> Unit,
    onDismissSearch: () -> Unit,
) {
    val navigator = LocalWiretapNavigator.current

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
                    onDismissSearch()
                    onRevealedItemIdChange(null)
                    navigator.pushDetailPane(
                        WiretapScreen.HttpDetailScreen(entry.id),
                    )
                },
                onCreateRule = {
                    onRevealedItemIdChange(null)
                    navigator.pushDetailPane(
                        WiretapScreen.CreateRuleScreen(prefillFromLogId = entry.id),
                    )
                },
                onViewRule = {
                    onRevealedItemIdChange(null)
                    entry.matchedRuleId?.let { ruleId ->
                        navigator.pushDetailPane(
                            WiretapScreen.RuleDetailScreen(ruleId),
                        )
                    }
                },
            )
        }

        if (lazyItems.loadState.append is LoadStateLoading) {
            item {
                LoaderView(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    loaderSize = 24.dp,
                )
            }
        }
    }
}
