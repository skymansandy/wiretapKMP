package dev.skymansandy.wiretap.ui.screens.console

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.paging.PagingData
import dev.skymansandy.wiretap.data.db.entity.HttpLogEntry
import dev.skymansandy.wiretap.data.db.entity.SocketLogEntry
import dev.skymansandy.wiretap.data.db.entity.WiretapRule
import dev.skymansandy.wiretap.domain.orchestrator.WiretapOrchestrator
import dev.skymansandy.wiretap.domain.repository.RuleRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class WiretapHomeViewModel(
    private val orchestrator: WiretapOrchestrator,
    private val ruleRepository: RuleRepository,
) : ViewModel() {

    val selectedTab: StateFlow<Int>
        field = MutableStateFlow(TAB_HTTP)

    val httpSubTab: StateFlow<Int>
        field = MutableStateFlow(HTTP_SUB_TAB_LOGS)

    val isSearchActive: StateFlow<Boolean>
        field = MutableStateFlow(false)

    val searchQuery: StateFlow<String>
        field = MutableStateFlow("")

    @OptIn(FlowPreview::class)
    val debouncedQuery: StateFlow<String> = searchQuery
        .debounce { if (it.isEmpty()) 0L else 450L }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "",
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagedLogs: Flow<PagingData<HttpLogEntry>> = debouncedQuery
        .flatMapLatest { query -> orchestrator.getPagedLogs(query) }

    val socketLogs: StateFlow<List<SocketLogEntry>> = combine(
        orchestrator.getAllSocketLogs(),
        debouncedQuery,
    ) { logs, query ->
        when {
            query.isEmpty() -> logs
            else -> logs.filter {
                it.url.contains(query, ignoreCase = true) || it.status.name.contains(
                    other = query,
                    ignoreCase = true,
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )

    fun selectTab(tab: Int) {
        selectedTab.value = tab
        searchQuery.value = ""
    }

    fun selectHttpSubTab(subTab: Int) {
        httpSubTab.value = subTab
        searchQuery.value = ""
    }

    fun setSearchActive(active: Boolean) {
        isSearchActive.value = active
        if (!active) searchQuery.value = ""
    }

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun clearHttpLogs() {
        viewModelScope.launch { orchestrator.clearLogs() }
    }

    fun clearSocketLogs() {
        viewModelScope.launch { orchestrator.clearSocketLogs() }
    }

    suspend fun getRuleById(id: Long): WiretapRule? {
        return ruleRepository.getById(id)
    }

    companion object {
        const val TAB_HTTP = 0
        const val TAB_WEBSOCKET = 1
        const val HTTP_SUB_TAB_LOGS = 0
        const val HTTP_SUB_TAB_RULES = 1
    }
}
