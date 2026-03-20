package dev.skymansandy.wiretap.ui.screens.console

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.cash.paging.compose.LazyPagingItems
import app.cash.paging.compose.collectAsLazyPagingItems
import dev.skymansandy.wiretap.data.db.entity.HttpLogEntry
import dev.skymansandy.wiretap.domain.orchestrator.WiretapOrchestrator
import dev.skymansandy.wiretap.domain.repository.RuleRepository
import dev.skymansandy.wiretap.resources.Res
import dev.skymansandy.wiretap.resources.back
import dev.skymansandy.wiretap.resources.clear_http_logs
import dev.skymansandy.wiretap.resources.clear_websocket_logs
import dev.skymansandy.wiretap.resources.close_search
import dev.skymansandy.wiretap.resources.search
import dev.skymansandy.wiretap.resources.tab_http
import dev.skymansandy.wiretap.resources.tab_logs
import dev.skymansandy.wiretap.resources.tab_rules
import dev.skymansandy.wiretap.resources.tab_websocket
import dev.skymansandy.wiretap.resources.wiretap_console
import dev.skymansandy.wiretap.ui.screens.WiretapRoute
import dev.skymansandy.wiretap.ui.common.SearchField
import dev.skymansandy.wiretap.ui.screens.console.http.components.HttpLogList
import dev.skymansandy.wiretap.ui.rules.RulesListScreen
import dev.skymansandy.wiretap.ui.socket.SocketLogList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

private const val TAB_HTTP = 0
private const val TAB_WEBSOCKET = 1

private const val HTTP_SUB_TAB_LOGS = 0
private const val HTTP_SUB_TAB_RULES = 1

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WiretapHomeScreen(
    onBack: () -> Unit,
    orchestrator: WiretapOrchestrator,
    ruleRepository: RuleRepository,
    onNavigate: (WiretapRoute) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(TAB_HTTP) }
    var httpSubTab by remember { mutableIntStateOf(HTTP_SUB_TAB_LOGS) }
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

    val socketLogs by remember {
        orchestrator.getAllSocketLogs().map { logs ->
            if (debouncedQuery.isEmpty()) logs
            else logs.filter { it.url.contains(debouncedQuery, ignoreCase = true) || it.status.name.contains(debouncedQuery, ignoreCase = true) }
        }
    }.collectAsStateWithLifecycle(emptyList())

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
                    if (selectedTab == TAB_HTTP && httpSubTab == HTTP_SUB_TAB_LOGS) {
                        IconButton(onClick = { scope.launch { orchestrator.clearLogs() } }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = stringResource(Res.string.clear_http_logs))
                        }
                    }
                    if (selectedTab == TAB_WEBSOCKET && socketLogs.isNotEmpty()) {
                        IconButton(onClick = { scope.launch { orchestrator.clearSocketLogs() } }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = stringResource(Res.string.clear_websocket_logs))
                        }
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == TAB_HTTP,
                    onClick = {
                        selectedTab = TAB_HTTP
                        searchQuery = ""
                    },
                    icon = { Icon(Icons.Default.Http, contentDescription = null) },
                    label = { Text(stringResource(Res.string.tab_http)) },
                )
                NavigationBarItem(
                    selected = selectedTab == TAB_WEBSOCKET,
                    onClick = {
                        selectedTab = TAB_WEBSOCKET
                        searchQuery = ""
                    },
                    icon = { Icon(Icons.Default.Wifi, contentDescription = null) },
                    label = { Text(stringResource(Res.string.tab_websocket)) },
                )
            }
        },
    ) { padding ->
        when (selectedTab) {
            TAB_HTTP -> Column(
                modifier = Modifier.fillMaxSize().padding(padding),
            ) {

                SecondaryTabRow(
                    selectedTabIndex = httpSubTab,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Tab(
                        selected = httpSubTab == HTTP_SUB_TAB_LOGS,
                        onClick = {
                            httpSubTab = HTTP_SUB_TAB_LOGS
                            searchQuery = ""
                        },
                        text = { Text(stringResource(Res.string.tab_logs)) },
                    )
                    Tab(
                        selected = httpSubTab == HTTP_SUB_TAB_RULES,
                        onClick = {
                            httpSubTab = HTTP_SUB_TAB_RULES
                            searchQuery = ""
                        },
                        text = { Text(stringResource(Res.string.tab_rules)) },
                    )
                }

                when (httpSubTab) {
                    HTTP_SUB_TAB_LOGS -> HttpLogList(
                        lazyItems = lazyItems,
                        searchQuery = searchQuery,
                        onHttpClick = { onNavigate(WiretapRoute.HttpDetail(it)) },
                        onCreateRule = { onNavigate(WiretapRoute.CreateRule(prefillFromLog = it)) },
                        onViewRule = { ruleId ->
                            scope.launch {
                                val rule = ruleRepository.getById(ruleId)
                                if (rule != null) onNavigate(WiretapRoute.RuleDetail(rule))
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                    )

                    HTTP_SUB_TAB_RULES -> RulesListScreen(
                        ruleRepository = ruleRepository,
                        searchQuery = debouncedQuery,
                        onRuleClick = { onNavigate(WiretapRoute.RuleDetail(it)) },
                        onCreateClick = { onNavigate(WiretapRoute.CreateRule()) },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            TAB_WEBSOCKET -> SocketLogList(
                socketLogs = socketLogs,
                searchQuery = searchQuery,
                onSocketClick = { onNavigate(WiretapRoute.SocketDetail(it.id)) },
                modifier = Modifier.fillMaxSize().padding(padding),
            )
        }
    }
}

@Composable
private fun rememberPagedLogs(
    orchestrator: WiretapOrchestrator,
    query: String,
): LazyPagingItems<HttpLogEntry> {
    val flow = remember(query) { orchestrator.getPagedLogs(query) }
    return flow.collectAsLazyPagingItems()
}
