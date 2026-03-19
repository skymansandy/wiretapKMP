package dev.skymansandy.wiretapsample.viewmodel

import androidx.compose.runtime.mutableStateListOf
import dev.skymansandy.wiretap.plugin.WiretapWebSocketSession
import dev.skymansandy.wiretap.plugin.wiretapWrap
import dev.skymansandy.wiretapsample.model.WsLogEntry
import dev.skymansandy.wiretapsample.model.wsServers
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

internal class WebSocketViewModel(
    private val client: HttpClient,
    private val scope: CoroutineScope,
) {
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val _isConnecting = MutableStateFlow(false)
    val isConnecting: StateFlow<Boolean> = _isConnecting

    private val _selectedServerIndex = MutableStateFlow(0)
    val selectedServerIndex: StateFlow<Int> = _selectedServerIndex

    private val _wsUrl = MutableStateFlow(wsServers[0].first)
    val wsUrl: StateFlow<String> = _wsUrl

    val messageLog = mutableStateListOf<WsLogEntry>()

    private var session: WiretapWebSocketSession? = null
    private var connectionJob: Job? = null

    fun selectServer(index: Int) {
        if (!_isConnected.value && !_isConnecting.value) {
            _selectedServerIndex.value = index
            _wsUrl.value = wsServers[index].first
        }
    }

    fun toggleConnection() {
        if (_isConnected.value) {
            disconnect()
        } else if (!_isConnecting.value) {
            connect()
        }
    }

    private fun connect() {
        _isConnecting.value = true
        messageLog.clear()
        messageLog.add(WsLogEntry("SYS", "Connecting to ${_wsUrl.value} ..."))
        connectionJob = scope.launch {
            try {
                client.webSocket(_wsUrl.value) {
                    val wrapped = this.wiretapWrap()
                    session = wrapped
                    _isConnected.value = true
                    _isConnecting.value = false
                    messageLog.add(WsLogEntry("SYS", "Connected!"))

                    try {
                        for (frame in wrapped.incoming) {
                            if (frame is Frame.Text) {
                                val text = frame.readText()
                                wrapped.logReceivedFrame(frame)
                                messageLog.add(WsLogEntry("RECV", text))
                            }
                        }
                    } catch (_: Exception) {
                        // Connection closed
                    }
                    _isConnected.value = false
                    session = null
                    messageLog.add(WsLogEntry("SYS", "Connection closed"))
                }
            } catch (e: Exception) {
                _isConnecting.value = false
                _isConnected.value = false
                session = null
                messageLog.add(WsLogEntry("SYS", "Error: ${e.message}"))
            }
        }
    }

    private fun disconnect() {
        scope.launch {
            try {
                session?.markClosed(1000, "User disconnected")
                session?.delegate?.close()
            } catch (_: Exception) {
                // Ignore close errors
            }
            connectionJob?.cancel()
            session = null
            _isConnected.value = false
            messageLog.add(WsLogEntry("SYS", "Disconnected"))
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || !_isConnected.value) return
        val currentSession = session
        scope.launch {
            try {
                currentSession?.send(Frame.Text(text))
                messageLog.add(WsLogEntry("SENT", text))
            } catch (e: Exception) {
                messageLog.add(WsLogEntry("SYS", "Send failed: ${e.message}"))
            }
        }
    }
}
