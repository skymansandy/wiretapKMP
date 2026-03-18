package dev.skymansandy.wiretap.ui

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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Rule
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import app.cash.paging.LoadStateError
import app.cash.paging.LoadStateLoading
import app.cash.paging.LoadStateNotLoading
import app.cash.paging.compose.LazyPagingItems
import app.cash.paging.compose.collectAsLazyPagingItems
import app.cash.paging.compose.itemKey
import dev.skymansandy.wiretap.data.db.entity.ActivityEntry
import dev.skymansandy.wiretap.data.db.entity.NetworkLogEntry
import dev.skymansandy.wiretap.data.db.entity.SocketLogEntry
import dev.skymansandy.wiretap.data.db.entity.WiretapRule
import dev.skymansandy.wiretap.di.WiretapDi
import dev.skymansandy.wiretap.domain.model.ResponseSource
import dev.skymansandy.wiretap.domain.model.SocketStatus
import dev.skymansandy.wiretap.domain.orchestrator.WiretapOrchestrator
import dev.skymansandy.wiretap.domain.repository.RuleRepository
import dev.skymansandy.wiretap.ui.network.NetworkLogDetailScreen
import dev.skymansandy.wiretap.ui.network.highlightText
import dev.skymansandy.wiretap.ui.rules.CreateRuleScreen
import dev.skymansandy.wiretap.ui.rules.RuleDetailScreen
import dev.skymansandy.wiretap.ui.rules.RulesListScreen
import dev.skymansandy.wiretap.ui.socket.SocketDetailScreen
import dev.skymansandy.wiretap.util.formatTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private fun formatOneDecimal(value: Float): String {
    val intPart = value.toLong()
    val decPart = ((value - intPart) * 10).toInt().let { kotlin.math.abs(it) }
    return "$intPart.$decPart"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WiretapScreen(
    onBack: () -> Unit,
    orchestrator: WiretapOrchestrator = WiretapDi.orchestrator,
    ruleRepository: RuleRepository = WiretapDi.ruleRepository,
) {
    var selectedLog by remember { mutableStateOf<NetworkLogEntry?>(null) }
    var selectedSocketId by remember { mutableStateOf<Long?>(null) }
    var selectedRule by remember { mutableStateOf<WiretapRule?>(null) }
    var showCreateRule by remember { mutableStateOf(false) }
    var editRule by remember { mutableStateOf<WiretapRule?>(null) }
    var createRuleFromLog by remember { mutableStateOf<NetworkLogEntry?>(null) }

    if (selectedSocketId != null) {
        SocketDetailScreen(
            socketId = selectedSocketId!!,
            orchestrator = orchestrator,
            onBack = { selectedSocketId = null },
        )
        return
    }

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

    if (createRuleFromLog != null) {
        CreateRuleScreen(
            ruleRepository = ruleRepository,
            onBack = { createRuleFromLog = null },
            onSaved = { createRuleFromLog = null },
            prefillFromLog = createRuleFromLog,
            onEditConflictingRule = { rule ->
                createRuleFromLog = null
                editRule = rule
            },
        )
        return
    }

    if (showCreateRule) {
        CreateRuleScreen(
            ruleRepository = ruleRepository,
            onBack = { showCreateRule = false },
            onSaved = { showCreateRule = false },
            onEditConflictingRule = { rule ->
                showCreateRule = false
                editRule = rule
            },
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

    // Collect socket logs for merging into activity list
    val socketLogs by remember {
        orchestrator.getAllSocketLogs().map { logs ->
            if (debouncedQuery.isEmpty()) logs
            else logs.filter { it.url.contains(debouncedQuery, ignoreCase = true) || it.status.name.contains(debouncedQuery, ignoreCase = true) }
        }
    }.collectAsState(emptyList())

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
                        Text("Wiretap Console")
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
                        IconButton(onClick = {
                            orchestrator.clearLogs()
                            orchestrator.clearSocketLogs()
                        }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear logs")
                        }
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0; searchQuery = "" },
                    icon = { Icon(Icons.Default.SwapVert, contentDescription = null) },
                    label = { Text("Activity") },
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1; searchQuery = "" },
                    icon = { Icon(Icons.AutoMirrored.Filled.Rule, contentDescription = null) },
                    label = { Text("Rules") },
                )
            }
        },
    ) { padding ->
        when (selectedTab) {
            0 -> ActivityList(
                lazyItems = lazyItems,
                socketLogs = socketLogs,
                searchQuery = searchQuery,
                onHttpClick = { selectedLog = it },
                onSocketClick = { selectedSocketId = it.id },
                onCreateRule = { createRuleFromLog = it },
                onViewRule = { ruleId ->
                    val rule = ruleRepository.getById(ruleId)
                    if (rule != null) selectedRule = rule
                },
                modifier = Modifier.fillMaxSize().padding(padding),
            )

            1 -> RulesListScreen(
                ruleRepository = ruleRepository,
                searchQuery = debouncedQuery,
                onRuleClick = { selectedRule = it },
                onCreateClick = { showCreateRule = true },
                modifier = Modifier.fillMaxSize().padding(padding),
            )
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
private fun ActivityList(
    lazyItems: LazyPagingItems<NetworkLogEntry>,
    socketLogs: List<SocketLogEntry>,
    searchQuery: String,
    onHttpClick: (NetworkLogEntry) -> Unit,
    onSocketClick: (SocketLogEntry) -> Unit,
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

        lazyItems.loadState.refresh is LoadStateNotLoading && lazyItems.itemCount == 0 && socketLogs.isEmpty() -> {
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
            // Merge HTTP paged items and socket logs by timestamp (newest first)
            // Socket logs are collected as full list; HTTP items come from paging
            val mergedItems = remember(lazyItems.itemCount, socketLogs) {
                val httpItems = (0 until lazyItems.itemCount).mapNotNull { index ->
                    lazyItems.peek(index)?.let { ActivityEntry.Http(it) }
                }
                val socketItems = socketLogs.map { ActivityEntry.Socket(it) }
                (httpItems + socketItems).sortedByDescending { it.timestamp }
            }

            val listState = rememberLazyListState()
            val scope = rememberCoroutineScope()
            val isAtTop = remember(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
                listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
            }
            var lastItemCount by remember { mutableIntStateOf(mergedItems.size) }
            var revealedItemId by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(mergedItems.size) {
                if (mergedItems.size > lastItemCount && isAtTop) {
                    scope.launch { listState.scrollToItem(0) }
                }
                lastItemCount = mergedItems.size
            }

            LaunchedEffect(revealedItemId) {
                if (revealedItemId != null) {
                    delay(3000)
                    revealedItemId = null
                }
            }

            LazyColumn(state = listState, modifier = modifier) {
                items(
                    count = mergedItems.size,
                    key = { index ->
                        when (val item = mergedItems[index]) {
                            is ActivityEntry.Http -> "http_${item.entry.id}"
                            is ActivityEntry.Socket -> "ws_${item.entry.id}"
                        }
                    },
                ) { index ->
                    when (val item = mergedItems[index]) {
                        is ActivityEntry.Http -> {
                            val entry = item.entry
                            val itemKey = "http_${entry.id}"
                            SwipeableNetworkLogItem(
                                entry = entry,
                                searchQuery = searchQuery,
                                isRevealed = revealedItemId == itemKey,
                                onReveal = { revealedItemId = itemKey },
                                onCollapse = { if (revealedItemId == itemKey) revealedItemId = null },
                                onClick = { revealedItemId = null; onHttpClick(entry) },
                                onCreateRule = { revealedItemId = null; onCreateRule(entry) },
                                onViewRule = { revealedItemId = null; entry.matchedRuleId?.let(onViewRule) },
                            )
                        }
                        is ActivityEntry.Socket -> {
                            SocketLogItemContent(
                                entry = item.entry,
                                searchQuery = searchQuery,
                                onClick = { onSocketClick(item.entry) },
                            )
                        }
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
private fun SocketLogItemContent(
    entry: SocketLogEntry,
    searchQuery: String,
    onClick: () -> Unit,
) {
    val statusColor = when (entry.status) {
        SocketStatus.CONNECTING -> Color(0xFF42A5F5) // Blue
        SocketStatus.OPEN -> Color(0xFF4CAF50) // Green
        SocketStatus.CLOSING -> Color(0xFFFFA726) // Amber
        SocketStatus.CLOSED -> Color(0xFF9E9E9E) // Gray
        SocketStatus.FAILED -> Color(0xFFEF5350) // Red
    }

    val isSecure = entry.url.startsWith("wss://", ignoreCase = true)
    val withoutScheme = entry.url.substringAfter("://")
    val host = withoutScheme.substringBefore("/").substringBefore("?")
    val path = withoutScheme.removePrefix(host).ifEmpty { "/" }

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
                text = "WS",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = statusColor,
                modifier = Modifier.width(44.dp),
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = highlightText(path, searchQuery),
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
                    if (entry.messageCount > 0) {
                        Text(
                            text = "${entry.messageCount} msgs",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    SocketStatusChip(entry.status)
                }
            }
        }
        HorizontalDivider()
    }
}

@Composable
private fun SocketStatusChip(status: SocketStatus) {
    val (bgColor, label) = when (status) {
        SocketStatus.CONNECTING -> Color(0xFF42A5F5) to "Connecting"
        SocketStatus.OPEN -> Color(0xFF4CAF50) to "Open"
        SocketStatus.CLOSING -> Color(0xFFFFA726) to "Closing"
        SocketStatus.CLOSED -> Color(0xFF9E9E9E) to "Closed"
        SocketStatus.FAILED -> Color(0xFFEF5350) to "Failed"
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = Color.White,
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 5.dp, vertical = 1.dp),
    )
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
                            "Search\u2026",
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
    val hasMatchedRule = entry.source != ResponseSource.NETWORK && entry.matchedRuleId != null
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
                    text = if (hasMatchedRule) "View\nRule" else "Create\nRule",
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
                    if (entry.source != ResponseSource.NETWORK) {
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
