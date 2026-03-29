/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.ui.screens.http.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.skymansandy.wiretap.domain.model.HttpLog
import dev.skymansandy.wiretap.domain.model.ResponseSource
import dev.skymansandy.wiretap.helper.util.buildCurlCommand
import dev.skymansandy.wiretap.helper.util.buildShareText
import dev.skymansandy.wiretap.helper.util.shareHttpLogs
import dev.skymansandy.wiretap.navigation.api.WiretapScreen
import dev.skymansandy.wiretap.navigation.compose.LocalWiretapNavigator
import dev.skymansandy.wiretap.ui.common.LocalSnackbarHostState
import dev.skymansandy.wiretap.ui.common.SearchField
import dev.skymansandy.wiretap.ui.mock.PreviewWithNavigator
import dev.skymansandy.wiretap.ui.screens.http.detail.component.RuleMatchBanner
import dev.skymansandy.wiretap.ui.screens.http.detail.tabs.OverviewTab
import dev.skymansandy.wiretap.ui.screens.http.detail.tabs.RequestTab
import dev.skymansandy.wiretap.ui.screens.http.detail.tabs.ResponseTab
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HttpLogDetailScreen(
    entryId: Long,
    modifier: Modifier = Modifier,
) {
    val vm = koinViewModel<HttpLogDetailViewModel> { parametersOf(entryId) }
    val entry by vm.entry.collectAsStateWithLifecycle()
    val currentEntry = entry ?: return

    HttpLogDetailScreenContent(
        modifier = modifier,
        entry = currentEntry,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HttpLogDetailScreenContent(
    modifier: Modifier = Modifier,
    entry: HttpLog,
) {
    val navigator = LocalWiretapNavigator.current

    val coroutineScope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    val searchFocusRequester = remember { FocusRequester() }
    val tabs = remember {
        listOf(
            "OVERVIEW",
            "REQUEST",
            "RESPONSE",
        )
    }

    val pagerState = rememberPagerState(
        pageCount = { tabs.size },
    )
    val selectedTab = pagerState.currentPage
    val supportsSearch = selectedTab != 0

    LaunchedEffect(selectedTab) {
        isSearchActive = false
        searchQuery = ""
    }

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            searchFocusRequester.requestFocus()
        }
    }

    val debouncedQuery by produceState(initialValue = "", key1 = searchQuery) {
        if (searchQuery.isEmpty()) {
            value = ""
        } else {
            delay(450)
            value = searchQuery
        }
    }

    var showShareMenu by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
        Scaffold(
            modifier = modifier,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        if (isSearchActive && supportsSearch) {
                            SearchField(
                                modifier = Modifier.focusRequester(searchFocusRequester),
                                query = searchQuery,
                                onQueryChange = { searchQuery = it },
                            )
                        } else {
                            Column {
                                Text(
                                    text = entry.method + " " + entry.statusText,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )

                                Text(
                                    text = entry.url,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                if (isSearchActive) {
                                    isSearchActive = false
                                    searchQuery = ""
                                } else {
                                    navigator.pop()
                                }
                            },
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    },
                    actions = {
                        if (supportsSearch) {
                            if (isSearchActive) {
                                IconButton(
                                    onClick = {
                                        isSearchActive = false
                                        searchQuery = ""
                                    },
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close search",
                                    )
                                }
                            } else {
                                IconButton(onClick = { isSearchActive = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search",
                                    )
                                }
                            }
                        }
                        Box {
                            IconButton(
                                onClick = {
                                    showShareMenu = true
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                )
                            }

                            DropdownMenu(
                                expanded = showShareMenu,
                                onDismissRequest = { showShareMenu = false },
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Share as text") },
                                    onClick = {
                                        showShareMenu = false
                                        shareHttpLogs(
                                            subject = "${entry.method} ${entry.responseCode} - ${entry.url}",
                                            text = buildShareText(entry),
                                        )
                                    },
                                )

                                DropdownMenuItem(
                                    text = { Text("Share as cURL") },
                                    onClick = {
                                        showShareMenu = false
                                        shareHttpLogs(
                                            subject = "cURL - ${entry.method} ${entry.url}",
                                            text = buildCurlCommand(entry),
                                        )
                                    },
                                )
                            }
                        }
                    },
                )
            },
        ) { padding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            text = { Text(title) },
                        )
                    }
                }

                if (entry.source != ResponseSource.Network) {
                    RuleMatchBanner(
                        modifier = Modifier.fillMaxWidth(),
                        source = entry.source,
                        matchedRuleId = entry.matchedRuleId,
                        onViewRule = { ruleId ->
                            navigator.push(WiretapScreen.RuleDetailScreen(ruleId))
                        },
                    )
                }

                HorizontalPager(
                    modifier = Modifier.weight(1f),
                    state = pagerState,
                    beyondViewportPageCount = tabs.size,
                ) { page ->
                    when (page) {
                        0 -> OverviewTab(
                            modifier = Modifier.fillMaxSize(),
                            entry = entry,
                        )

                        1 -> RequestTab(
                            modifier = Modifier.fillMaxSize(),
                            entry = entry,
                            searchQuery = debouncedQuery,
                        )

                        2 -> ResponseTab(
                            modifier = Modifier.fillMaxSize(),
                            entry = entry,
                            searchQuery = debouncedQuery,
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview_HttpLogDetailScreen() {
    PreviewWithNavigator {
        HttpLogDetailScreenContent(
            entry = HttpLog(
                id = 1,
                url = "https://api.example.com/users/123",
                method = "GET",
                requestHeaders = mapOf(
                    "Authorization" to "Bearer token",
                    "Accept" to "application/json",
                ),
                responseCode = 200,
                responseHeaders = mapOf(
                    "Content-Type" to "application/json",
                ),
                responseBody = """{"id":123,"name":"John","email":"john@example.com"}""",
                durationMs = 142,
                timestamp = 1710850000000,
            ),
        )
    }
}

@Preview
@Composable
private fun Preview_HttpLogDetailScreenMocked() {
    PreviewWithNavigator {
        HttpLogDetailScreenContent(
            entry = HttpLog(
                id = 2,
                url = "https://api.example.com/users",
                method = "POST",
                responseCode = 201,
                durationMs = 3,
                timestamp = 1710850000000,
                source = ResponseSource.Mock,
                matchedRuleId = 1,
                responseBody = """{"id":456,"name":"Mock User"}""",
            ),
        )
    }
}
