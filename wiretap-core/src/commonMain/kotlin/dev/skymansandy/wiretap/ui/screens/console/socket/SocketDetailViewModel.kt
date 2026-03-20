package dev.skymansandy.wiretap.ui.screens.console.socket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.skymansandy.wiretap.data.db.entity.SocketLogEntry
import dev.skymansandy.wiretap.data.db.entity.SocketMessage
import dev.skymansandy.wiretap.domain.orchestrator.WiretapOrchestrator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class SocketDetailViewModel(
    private val socketId: Long,
    private val orchestrator: WiretapOrchestrator,
) : ViewModel() {

    val initialEntry: StateFlow<SocketLogEntry?>
        field = MutableStateFlow(null)

    val liveEntry: StateFlow<SocketLogEntry?> = orchestrator.getSocketByIdFlow(socketId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null,
        )

    val messages: StateFlow<List<SocketMessage>> = orchestrator.getSocketMessages(socketId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    init {
        viewModelScope.launch {
            initialEntry.value = orchestrator.getSocketById(socketId)
        }
    }

    fun urlDisplay(url: String): String {
        return url.substringAfter("://").let {
            val host = it.substringBefore("/").substringBefore("?")
            val path = it.removePrefix(host).ifEmpty { "/" }
            "$host$path"
        }
    }
}
