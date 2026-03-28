package dev.skymansandy.wiretap.ui.screens.console

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.cash.paging.compose.collectAsLazyPagingItems
import dev.skymansandy.wiretap.domain.repository.RuleRepository
import dev.skymansandy.wiretap.navigation.api.WiretapScreen
import dev.skymansandy.wiretap.navigation.compose.LocalWiretapNavigator
import dev.skymansandy.wiretap.ui.common.LocalWideScreen
import dev.skymansandy.wiretap.ui.common.SearchField
import dev.skymansandy.wiretap.ui.rules.RulesListScreen
import dev.skymansandy.wiretap.ui.screens.console.WiretapHomeViewModel.Companion.HTTP_SUB_TAB_LOGS
import dev.skymansandy.wiretap.ui.screens.console.WiretapHomeViewModel.Companion.HTTP_SUB_TAB_RULES
import dev.skymansandy.wiretap.ui.screens.console.WiretapHomeViewModel.Companion.TAB_HTTP
import dev.skymansandy.wiretap.ui.screens.console.WiretapHomeViewModel.Companion.TAB_WEBSOCKET
import dev.skymansandy.wiretap.ui.screens.console.http.components.HttpLogList
import dev.skymansandy.wiretap.ui.socket.SocketLogList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WiretapHomeScreen(
    viewModel: WiretapHomeViewModel,
    ruleRepository: RuleRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val navigator = LocalWiretapNavigator.current
    val isWideScreen = LocalWideScreen.current
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val httpSubTab by viewModel.httpSubTab.collectAsStateWithLifecycle()
    val isSearchActive by viewModel.isSearchActive.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val debouncedQuery by viewModel.debouncedQuery.collectAsStateWithLifecycle()
    val socketLogs by viewModel.socketLogs.collectAsStateWithLifecycle()
    val lazyItems = viewModel.pagedLogs.collectAsLazyPagingItems()
    val searchFocusRequester = remember { FocusRequester() }
    var showClearConfirmation by remember { mutableStateOf<(() -> Unit)?>(null) }
    var isSubTabVisible by remember { mutableStateOf(true) }
    val subTabScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < -1f) {
                    isSubTabVisible = false
                } else if (available.y > 1f) {
                    isSubTabVisible = true
                }
                return Offset.Zero
            }
        }
    }

    // Reset tab visibility when switching tabs
    LaunchedEffect(selectedTab, httpSubTab) {
        isSubTabVisible = true
    }

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            searchFocusRequester.requestFocus()
        }
    }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = if (isWideScreen) {
            ScaffoldDefaults.contentWindowInsets.exclude(
                WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
            )
        } else {
            ScaffoldDefaults.contentWindowInsets
        },
        topBar = {
            WiretapTopBar(
                title = if (selectedTab == TAB_HTTP) "HTTP Console" else "WebSocket Console",
                isSearchActive = isSearchActive,
                searchQuery = searchQuery,
                searchFocusRequester = searchFocusRequester,
                showClearHttpLogs = selectedTab == TAB_HTTP && httpSubTab == HTTP_SUB_TAB_LOGS && lazyItems.itemCount > 0,
                showClearSocketLogs = selectedTab == TAB_WEBSOCKET && socketLogs.isNotEmpty(),
                onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                onSearchActiveChange = { viewModel.setSearchActive(it) },
                onBack = onBack,
                onClearHttpLogs = { showClearConfirmation = { viewModel.clearHttpLogs() } },
                onClearSocketLogs = { showClearConfirmation = { viewModel.clearSocketLogs() } },
            )
        },
        bottomBar = {
            if (!isWideScreen) {
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedTab == TAB_HTTP,
                        onClick = { viewModel.selectTab(TAB_HTTP) },
                        icon = { Icon(Icons.Default.Http, contentDescription = null) },
                        label = { Text("HTTP") },
                    )
                    NavigationBarItem(
                        selected = selectedTab == TAB_WEBSOCKET,
                        onClick = { viewModel.selectTab(TAB_WEBSOCKET) },
                        icon = { Icon(Icons.Default.Wifi, contentDescription = null) },
                        label = { Text("WebSocket") },
                    )
                }
            }
        },
    ) { padding ->
        val content: @Composable (Modifier) -> Unit = { contentModifier ->
            when (selectedTab) {
                TAB_HTTP -> Column(
                    modifier = contentModifier.nestedScroll(subTabScrollConnection),
                ) {
                    AnimatedVisibility(
                        visible = isSubTabVisible,
                        enter = expandVertically(),
                        exit = shrinkVertically(),
                    ) {
                        SecondaryTabRow(
                            selectedTabIndex = httpSubTab,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Tab(
                                selected = httpSubTab == HTTP_SUB_TAB_LOGS,
                                onClick = { viewModel.selectHttpSubTab(HTTP_SUB_TAB_LOGS) },
                                text = { Text("Logs") },
                            )
                            Tab(
                                selected = httpSubTab == HTTP_SUB_TAB_RULES,
                                onClick = { viewModel.selectHttpSubTab(HTTP_SUB_TAB_RULES) },
                                text = { Text("Rules") },
                            )
                        }
                    }

                    when (httpSubTab) {
                        HTTP_SUB_TAB_LOGS -> HttpLogList(
                            lazyItems = lazyItems,
                            searchQuery = searchQuery,
                            onDismissSearch = { viewModel.setSearchActive(false) },
                            onHttpClick = { navigator.pushDetailPane(WiretapScreen.HttpDetailScreen(it.id)) },
                            onCreateRule = { navigator.pushDetailPane(WiretapScreen.CreateRuleScreen(prefillFromLogId = it.id)) },
                            onViewRule = { ruleId -> navigator.pushDetailPane(WiretapScreen.RuleDetailScreen(ruleId)) },
                            modifier = Modifier.fillMaxSize(),
                        )

                        HTTP_SUB_TAB_RULES -> RulesListScreen(
                            ruleRepository = ruleRepository,
                            searchQuery = debouncedQuery,
                            onRuleClick = { navigator.pushDetailPane(WiretapScreen.RuleDetailScreen(it.id)) },
                            onCreateClick = { navigator.pushDetailPane(WiretapScreen.CreateRuleScreen()) },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }

                TAB_WEBSOCKET -> SocketLogList(
                    socketLogs = socketLogs,
                    searchQuery = searchQuery,
                    onDismissSearch = { viewModel.setSearchActive(false) },
                    onSocketClick = { navigator.pushDetailPane(WiretapScreen.SocketDetailScreen(it.id)) },
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
                        label = { Text("HTTP") },
                    )
                    NavigationRailItem(
                        selected = selectedTab == TAB_WEBSOCKET,
                        onClick = { viewModel.selectTab(TAB_WEBSOCKET) },
                        icon = { Icon(Icons.Default.Wifi, contentDescription = null) },
                        label = { Text("WebSocket") },
                    )
                }
                content(Modifier.weight(1f).fillMaxHeight())
            }
        } else {
            content(Modifier.fillMaxSize().padding(padding))
        }
    }

    showClearConfirmation?.let { onConfirm ->
        ClearLogsConfirmationDialog(
            onConfirm = {
                onConfirm()
                showClearConfirmation = null
            },
            onDismiss = { showClearConfirmation = null },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WiretapTopBar(
    title: String,
    isSearchActive: Boolean,
    searchQuery: String,
    searchFocusRequester: FocusRequester,
    showClearHttpLogs: Boolean,
    showClearSocketLogs: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onSearchActiveChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    onClearHttpLogs: () -> Unit,
    onClearSocketLogs: () -> Unit,
) {

    TopAppBar(
        title = {
            if (isSearchActive) {
                SearchField(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange,
                    focusRequester = searchFocusRequester,
                )
            } else {
                Text(title)
            }
        },
        navigationIcon = {
            IconButton(onClick = {
                if (isSearchActive) {
                    onSearchActiveChange(false)
                } else {
                    onBack()
                }
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            if (isSearchActive) {
                IconButton(onClick = { onSearchActiveChange(false) }) {
                    Icon(Icons.Default.Close, contentDescription = "Close search")
                }
            } else {
                IconButton(onClick = { onSearchActiveChange(true) }) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            }
            if (showClearHttpLogs) {
                IconButton(onClick = onClearHttpLogs) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = "Clear HTTP logs")
                }
            }
            if (showClearSocketLogs) {
                IconButton(onClick = onClearSocketLogs) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = "Clear WebSocket logs")
                }
            }
        },
    )
}

@Composable
private fun ClearLogsConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Clear logs") },
        text = { Text("Are you sure you want to clear all logs?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Clear", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
