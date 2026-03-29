/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.ui.screens.home.tabs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
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
import dev.skymansandy.wiretap.ui.common.ClearLogsConfirmationDialog
import dev.skymansandy.wiretap.ui.common.WiretapTopBar
import dev.skymansandy.wiretap.ui.model.HttpSubTab
import dev.skymansandy.wiretap.ui.screens.http.list.HttpLogFilterBottomSheet
import dev.skymansandy.wiretap.ui.screens.http.list.HttpLogList
import dev.skymansandy.wiretap.ui.screens.http.list.HttpLogListViewModel
import dev.skymansandy.wiretap.ui.screens.rules.list.RulesListScreen
import dev.skymansandy.wiretap.ui.screens.rules.list.RulesListViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HttpTabScreen(
    modifier: Modifier = Modifier,
    httpListViewModel: HttpLogListViewModel = koinViewModel(),
    rulesListViewModel: RulesListViewModel = koinViewModel(),
    navigationRail: (@Composable () -> Unit)? = null,
    onBack: () -> Unit,
) {
    val hasHttpLogs by httpListViewModel.hasLogs.collectAsStateWithLifecycle()

    var httpSubTab by remember { mutableStateOf(HttpSubTab.Logs) }
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val searchFocusRequester = remember { FocusRequester() }
    var showClearConfirmation by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    val filter by httpListViewModel.filter.collectAsStateWithLifecycle()

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
            HttpSubTab.Logs -> httpListViewModel.updateSearchQuery(searchQuery)
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
            showFilterAction = httpSubTab == HttpSubTab.Logs,
            activeFilterCount = filter.activeCount,
            onSearchQueryChange = { searchQuery = it },
            onSearchActiveChange = { active ->
                isSearchActive = active
                if (!active) searchQuery = ""
            },
            onBack = onBack,
            onFilter = { showFilterSheet = true },
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
                            text = { Text("LOGS") },
                        )
                        Tab(
                            selected = httpSubTab == HttpSubTab.Rules,
                            onClick = {
                                httpSubTab = HttpSubTab.Rules
                                searchQuery = ""
                            },
                            text = { Text("RULES") },
                        )
                    }
                }

                when (httpSubTab) {
                    HttpSubTab.Logs -> HttpLogList(
                        modifier = Modifier.fillMaxSize(),
                        viewModel = httpListViewModel,
                        filter = filter,
                        searchQuery = searchQuery,
                        onDismissSearch = {
                            isSearchActive = false
                            searchQuery = ""
                        },
                    )

                    HttpSubTab.Rules -> RulesListScreen(
                        modifier = Modifier.fillMaxSize(),
                        viewModel = rulesListViewModel,
                    )
                }
            }
        }
    }

    if (showClearConfirmation) {
        ClearLogsConfirmationDialog(
            onDismiss = { showClearConfirmation = false },
            onConfirm = {
                httpListViewModel.clearLogs()
                showClearConfirmation = false
            },
        )
    }

    if (showFilterSheet) {
        HttpLogFilterBottomSheet(
            modifier = Modifier.statusBarsPadding(),
            viewModel = httpListViewModel,
            onDismiss = { showFilterSheet = false },
        )
    }
}
