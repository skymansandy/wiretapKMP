package dev.skymansandy.wiretap.ui.screens.console

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.cash.paging.compose.collectAsLazyPagingItems
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
import dev.skymansandy.wiretap.ui.common.LocalWideScreen
import dev.skymansandy.wiretap.ui.common.SearchField
import dev.skymansandy.wiretap.ui.rules.RulesListScreen
import dev.skymansandy.wiretap.ui.screens.WiretapRoute
import dev.skymansandy.wiretap.ui.screens.console.WiretapHomeViewModel.Companion.HTTP_SUB_TAB_LOGS
import dev.skymansandy.wiretap.ui.screens.console.WiretapHomeViewModel.Companion.HTTP_SUB_TAB_RULES
import dev.skymansandy.wiretap.ui.screens.console.WiretapHomeViewModel.Companion.TAB_HTTP
import dev.skymansandy.wiretap.ui.screens.console.WiretapHomeViewModel.Companion.TAB_WEBSOCKET
import dev.skymansandy.wiretap.ui.screens.console.http.components.HttpLogList
import dev.skymansandy.wiretap.ui.socket.SocketLogList
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WiretapHomeScreen(
    viewModel: WiretapHomeViewModel,
    ruleRepository: RuleRepository,
    onBack: () -> Unit,
    onNavigate: (WiretapRoute?) -> Unit,
    modifier: Modifier = Modifier,
) {

    val isWideScreen = LocalWideScreen.current
    val scope = rememberCoroutineScope()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val httpSubTab by viewModel.httpSubTab.collectAsStateWithLifecycle()
    val isSearchActive by viewModel.isSearchActive.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val debouncedQuery by viewModel.debouncedQuery.collectAsStateWithLifecycle()
    val socketLogs by viewModel.socketLogs.collectAsStateWithLifecycle()
    val lazyItems = viewModel.pagedLogs.collectAsLazyPagingItems()
    val searchFocusRequester = remember { FocusRequester() }

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            searchFocusRequester.requestFocus()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive) {
                        SearchField(
                            query = searchQuery,
                            onQueryChange = { viewModel.updateSearchQuery(it) },
                            focusRequester = searchFocusRequester,
                        )
                    } else {
                        Text(stringResource(Res.string.wiretap_console))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isSearchActive) {
                            viewModel.setSearchActive(false)
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.back))
                    }
                },
                actions = {
                    if (isSearchActive) {
                        IconButton(onClick = { viewModel.setSearchActive(false) }) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(Res.string.close_search))
                        }
                    } else {
                        IconButton(onClick = { viewModel.setSearchActive(true) }) {
                            Icon(Icons.Default.Search, contentDescription = stringResource(Res.string.search))
                        }
                    }
                    if (selectedTab == TAB_HTTP && httpSubTab == HTTP_SUB_TAB_LOGS) {
                        IconButton(onClick = { viewModel.clearHttpLogs() }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = stringResource(Res.string.clear_http_logs))
                        }
                    }
                    if (selectedTab == TAB_WEBSOCKET && socketLogs.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearSocketLogs() }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = stringResource(Res.string.clear_websocket_logs))
                        }
                    }
                },
            )
        },
        bottomBar = {
            if (!isWideScreen) {
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedTab == TAB_HTTP,
                        onClick = { viewModel.selectTab(TAB_HTTP) },
                        icon = { Icon(Icons.Default.Http, contentDescription = null) },
                        label = { Text(stringResource(Res.string.tab_http)) },
                    )
                    NavigationBarItem(
                        selected = selectedTab == TAB_WEBSOCKET,
                        onClick = { viewModel.selectTab(TAB_WEBSOCKET) },
                        icon = { Icon(Icons.Default.Wifi, contentDescription = null) },
                        label = { Text(stringResource(Res.string.tab_websocket)) },
                    )
                }
            }
        },
    ) { padding ->
        val content: @Composable (Modifier) -> Unit = { contentModifier ->
            when (selectedTab) {
                TAB_HTTP -> Column(
                    modifier = contentModifier,
                ) {

                    SecondaryTabRow(
                        selectedTabIndex = httpSubTab,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Tab(
                            selected = httpSubTab == HTTP_SUB_TAB_LOGS,
                            onClick = { viewModel.selectHttpSubTab(HTTP_SUB_TAB_LOGS) },
                            text = { Text(stringResource(Res.string.tab_logs)) },
                        )
                        Tab(
                            selected = httpSubTab == HTTP_SUB_TAB_RULES,
                            onClick = {
                                viewModel.selectHttpSubTab(HTTP_SUB_TAB_RULES)
                                onNavigate(null)
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
                                    val rule = viewModel.getRuleById(ruleId)
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
                    modifier = contentModifier,
                )
            }
        }

        if (isWideScreen) {
            Row(modifier = Modifier.fillMaxSize().padding(padding)) {
                NavigationRail(modifier = Modifier.fillMaxHeight()) {
                    NavigationRailItem(
                        selected = selectedTab == TAB_HTTP,
                        onClick = { viewModel.selectTab(TAB_HTTP) },
                        icon = { Icon(Icons.Default.Http, contentDescription = null) },
                        label = { Text(stringResource(Res.string.tab_http)) },
                    )
                    NavigationRailItem(
                        selected = selectedTab == TAB_WEBSOCKET,
                        onClick = { viewModel.selectTab(TAB_WEBSOCKET) },
                        icon = { Icon(Icons.Default.Wifi, contentDescription = null) },
                        label = { Text(stringResource(Res.string.tab_websocket)) },
                    )
                }
                content(Modifier.weight(1f).fillMaxHeight())
            }
        } else {
            content(Modifier.fillMaxSize().padding(padding))
        }
    }
}
