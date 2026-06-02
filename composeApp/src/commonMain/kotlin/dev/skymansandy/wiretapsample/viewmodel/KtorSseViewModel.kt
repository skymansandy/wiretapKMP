package dev.skymansandy.wiretapsample.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.skymansandy.wiretapsample.model.SampleMessage
import dev.skymansandy.wiretapsample.model.SampleMessage.MessageType
import dev.skymansandy.wiretapsample.model.SseSampleActions
import dev.skymansandy.wiretapsample.model.sseServers
import io.ktor.client.HttpClient
import io.ktor.client.plugins.sse.sse
import io.ktor.client.request.url
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class KtorSseViewModel(
    private val client: HttpClient,
) : ViewModel(), SseSampleActions {

    private val _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _isConnecting = MutableStateFlow(false)
    override val isConnecting: StateFlow<Boolean> = _isConnecting.asStateFlow()

    private val _selectedServerIndex = MutableStateFlow(0)
    override val selectedServerIndex: StateFlow<Int> = _selectedServerIndex.asStateFlow()

    override val servers: List<Pair<String, String>> = sseServers

    override val eventLog: SnapshotStateList<SampleMessage> = mutableStateListOf()

    private var sseUrl = sseServers[0].first
    private var connectionJob: Job? = null
    private val exceptionHandler = CoroutineExceptionHandler { _, _ -> }

    override fun selectServer(index: Int) {
        if (!_isConnected.value && !_isConnecting.value) {
            _selectedServerIndex.value = index
            sseUrl = sseServers[index].first
        }
    }

    override fun toggleConnection() {
        if (_isConnected.value) {
            disconnect()
        } else if (!_isConnecting.value) {
            connect()
        }
    }

    private fun connect() {
        _isConnecting.value = true
        eventLog.clear()
        eventLog.add(SampleMessage(MessageType.System, "Connecting to $sseUrl ..."))
        connectionJob = viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            try {
                client.sse({
                    url(sseUrl)
                    headers.remove("Accept")
                    headers.append("Accept", "text/event-stream")
                }) {
                    _isConnected.value = true
                    _isConnecting.value = false
                    eventLog.add(SampleMessage(MessageType.System, "Connected!"))

                    try {
                        incoming.collect { event ->
                            val text = buildString {
                                event.event?.let { append("[$it] ") }
                                append(event.data ?: "")
                                event.id?.let { append(" (id=$it)") }
                            }
                            if (text.isNotBlank()) {
                                eventLog.add(SampleMessage(MessageType.Received, text))
                            }
                        }
                    } catch (e: Exception) {
                        if (e !is CancellationException) {
                            eventLog.add(SampleMessage(MessageType.System, "Error: ${e.message}"))
                        }
                    }
                    _isConnected.value = false
                    eventLog.add(SampleMessage(MessageType.System, "Connection closed"))
                }
            } catch (e: Exception) {
                _isConnecting.value = false
                _isConnected.value = false
                if (e !is CancellationException) {
                    eventLog.add(SampleMessage(MessageType.System, "Error: ${e.message}"))
                }
            }
        }
    }

    private fun disconnect() {
        connectionJob?.cancel()
        connectionJob = null
        _isConnected.value = false
        _isConnecting.value = false
        eventLog.add(SampleMessage(MessageType.System, "Disconnected"))
    }
}
