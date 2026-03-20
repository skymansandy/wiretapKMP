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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
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
import dev.skymansandy.wiretap.resources.Res
import dev.skymansandy.wiretap.resources.back
import dev.skymansandy.wiretap.resources.close_search
import dev.skymansandy.wiretap.resources.mocked_by_rule
import dev.skymansandy.wiretap.resources.search
import dev.skymansandy.wiretap.resources.share
import dev.skymansandy.wiretap.resources.share_as_curl
import dev.skymansandy.wiretap.resources.share_as_text
import dev.skymansandy.wiretap.resources.tab_overview
import dev.skymansandy.wiretap.resources.tab_request
import dev.skymansandy.wiretap.resources.tab_response
import dev.skymansandy.wiretap.resources.throttled_by_rule
import dev.skymansandy.wiretap.resources.view_rule_arrow
import dev.skymansandy.wiretap.ui.common.SearchField
import dev.skymansandy.wiretap.ui.screens.console.http.components.tabs.OverviewTab
import dev.skymansandy.wiretap.ui.screens.console.http.components.tabs.RequestTab
import dev.skymansandy.wiretap.ui.screens.console.http.components.tabs.ResponseTab
import dev.skymansandy.wiretap.helper.util.buildCurlCommand
import dev.skymansandy.wiretap.helper.util.buildShareText
import dev.skymansandy.wiretap.helper.util.shareNetworkLog
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkLogDetailScreen(
    entry: HttpLogEntry,
    onBack: () -> Unit,
    onViewRule: ((ruleId: Long) -> Unit)? = null,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    val searchFocusRequester = remember { FocusRequester() }
    val tabs = listOf(stringResource(Res.string.tab_overview), stringResource(Res.string.tab_request), stringResource(Res.string.tab_response))
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
                        Text(
                            text = "${entry.method} ${entry.url}",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
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
                    if (supportsSearch) {
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
                    }
                    Box {
                        IconButton(onClick = { showShareMenu = true }) {
                            Icon(Icons.Default.Share, contentDescription = stringResource(Res.string.share))
                        }
                        DropdownMenu(
                            expanded = showShareMenu,
                            onDismissRequest = { showShareMenu = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(Res.string.share_as_text)) },
                                onClick = {
                                    showShareMenu = false
                                    shareNetworkLog(
                                        subject = "${entry.method} ${entry.responseCode} - ${entry.url}",
                                        text = buildShareText(entry),
                                    )
                                },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(Res.string.share_as_curl)) },
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
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) },
                    )
                }
            }

            if (entry.source != ResponseSource.Network) {
                RuleMatchBanner(entry.source, entry.matchedRuleId, onViewRule)
            }

            when (selectedTab) {
                0 -> OverviewTab(entry)
                1 -> RequestTab(entry, debouncedQuery)
                2 -> ResponseTab(entry, debouncedQuery)
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
            label = stringResource(Res.string.mocked_by_rule)
        }
        ResponseSource.Throttle -> {
            bgColor = MaterialTheme.colorScheme.tertiaryContainer
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            label = stringResource(Res.string.throttled_by_rule)
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
                text = stringResource(Res.string.view_rule_arrow),
                style = MaterialTheme.typography.labelMedium,
                color = contentColor,
            )
        }
    }
}

@Preview
@Composable
private fun NetworkLogDetailScreenPreview() {
    MaterialTheme {
        NetworkLogDetailScreen(
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
private fun NetworkLogDetailScreenMockedPreview() {
    MaterialTheme {
        NetworkLogDetailScreen(
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
private fun RuleMatchBannerMockPreview() {
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
private fun RuleMatchBannerThrottlePreview() {
    MaterialTheme {
        RuleMatchBanner(
            source = ResponseSource.Throttle,
            matchedRuleId = 2,
            onViewRule = {},
        )
    }
}
