/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.ui.screens.socket.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.skymansandy.wiretap.domain.model.SocketConnection
import dev.skymansandy.wiretap.domain.model.SocketContentType
import dev.skymansandy.wiretap.domain.model.SocketMessage
import dev.skymansandy.wiretap.domain.orchestrator.SocketLogManager
import dev.skymansandy.wiretap.helper.util.SOCKET_LOG_FILE_NAME
import dev.skymansandy.wiretap.helper.util.buildSocketShareText
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
internal class SocketDetailViewModel(
    private val socketId: Long,
    private val socketLogManager: SocketLogManager,
) : ViewModel() {

    private val _initialEntry: MutableStateFlow<SocketConnection?> = MutableStateFlow(null)
    val initialEntry: StateFlow<SocketConnection?> get() = _initialEntry

    val liveEntry: StateFlow<SocketConnection?> = socketLogManager.flowSocketById(socketId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null,
        )

    val messages: StateFlow<List<SocketMessage>> = socketLogManager.flowSocketMessagesById(socketId)
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

    val matches: StateFlow<List<SocketMatchPosition>> = combine(messages, debouncedQuery) { msgs, q ->
        computeSocketMatches(msgs, q)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )

    private val _currentMatchIndex = MutableStateFlow(0)
    val currentMatchIndex: StateFlow<Int> = _currentMatchIndex.asStateFlow()

    val shareFileName: String = SOCKET_LOG_FILE_NAME

    val shareSubject: String
        get() = currentEntry()?.let { "WS ${it.url}" } ?: ""

    init {
        viewModelScope.launch {
            _initialEntry.value = socketLogManager.getSocketById(socketId)
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
        return buildSocketShareText(entry, messages.value)
    }

    private fun currentEntry(): SocketConnection? = liveEntry.value ?: _initialEntry.value

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 450L
    }
}

internal data class SocketMatchPosition(
    val messageIndex: Int,
    val start: Int,
    val endInclusive: Int,
)

internal fun computeSocketMatches(
    messages: List<SocketMessage>,
    query: String,
): List<SocketMatchPosition> {
    if (query.isBlank()) return emptyList()
    val lowerQuery = query.lowercase()
    val results = mutableListOf<SocketMatchPosition>()
    messages.forEachIndexed { index, message ->
        if (!message.contentType.isSearchable()) return@forEachIndexed
        val lowerContent = message.content.lowercase()
        var cursor = 0
        while (true) {
            val hit = lowerContent.indexOf(lowerQuery, cursor)
            if (hit < 0) break
            results += SocketMatchPosition(
                messageIndex = index,
                start = hit,
                endInclusive = hit + query.length - 1,
            )
            cursor = hit + query.length
        }
    }
    return results
}

private fun SocketContentType.isSearchable(): Boolean = when (this) {
    SocketContentType.Text -> true
    SocketContentType.Binary,
    SocketContentType.Ping,
    SocketContentType.Pong,
    SocketContentType.Close,
    -> false
}
