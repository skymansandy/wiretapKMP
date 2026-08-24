/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.ui.screens.sse.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.skymansandy.wiretap.domain.model.SseConnection
import dev.skymansandy.wiretap.domain.model.SseEvent
import dev.skymansandy.wiretap.domain.orchestrator.SseLogManager
import dev.skymansandy.wiretap.helper.util.SSE_LOG_FILE_PREFIX
import dev.skymansandy.wiretap.helper.util.buildSseShareText
import dev.skymansandy.wiretap.helper.util.shareFileName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
internal class SseDetailViewModel(
    private val connectionId: Long,
    private val sseLogManager: SseLogManager,
) : ViewModel() {

    private val _initialEntry: MutableStateFlow<SseConnection?> = MutableStateFlow(null)
    val initialEntry: StateFlow<SseConnection?> get() = _initialEntry

    // liveEntry, the message list and the match list are all read via .value when
    // sharing or stepping through matches, which can happen with nothing collecting
    // them. They are kept eagerly hot for the view model's lifetime rather than
    // relying on a subscriber elsewhere to have brought them up.
    val liveEntry: StateFlow<SseConnection?> = sseLogManager.flowConnectionById(connectionId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null,
        )

    private val entry: StateFlow<SseConnection?> =
        combine(liveEntry, _initialEntry) { live, initial -> live ?: initial }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = null,
            )

    val events: StateFlow<List<SseEvent>> = sseLogManager.flowEventsById(connectionId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
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

    val matches: StateFlow<List<SseMatchPosition>> =
        combine(entry, events, debouncedQuery) { conn, evts, q ->
            computeSseMatches(conn, evts, q)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList(),
        )

    private val _currentMatchIndex = MutableStateFlow(0)
    val currentMatchIndex: StateFlow<Int> = _currentMatchIndex.asStateFlow()

    val shareFileName: String = shareFileName(SSE_LOG_FILE_PREFIX, connectionId)

    val shareSubject: String
        get() = currentEntry()?.let { "SSE ${it.url}" } ?: ""

    init {
        viewModelScope.launch {
            _initialEntry.value = sseLogManager.getConnectionById(connectionId)
        }
    }

    fun setSearchQuery(query: String) {
        if (_searchQuery.value == query) return
        _searchQuery.value = query
        _currentMatchIndex.value = 0
    }

    fun activateSearch() {
        _isSearchActive.value = true
    }

    fun closeSearch() {
        _isSearchActive.value = false
        _searchQuery.value = ""
        _currentMatchIndex.value = 0
    }

    fun goToPreviousMatch() {
        val list = matches.value
        if (list.isEmpty()) return
        val current = _currentMatchIndex.value.coerceAtMost(list.lastIndex)
        _currentMatchIndex.value = (current - 1 + list.size) % list.size
    }

    fun goToNextMatch() {
        val list = matches.value
        if (list.isEmpty()) return
        val current = _currentMatchIndex.value.coerceAtMost(list.lastIndex)
        _currentMatchIndex.value = (current + 1) % list.size
    }

    /**
     * Builds off the main thread: a long-lived stream's event list is
     * unbounded, and this runs from a tap on the share menu.
     */
    suspend fun buildShareText(): String {
        val entry = currentEntry() ?: return ""
        val snapshot = events.value
        return withContext(Dispatchers.Default) { buildSseShareText(entry, snapshot) }
    }

    private fun currentEntry(): SseConnection? = entry.value

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 450L
    }
}

/** Which of an event's rendered fields a match landed in. */
internal enum class SseMatchField {
    Url,
    RequestHeader,
    EventType,
    Data,
    EventId,
}

internal data class SseMatchPosition(
    val field: SseMatchField,
    /** Event index, or request-header line index. Always 0 for [SseMatchField.Url]. */
    val index: Int,
    val start: Int,
    val endInclusive: Int,
)

internal fun computeSseMatches(
    connection: SseConnection?,
    events: List<SseEvent>,
    query: String,
): List<SseMatchPosition> {
    if (query.isBlank()) return emptyList()
    val results = mutableListOf<SseMatchPosition>()
    // The connection block renders above the stream, so its matches come first
    // and stepping through them walks the screen top to bottom.
    connection?.let { conn ->
        results += conn.url.matchesIn(SseMatchField.Url, index = 0, query = query)
        conn.requestHeaders.entries.forEachIndexed { index, (key, value) ->
            results += "$key: $value".matchesIn(SseMatchField.RequestHeader, index, query)
        }
    }
    events.forEachIndexed { index, event ->
        // Visual order within the bubble, so stepping through matches walks the
        // screen top to bottom rather than jumping between fields.
        results += event.eventType.matchesIn(SseMatchField.EventType, index, query)
        results += event.data.matchesIn(SseMatchField.Data, index, query)
        results += event.eventId.matchesIn(SseMatchField.EventId, index, query)
    }
    return results
}

private fun String?.matchesIn(
    field: SseMatchField,
    index: Int,
    query: String,
): List<SseMatchPosition> {
    val text = this ?: return emptyList()
    val results = mutableListOf<SseMatchPosition>()
    var cursor = 0
    while (true) {
        val hit = text.indexOf(query, cursor, ignoreCase = true)
        if (hit < 0) break
        results += SseMatchPosition(
            field = field,
            index = index,
            start = hit,
            endInclusive = hit + query.length - 1,
        )
        cursor = hit + query.length
    }
    return results
}
