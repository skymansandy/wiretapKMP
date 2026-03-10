package dev.skymansandy.wiretap.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.cash.paging.LoadStateError
import app.cash.paging.LoadStateLoading
import app.cash.paging.LoadStateNotLoading
import app.cash.paging.compose.LazyPagingItems
import app.cash.paging.compose.collectAsLazyPagingItems
import app.cash.paging.compose.itemKey
import dev.skymansandy.wiretap.model.NetworkLogEntry
import dev.skymansandy.wiretap.model.ResponseSource
import dev.skymansandy.wiretap.orchestrator.WiretapOrchestrator
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WiretapScreen(
    onBack: () -> Unit,
    orchestrator: WiretapOrchestrator = koinInject(),
) {
    var selectedLog by remember { mutableStateOf<NetworkLogEntry?>(null) }

    if (selectedLog != null) {
        NetworkLogDetailScreen(
            entry = selectedLog!!,
            onBack = { selectedLog = null },
        )
        return
    }

    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val lazyItems = rememberPagedLogs(orchestrator, searchQuery)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive) {
                        SearchField(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                        )
                    } else {
                        Text("Wiretap")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isSearchActive) {
                        IconButton(onClick = { isSearchActive = false; searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Close search")
                        }
                    } else {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    }
                    IconButton(onClick = { orchestrator.clearLogs() }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear logs")
                    }
                },
            )
        },
    ) { padding ->
        LogList(
            lazyItems = lazyItems,
            searchQuery = searchQuery,
            onItemClick = { selectedLog = it },
            modifier = Modifier.fillMaxSize().padding(padding),
        )
    }
}

@Composable
private fun rememberPagedLogs(
    orchestrator: WiretapOrchestrator,
    query: String,
): LazyPagingItems<NetworkLogEntry> {
    val flow = remember(query) { orchestrator.getPagedLogs(query) }
    return flow.collectAsLazyPagingItems()
}

@Composable
private fun LogList(
    lazyItems: LazyPagingItems<NetworkLogEntry>,
    searchQuery: String,
    onItemClick: (NetworkLogEntry) -> Unit,
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
                Text("No network logs yet", style = MaterialTheme.typography.bodyLarge)
            }
        }
        lazyItems.loadState.refresh is LoadStateError -> {
            Box(modifier, contentAlignment = Alignment.Center) {
                Text("Failed to load logs", style = MaterialTheme.typography.bodyLarge)
            }
        }
        else -> {
            LazyColumn(modifier = modifier) {
                items(
                    count = lazyItems.itemCount,
                    key = lazyItems.itemKey { it.id },
                ) { index ->
                    val entry = lazyItems[index]
                    if (entry != null) {
                        NetworkLogItem(
                            entry = entry,
                            searchQuery = searchQuery,
                            onClick = { onItemClick(entry) },
                        )
                    }
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

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = LocalContentColor.current),
        singleLine = true,
        decorationBox = { innerTextField ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = LocalContentColor.current.copy(alpha = 0.6f),
                )
                Spacer(Modifier.width(8.dp))
                Box {
                    if (query.isEmpty()) {
                        Text(
                            "Search…",
                            style = MaterialTheme.typography.bodyLarge,
                            color = LocalContentColor.current.copy(alpha = 0.4f),
                        )
                    }
                    innerTextField()
                }
            }
        },
    )
}

@Composable
private fun NetworkLogItem(
    entry: NetworkLogEntry,
    searchQuery: String,
    onClick: () -> Unit,
) {
    val statusColor = when {
        entry.responseCode in 200..299 -> MaterialTheme.colorScheme.primary
        entry.responseCode in 300..399 -> MaterialTheme.colorScheme.tertiary
        entry.responseCode in 400..499 -> MaterialTheme.colorScheme.error
        entry.responseCode >= 500 -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }

    val sourceLabel = when (entry.source) {
        ResponseSource.MOCK -> " MOCK"
        ResponseSource.THROTTLE -> " THROTTLE"
        ResponseSource.NETWORK -> ""
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = highlightText(entry.method, searchQuery),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = highlightText(entry.responseCode.toString() + sourceLabel, searchQuery),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = statusColor,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "${entry.durationMs}ms",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = highlightText(entry.url, searchQuery),
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    HorizontalDivider()
}
