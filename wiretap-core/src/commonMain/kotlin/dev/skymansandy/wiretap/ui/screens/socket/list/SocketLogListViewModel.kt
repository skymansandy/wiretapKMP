package dev.skymansandy.wiretap.ui.screens.socket.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.skymansandy.wiretap.domain.model.SocketConnection
import dev.skymansandy.wiretap.domain.orchestrator.SocketLogManager
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
internal class SocketLogListViewModel(
    private val socketLogManager: SocketLogManager,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    private val debouncedQuery: StateFlow<String> = _searchQuery
        .debounce { if (it.isEmpty()) 0L else 450L }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "",
        )

    val socketLogs: StateFlow<List<SocketConnection>> = combine(
        socketLogManager.flowAllSockets(),
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

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearLogs() {
        viewModelScope.launch { socketLogManager.clearLogs() }
    }
}
