package dev.skymansandy.wiretap.ui.screens.http.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
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
import dev.skymansandy.wiretap.navigation.api.WiretapScreen
import dev.skymansandy.wiretap.navigation.compose.LocalWiretapNavigator
import dev.skymansandy.wiretap.ui.common.ClearLogsConfirmationDialog
import dev.skymansandy.wiretap.ui.common.WiretapTopBar
import dev.skymansandy.wiretap.ui.model.HttpSubTab
import dev.skymansandy.wiretap.ui.screens.rules.list.RulesListScreen
import dev.skymansandy.wiretap.ui.screens.rules.list.RulesListViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HttpTabScreen(
    onBack: () -> Unit,
    navigationRail: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
    httpLogListViewModel: HttpLogListViewModel = koinViewModel(),
    rulesListViewModel: RulesListViewModel = koinViewModel(),
) {
    val navigator = LocalWiretapNavigator.current
    val hasHttpLogs by httpLogListViewModel.hasLogs.collectAsStateWithLifecycle()

    var httpSubTab by remember { mutableStateOf(HttpSubTab.Logs) }
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val searchFocusRequester = remember { FocusRequester() }
    var showClearConfirmation by remember { mutableStateOf(false) }

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

    // Reset tab visibility when switching sub-tabs
    LaunchedEffect(httpSubTab) {
        isSubTabVisible = true
    }

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            searchFocusRequester.requestFocus()
        }
    }

    // Sync search query to the active sub-ViewModel
    LaunchedEffect(searchQuery, httpSubTab) {
        when (httpSubTab) {
            HttpSubTab.Logs -> httpLogListViewModel.updateSearchQuery(searchQuery)
            HttpSubTab.Rules -> rulesListViewModel.updateSearchQuery(searchQuery)
        }
    }

    Column(modifier = modifier) {
        WiretapTopBar(
            title = "HTTP Console",
            isSearchActive = isSearchActive,
            searchQuery = searchQuery,
            searchFocusRequester = searchFocusRequester,
            showClearAction = httpSubTab == HttpSubTab.Logs && hasHttpLogs,
            onSearchQueryChange = { searchQuery = it },
            onSearchActiveChange = { active ->
                isSearchActive = active
                if (!active) searchQuery = ""
            },
            onBack = onBack,
            onClear = { showClearConfirmation = true },
        )

        Row(modifier = Modifier.weight(1f)) {
            navigationRail?.invoke()

            Column(
                modifier = Modifier.weight(1f).fillMaxHeight().nestedScroll(subTabScrollConnection),
            ) {
                AnimatedVisibility(
                    visible = isSubTabVisible,
                    enter = expandVertically(),
                    exit = shrinkVertically(),
                ) {
                    SecondaryTabRow(
                        selectedTabIndex = httpSubTab.ordinal,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Tab(
                            selected = httpSubTab == HttpSubTab.Logs,
                            onClick = {
                                httpSubTab = HttpSubTab.Logs
                                searchQuery = ""
                            },
                            text = { Text("Logs") },
                        )
                        Tab(
                            selected = httpSubTab == HttpSubTab.Rules,
                            onClick = {
                                httpSubTab = HttpSubTab.Rules
                                searchQuery = ""
                            },
                            text = { Text("Rules") },
                        )
                    }
                }

                when (httpSubTab) {
                    HttpSubTab.Logs -> HttpLogList(
                        viewModel = httpLogListViewModel,
                        searchQuery = searchQuery,
                        onDismissSearch = {
                            isSearchActive = false
                            searchQuery = ""
                        },
                        onHttpClick = {
                            navigator.pushDetailPane(
                                WiretapScreen.HttpDetailScreen(it.id),
                            )
                        },
                        onCreateRule = {
                            navigator.pushDetailPane(
                                WiretapScreen.CreateRuleScreen(prefillFromLogId = it.id),
                            )
                        },
                        onViewRule = { ruleId ->
                            navigator.pushDetailPane(
                                WiretapScreen.RuleDetailScreen(ruleId),
                            )
                        },
                        modifier = Modifier.fillMaxSize(),
                    )

                    HttpSubTab.Rules -> RulesListScreen(
                        viewModel = rulesListViewModel,
                        onRuleClick = {
                            navigator.pushDetailPane(
                                WiretapScreen.RuleDetailScreen(it.id),
                            )
                        },
                        onCreateClick = { navigator.pushDetailPane(WiretapScreen.CreateRuleScreen()) },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }

    if (showClearConfirmation) {
        ClearLogsConfirmationDialog(
            onDismiss = { showClearConfirmation = false },
            onConfirm = {
                httpLogListViewModel.clearLogs()
                showClearConfirmation = false
            },
        )
    }
}
