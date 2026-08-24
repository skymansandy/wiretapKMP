/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.ui.screens.sse.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.skymansandy.wiretap.domain.model.SseConnection
import dev.skymansandy.wiretap.domain.model.SseEvent
import dev.skymansandy.wiretap.domain.orchestrator.SseLogManager
import dev.skymansandy.wiretap.helper.util.SSE_LOG_FILE_NAME
import dev.skymansandy.wiretap.helper.util.buildSseShareText
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
internal class SseDetailViewModel(
    private val connectionId: Long,
    private val sseLogManager: SseLogManager,
) : ViewModel() {

    private val _initialEntry: MutableStateFlow<SseConnection?> = MutableStateFlow(null)
    val initialEntry: StateFlow<SseConnection?> get() = _initialEntry

    val liveEntry: StateFlow<SseConnection?> = sseLogManager.flowConnectionById(connectionId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null,
        )

    val events: StateFlow<List<SseEvent>> = sseLogManager.flowEventsById(connectionId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()

    val debouncedQuery: StateFlow<String> = _searchQuery
        .debounce { if (it.isEmpty()) 0L else SEARCH_DEBOUNCE_MS }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "",
        )

    val matches: StateFlow<List<SseMatchPosition>> = combine(events, debouncedQuery) { evts, q ->
        computeSseMatches(evts, q)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )

    private val _currentMatchIndex = MutableStateFlow(0)
    val currentMatchIndex: StateFlow<Int> = _currentMatchIndex.asStateFlow()

    val shareFileName: String = SSE_LOG_FILE_NAME

    val shareSubject: String
        get() = currentEntry()?.let { "SSE ${it.url}" } ?: ""

    init {
        viewModelScope.launch {
            _initialEntry.value = sseLogManager.getConnectionById(connectionId)
        }
        matches.onEach { _currentMatchIndex.value = 0 }.launchIn(viewModelScope)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun activateSearch() {
        _isSearchActive.value = true
    }

    fun closeSearch() {
        _isSearchActive.value = false
        _searchQuery.value = ""
    }

    fun goToPreviousMatch() {
        val list = matches.value
        if (list.isEmpty()) return
        _currentMatchIndex.value = (_currentMatchIndex.value - 1 + list.size) % list.size
    }

    fun goToNextMatch() {
        val list = matches.value
        if (list.isEmpty()) return
        _currentMatchIndex.value = (_currentMatchIndex.value + 1) % list.size
    }

    fun buildShareText(): String {
        val entry = currentEntry() ?: return ""
        return buildSseShareText(entry, events.value)
    }

    private fun currentEntry(): SseConnection? = liveEntry.value ?: _initialEntry.value

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 450L
    }
}

internal data class SseMatchPosition(
    val eventIndex: Int,
    val start: Int,
    val endInclusive: Int,
)

internal fun computeSseMatches(
    events: List<SseEvent>,
    query: String,
): List<SseMatchPosition> {
    if (query.isBlank()) return emptyList()
    val results = mutableListOf<SseMatchPosition>()
    events.forEachIndexed { index, event ->
        var cursor = 0
        while (true) {
            val hit = event.data.indexOf(query, cursor, ignoreCase = true)
            if (hit < 0) break
            results += SseMatchPosition(
                eventIndex = index,
                start = hit,
                endInclusive = hit + query.length - 1,
            )
            cursor = hit + query.length
        }
    }
    return results
}
