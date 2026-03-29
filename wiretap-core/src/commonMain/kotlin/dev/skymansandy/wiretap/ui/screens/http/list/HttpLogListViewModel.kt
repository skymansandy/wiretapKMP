/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.ui.screens.http.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.paging.PagingData
import app.cash.paging.cachedIn
import dev.skymansandy.wiretap.domain.model.HttpLog
import dev.skymansandy.wiretap.domain.model.HttpLogFilter
import dev.skymansandy.wiretap.domain.orchestrator.HttpLogManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
internal class HttpLogListViewModel(
    private val httpLogManager: HttpLogManager,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val debouncedQuery: StateFlow<String> = _searchQuery
        .debounce { if (it.isEmpty()) 0L else 450L }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = "",
        )

    private val _filter = MutableStateFlow(HttpLogFilter())
    val filter: StateFlow<HttpLogFilter> = _filter

    val pagedLogs: Flow<PagingData<HttpLog>> = combine(debouncedQuery, _filter) { query, filter ->
        query to filter
    }.flatMapLatest { (query, filter) ->
        httpLogManager.flowPagedHttpLogsForSearchQuery(query, filter)
    }.cachedIn(viewModelScope)

    val availableHosts: StateFlow<List<String>> = httpLogManager.flowDistinctHosts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = emptyList(),
        )

    val hasLogs: StateFlow<Boolean> = httpLogManager.flowHttpLogs()
        .map { it.isNotEmpty() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = false,
        )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun applyFilter(filter: HttpLogFilter) {
        _filter.value = filter
    }

    fun clearFilters() {
        _filter.value = HttpLogFilter()
    }

    fun clearLogs() {
        viewModelScope.launch {
            httpLogManager.clearHttpLogs()
        }
    }
}
