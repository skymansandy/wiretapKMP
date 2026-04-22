/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.ui.screens.sse.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.skymansandy.wiretap.domain.model.SseConnection
import dev.skymansandy.wiretap.domain.model.SseEvent
import dev.skymansandy.wiretap.domain.orchestrator.SseLogManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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

    init {
        viewModelScope.launch {
            _initialEntry.value = sseLogManager.getConnectionById(connectionId)
        }
    }
}
