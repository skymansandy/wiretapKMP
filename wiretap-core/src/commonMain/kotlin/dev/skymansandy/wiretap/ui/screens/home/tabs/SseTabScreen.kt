/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.ui.screens.home.tabs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import app.cash.paging.compose.collectAsLazyPagingItems
import dev.skymansandy.wiretap.navigation.api.WiretapScreen
import dev.skymansandy.wiretap.navigation.compose.LocalWiretapNavigator
import dev.skymansandy.wiretap.ui.common.ClearLogsConfirmationDialog
import dev.skymansandy.wiretap.ui.common.WiretapTopBar
import dev.skymansandy.wiretap.ui.screens.sse.list.SseLogList
import dev.skymansandy.wiretap.ui.screens.sse.list.SseLogListViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun SseTabScreen(
    modifier: Modifier = Modifier,
    viewModel: SseLogListViewModel = koinViewModel(),
    navigationRail: (@Composable () -> Unit)? = null,
) {
    val navigator = LocalWiretapNavigator.current
    val lazyItems = viewModel.sseLogs.collectAsLazyPagingItems()

    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val searchFocusRequester = remember { FocusRequester() }
    var showClearConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            searchFocusRequester.requestFocus()
        }
    }

    // Sync search query to ViewModel
    LaunchedEffect(searchQuery) {
        viewModel.updateSearchQuery(searchQuery)
    }

    Column(modifier = modifier) {
        WiretapTopBar(
            title = "SSE Console",
            isSearchActive = isSearchActive,
            searchQuery = searchQuery,
            searchFocusRequester = searchFocusRequester,
            showClearAction = lazyItems.itemCount > 0,
            onClose = navigator::exit,
            onSearchQueryChange = { searchQuery = it },
            onSearchActiveChange = { active ->
                isSearchActive = active
                if (!active) searchQuery = ""
            },
            onClear = { showClearConfirmation = true },
        )

        Row(modifier = Modifier.weight(1f)) {
            navigationRail?.invoke()

            SseLogList(
                viewModel = viewModel,
                searchQuery = searchQuery,
                onDismissSearch = {
                    isSearchActive = false
                    searchQuery = ""
                },
                onConnectionClick = { navigator.pushDetailPane(WiretapScreen.SseDetailScreen(it.id)) },
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
        }
    }

    if (showClearConfirmation) {
        ClearLogsConfirmationDialog(
            onDismiss = { showClearConfirmation = false },
            onConfirm = {
                viewModel.clearLogs()
                showClearConfirmation = false
            },
        )
    }
}
