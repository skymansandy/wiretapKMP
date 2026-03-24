package dev.skymansandy.wiretap.ui.screens.console.http

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.data.db.entity.HttpLogEntry
import dev.skymansandy.wiretap.domain.model.ResponseSource
import dev.skymansandy.wiretap.helper.util.buildCurlCommand
import dev.skymansandy.wiretap.helper.util.buildShareText
import dev.skymansandy.wiretap.helper.util.shareNetworkLog
import dev.skymansandy.wiretap.ui.common.SearchField
import dev.skymansandy.wiretap.ui.screens.console.http.components.tabs.OverviewTab
import dev.skymansandy.wiretap.ui.screens.console.http.components.tabs.RequestTab
import dev.skymansandy.wiretap.ui.screens.console.http.components.tabs.ResponseTab
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HttpLogDetailScreen(
    entry: HttpLogEntry,
    onBack: () -> Unit,
    onViewRule: ((ruleId: Long) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    val searchFocusRequester = remember { FocusRequester() }
    val tabs = listOf(
        "Overview",
        "Request",
        "Response",
    )
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

    var showShareMenu by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive && supportsSearch) {
                        SearchField(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            focusRequester = searchFocusRequester,
                        )
                    } else {
                        Column {
                            Text(
                                text = entry.method + " " + when {
                                    entry.isInProgress -> "..."
                                    entry.responseCode > 0 -> entry.responseCode.toString()
                                    entry.responseCode == -1 -> "!!!"
                                    else -> "ERR"
                                },
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
                    IconButton(onClick = {
                        if (isSearchActive) {
                            isSearchActive = false
                            searchQuery = ""
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (supportsSearch) {
                        if (isSearchActive) {
                            IconButton(onClick = {
                                isSearchActive = false
                                searchQuery = ""
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Close search")
                            }
                        } else {
                            IconButton(onClick = { isSearchActive = true }) {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            }
                        }
                    }
                    Box {
                        IconButton(onClick = { showShareMenu = true }) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                        }
                        DropdownMenu(
                            expanded = showShareMenu,
                            onDismissRequest = { showShareMenu = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("Share as text") },
                                onClick = {
                                    showShareMenu = false
                                    shareNetworkLog(
                                        subject = "${entry.method} ${entry.responseCode} - ${entry.url}",
                                        text = buildShareText(entry),
                                    )
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Share as cURL") },
                                onClick = {
                                    showShareMenu = false
                                    shareNetworkLog(
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
                RuleMatchBanner(entry.source, entry.matchedRuleId, onViewRule)
            }

            HorizontalPager(
                modifier = Modifier.weight(1f),
                state = pagerState,
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

@Composable
private fun RuleMatchBanner(
    source: ResponseSource,
    matchedRuleId: Long?,
    onViewRule: ((ruleId: Long) -> Unit)?,
) {
    val bgColor: Color
    val contentColor: Color
    val label: String
    when (source) {
        ResponseSource.Mock -> {
            bgColor = MaterialTheme.colorScheme.secondaryContainer
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            label = "Mocked by rule"
        }

        ResponseSource.Throttle -> {
            bgColor = MaterialTheme.colorScheme.tertiaryContainer
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            label = "Throttled by rule"
        }

        ResponseSource.Network -> return
    }
    val clickable = matchedRuleId != null && onViewRule != null
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .then(
                if (clickable) Modifier.clickable { onViewRule?.invoke(matchedRuleId!!) }
                else Modifier,
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
        )
        if (clickable) {
            Text(
                text = "View Rule \u2192",
                style = MaterialTheme.typography.labelMedium,
                color = contentColor,
            )
        }
    }
}

@Preview
@Composable
private fun Preview_HttpLogDetailScreen() {
    MaterialTheme {
        HttpLogDetailScreen(
            entry = HttpLogEntry(
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
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun Preview_HttpLogDetailScreenMocked() {
    MaterialTheme {
        HttpLogDetailScreen(
            entry = HttpLogEntry(
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
            onBack = {},
            onViewRule = {},
        )
    }
}

@Preview
@Composable
private fun Preview_RuleMatchBannerMock() {
    MaterialTheme {
        RuleMatchBanner(
            source = ResponseSource.Mock,
            matchedRuleId = 1,
            onViewRule = {},
        )
    }
}

@Preview
@Composable
private fun Preview_RuleMatchBannerThrottle() {
    MaterialTheme {
        RuleMatchBanner(
            source = ResponseSource.Throttle,
            matchedRuleId = 2,
            onViewRule = {},
        )
    }
}
