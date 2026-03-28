package dev.skymansandy.wiretap.ui.screens.http.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.paging.PagingData
import dev.skymansandy.wiretap.domain.model.HttpLog
import dev.skymansandy.wiretap.domain.orchestrator.HttpLogManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class HttpLogListViewModel(
    private val httpLogManager: HttpLogManager,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    @OptIn(FlowPreview::class)
    private val debouncedQuery: StateFlow<String> = _searchQuery
        .debounce { if (it.isEmpty()) 0L else 450L }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "",
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagedLogs: Flow<PagingData<HttpLog>> = debouncedQuery
        .flatMapLatest { query -> httpLogManager.flowPagedHttpLogsForSearchQuery(query) }

    val hasLogs: StateFlow<Boolean> = httpLogManager.flowHttpLogs()
        .map { it.isNotEmpty() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false,
        )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearLogs() {
        viewModelScope.launch { httpLogManager.clearHttpLogs() }
    }
}
