/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.ui.screens.sse.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.paging.PagingData
import app.cash.paging.cachedIn
import dev.skymansandy.wiretap.domain.model.SseConnection
import dev.skymansandy.wiretap.domain.orchestrator.SseLogManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
internal class SseLogListViewModel(
    private val sseLogManager: SseLogManager,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    private val debouncedQuery: StateFlow<String> = _searchQuery
        .debounce { if (it.isEmpty()) 0L else 450L }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "",
        )

    val sseLogs: Flow<PagingData<SseConnection>> = debouncedQuery
        .flatMapLatest { query ->
            sseLogManager.flowPagedConnectionsForSearchQuery(query)
        }
        .cachedIn(viewModelScope)

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearLogs() {
        viewModelScope.launch { sseLogManager.clearLogs() }
    }
}
