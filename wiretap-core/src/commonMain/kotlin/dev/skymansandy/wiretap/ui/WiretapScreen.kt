package dev.skymansandy.wiretap.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.cash.paging.LoadStateError
import app.cash.paging.LoadStateLoading
import app.cash.paging.LoadStateNotLoading
import app.cash.paging.compose.LazyPagingItems
import app.cash.paging.compose.collectAsLazyPagingItems
import app.cash.paging.compose.itemKey
import dev.skymansandy.wiretap.data.db.entity.NetworkLogEntry
import dev.skymansandy.wiretap.data.db.entity.WiretapRule
import dev.skymansandy.wiretap.domain.model.ResponseSource
import dev.skymansandy.wiretap.domain.orchestrator.WiretapOrchestrator
import dev.skymansandy.wiretap.domain.repository.RuleRepository
import dev.skymansandy.wiretap.ui.network.NetworkLogDetailScreen
import dev.skymansandy.wiretap.ui.network.highlightText
import dev.skymansandy.wiretap.ui.rules.CreateRuleScreen
import dev.skymansandy.wiretap.ui.rules.RuleDetailScreen
import dev.skymansandy.wiretap.ui.rules.RulesListScreen
import dev.skymansandy.wiretap.util.formatTime
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WiretapScreen(
    onBack: () -> Unit,
    orchestrator: WiretapOrchestrator = koinInject(),
    ruleRepository: RuleRepository = koinInject(),
) {
    var selectedLog by remember { mutableStateOf<NetworkLogEntry?>(null) }
    var selectedRule by remember { mutableStateOf<WiretapRule?>(null) }
    var showCreateRule by remember { mutableStateOf(false) }
    var editRule by remember { mutableStateOf<WiretapRule?>(null) }

    if (selectedLog != null) {
        NetworkLogDetailScreen(
            entry = selectedLog!!,
            onBack = { selectedLog = null },
            onViewRule = { ruleId ->
                val rule = ruleRepository.getById(ruleId)
                if (rule != null) {
                    selectedLog = null
                    selectedRule = rule
                }
            },
        )
        return
    }

    if (selectedRule != null) {
        RuleDetailScreen(
            rule = selectedRule!!,
            ruleRepository = ruleRepository,
            onBack = { selectedRule = null },
            onDeleted = { selectedRule = null },
            onEditClick = { editRule = selectedRule; selectedRule = null },
        )
        return
    }

    if (editRule != null) {
        CreateRuleScreen(
            ruleRepository = ruleRepository,
            onBack = { editRule = null },
            onSaved = { editRule = null },
            existingRule = editRule,
        )
        return
    }

    if (showCreateRule) {
        CreateRuleScreen(
            ruleRepository = ruleRepository,
            onBack = { showCreateRule = false },
            onSaved = { showCreateRule = false },
        )
        return
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val searchFocusRequester = remember { FocusRequester() }

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) searchFocusRequester.requestFocus()
    }

    val debouncedQuery by produceState(initialValue = "", key1 = searchQuery) {
        if (searchQuery.isEmpty()) value = "" else {
            delay(450); value = searchQuery
        }
    }

    val lazyItems = rememberPagedLogs(orchestrator, debouncedQuery)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive) {
                        SearchField(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            focusRequester = searchFocusRequester,
                        )
                    } else {
                        Text("Wiretap")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isSearchActive) {
                            isSearchActive = false
                            searchQuery = ""
                        } else onBack()
                    }) {
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
                    if (selectedTab == 0) {
                        IconButton(onClick = { orchestrator.clearLogs() }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear logs")
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0; searchQuery = "" },
                    text = { Text("Activity") },
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1; searchQuery = "" },
                    text = { Text("Rules") },
                )
            }

            when (selectedTab) {
                0 -> LogList(
                    lazyItems = lazyItems,
                    searchQuery = searchQuery,
                    onItemClick = { selectedLog = it },
                    modifier = Modifier.fillMaxSize(),
                )

                1 -> RulesListScreen(
                    ruleRepository = ruleRepository,
                    searchQuery = debouncedQuery,
                    onRuleClick = { selectedRule = it },
                    onCreateClick = { showCreateRule = true },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
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
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    focusRequester: FocusRequester,
) {
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = LocalContentColor.current),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
        modifier = Modifier.focusRequester(focusRequester),
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

    val isHttps = entry.url.startsWith("https://", ignoreCase = true)
    val withoutScheme = entry.url.substringAfter("://")
    val host = withoutScheme.substringBefore("/").substringBefore("?")
    val path = withoutScheme.removePrefix(host).ifEmpty { "/" }

    val responseBytes = entry.responseBody?.encodeToByteArray()?.size ?: 0
    val formattedSize = when {
        responseBytes >= 1_048_576 -> "${"%.1f".format(responseBytes / 1_048_576f)} MB"
        responseBytes >= 1_024 -> "${"%.1f".format(responseBytes / 1_024f)} kB"
        responseBytes > 0 -> "$responseBytes B"
        else -> null
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = entry.responseCode.toString(),
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
                if (entry.source != ResponseSource.NETWORK) {
                    SourceChip(entry.source)
                }
            }
        }
    }
    HorizontalDivider()
}

@Composable
private fun SourceChip(source: ResponseSource) {
    val (bgColor, textColor, label) = when (source) {
        ResponseSource.MOCK -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            "Mock",
        )
        ResponseSource.THROTTLE -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            "Throttle",
        )
        ResponseSource.NETWORK -> return
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
