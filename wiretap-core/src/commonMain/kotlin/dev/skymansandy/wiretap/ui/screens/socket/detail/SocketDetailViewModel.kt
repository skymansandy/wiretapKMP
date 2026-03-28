package dev.skymansandy.wiretap.ui.screens.socket.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.skymansandy.wiretap.domain.model.SocketConnection
import dev.skymansandy.wiretap.domain.model.SocketMessage
import dev.skymansandy.wiretap.domain.orchestrator.SocketLogManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class SocketDetailViewModel(
    private val socketId: Long,
    private val socketLogManager: SocketLogManager,
) : ViewModel() {

    val initialEntry: StateFlow<SocketConnection?>
        field = MutableStateFlow(null)

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

    init {
        viewModelScope.launch {
            initialEntry.value = socketLogManager.getSocketById(socketId)
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
