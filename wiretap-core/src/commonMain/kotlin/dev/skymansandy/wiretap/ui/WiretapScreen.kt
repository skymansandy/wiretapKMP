package dev.skymansandy.wiretap.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Rule
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import app.cash.paging.compose.LazyPagingItems
import app.cash.paging.compose.collectAsLazyPagingItems
import dev.skymansandy.wiretap.data.db.entity.NetworkLogEntry
import dev.skymansandy.wiretap.data.db.entity.WiretapRule
import dev.skymansandy.wiretap.di.WiretapDi
import dev.skymansandy.wiretap.domain.orchestrator.WiretapOrchestrator
import dev.skymansandy.wiretap.domain.repository.RuleRepository
import dev.skymansandy.wiretap.ui.components.SearchField
import dev.skymansandy.wiretap.ui.http.HttpLogList
import dev.skymansandy.wiretap.ui.network.NetworkLogDetailScreen
import dev.skymansandy.wiretap.ui.rules.CreateRuleScreen
import dev.skymansandy.wiretap.ui.rules.RuleDetailScreen
import dev.skymansandy.wiretap.ui.rules.RulesListScreen
import dev.skymansandy.wiretap.ui.socket.SocketDetailScreen
import dev.skymansandy.wiretap.ui.socket.SocketLogList
import dev.skymansandy.wiretap_core.generated.resources.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WiretapScreen(
    onBack: () -> Unit,
    orchestrator: WiretapOrchestrator = WiretapDi.orchestrator,
    ruleRepository: RuleRepository = WiretapDi.ruleRepository,
    initialSocketId: Long? = null,
    onInitialSocketConsumed: () -> Unit = {},
) {
    var selectedLog by remember { mutableStateOf<NetworkLogEntry?>(null) }
    var selectedSocketId by remember { mutableStateOf<Long?>(null) }

    // Handle deep-link to socket detail
    LaunchedEffect(initialSocketId) {
        if (initialSocketId != null) {
            selectedSocketId = initialSocketId
            onInitialSocketConsumed()
        }
    }
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
            onEditClick = {
                editRule = selectedRule
                selectedRule = null
            },
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
        if (searchQuery.isEmpty()) {
            value = ""
        } else {
            delay(450)
            value = searchQuery
        }
    }

    val lazyItems = rememberPagedLogs(orchestrator, debouncedQuery)

    // Collect socket logs for WebSocket tab
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
                        Text(stringResource(Res.string.wiretap_console))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isSearchActive) {
                            isSearchActive = false
                            searchQuery = ""
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.back))
                    }
                },
                actions = {
                    if (isSearchActive) {
                        IconButton(onClick = {
                            isSearchActive = false
                            searchQuery = ""
                        }) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(Res.string.close_search))
                        }
                    } else {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(Icons.Default.Search, contentDescription = stringResource(Res.string.search))
                        }
                    }
                    if (selectedTab == 0) {
                        IconButton(onClick = { orchestrator.clearLogs() }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = stringResource(Res.string.clear_http_logs))
                        }
                    }
                    if (selectedTab == 1 && socketLogs.isNotEmpty()) {
                        IconButton(onClick = { orchestrator.clearSocketLogs() }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = stringResource(Res.string.clear_websocket_logs))
                        }
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        searchQuery = ""
                    },
                    icon = { Icon(Icons.Default.SwapVert, contentDescription = null) },
                    label = { Text(stringResource(Res.string.tab_http)) },
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        searchQuery = ""
                    },
                    icon = { Icon(Icons.Default.Wifi, contentDescription = null) },
                    label = { Text(stringResource(Res.string.tab_websocket)) },
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        searchQuery = ""
                    },
                    icon = { Icon(Icons.AutoMirrored.Filled.Rule, contentDescription = null) },
                    label = { Text(stringResource(Res.string.tab_rules)) },
                )
            }
        },
    ) { padding ->
        when (selectedTab) {
            0 -> HttpLogList(
                lazyItems = lazyItems,
                searchQuery = searchQuery,
                onHttpClick = { selectedLog = it },
                onCreateRule = { createRuleFromLog = it },
                onViewRule = { ruleId ->
                    val rule = ruleRepository.getById(ruleId)
                    if (rule != null) selectedRule = rule
                },
                modifier = Modifier.fillMaxSize().padding(padding),
            )

            1 -> SocketLogList(
                socketLogs = socketLogs,
                searchQuery = searchQuery,
                onSocketClick = { selectedSocketId = it.id },
                modifier = Modifier.fillMaxSize().padding(padding),
            )

            2 -> RulesListScreen(
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
