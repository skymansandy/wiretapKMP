package dev.skymansandy.wiretapsample.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.skymansandy.wiretap.plugin.ws.WiretapWebSocketSession
import dev.skymansandy.wiretap.plugin.ws.wiretapped
import dev.skymansandy.wiretapsample.model.SampleMessage
import dev.skymansandy.wiretapsample.model.SampleMessage.MessageType
import dev.skymansandy.wiretapsample.model.WsSampleActions
import dev.skymansandy.wiretapsample.model.wsServers
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class KtorWebSocketViewModel(
    private val client: HttpClient,
) : ViewModel(), WsSampleActions {

    private val _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _isConnecting = MutableStateFlow(false)
    override val isConnecting: StateFlow<Boolean> = _isConnecting.asStateFlow()

    private val _selectedServerIndex = MutableStateFlow(0)
    override val selectedServerIndex: StateFlow<Int> = _selectedServerIndex.asStateFlow()

    override val servers: List<Pair<String, String>> = wsServers

    override val messageLog: SnapshotStateList<SampleMessage> = mutableStateListOf()

    private var wsUrl = wsServers[0].first
    private var session: WiretapWebSocketSession? = null // nullable because it's only set during active connection
    private var connectionJob: Job? = null
    private val exceptionHandler = CoroutineExceptionHandler { _, _ -> }

    override fun selectServer(index: Int) {
        if (!_isConnected.value && !_isConnecting.value) {
            _selectedServerIndex.value = index
            wsUrl = wsServers[index].first
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
        messageLog.clear()
        messageLog.add(SampleMessage(MessageType.System, "Connecting to $wsUrl ..."))
        connectionJob = viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            try {
                client.webSocket(wsUrl) {
                    val wrapped = this.wiretapped()
                    session = wrapped
                    _isConnected.value = true
                    _isConnecting.value = false
                    messageLog.add(SampleMessage(MessageType.System, "Connected!"))

                    try {
                        for (frame in wrapped.incoming) {
                            if (frame is Frame.Text) {
                                val text = frame.readText()
                                messageLog.add(SampleMessage(MessageType.Received, text))
                            }
                        }
                    } catch (_: Exception) {
                        // Connection closed
                    }
                    _isConnected.value = false
                    session = null
                    messageLog.add(SampleMessage(MessageType.System, "Connection closed"))
                }
            } catch (e: Exception) {
                _isConnecting.value = false
                _isConnected.value = false
                session = null
                messageLog.add(SampleMessage(MessageType.System, "Error: ${e.message}"))
            }
        }
    }

    private fun disconnect() {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            try {
                session?.close(CloseReason(CloseReason.Codes.NORMAL, "User disconnected"))
            } catch (_: Exception) {
                // Ignore close errors
            }
            connectionJob?.cancel()
            session = null
            _isConnected.value = false
            messageLog.add(SampleMessage(MessageType.System, "Disconnected"))
        }
    }

    override fun sendMessage(text: String) {
        if (text.isBlank() || !_isConnected.value) return
        val currentSession = session
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            try {
                currentSession?.send(Frame.Text(text))
                messageLog.add(SampleMessage(MessageType.Sent, text))
            } catch (e: Exception) {
                messageLog.add(SampleMessage(MessageType.System, "Send failed: ${e.message}"))
            }
        }
    }
}
