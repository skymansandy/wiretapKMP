package dev.skymansandy.wiretapsample.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.skymansandy.wiretap.plugin.WiretapWebSocketSession
import dev.skymansandy.wiretap.plugin.wiretapWrap
import dev.skymansandy.wiretapsample.model.WsLogEntry
import dev.skymansandy.wiretapsample.model.wsServers
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

internal class WebSocketViewModel(
    private val client: HttpClient,
) : ViewModel() {

    val isConnected: StateFlow<Boolean>
        field = MutableStateFlow(false)

    val isConnecting: StateFlow<Boolean>
        field = MutableStateFlow(false)

    val selectedServerIndex: StateFlow<Int>
        field = MutableStateFlow(0)

    val wsUrl: StateFlow<String>
        field = MutableStateFlow(wsServers[0].first)

    val messageLog = mutableStateListOf<WsLogEntry>()

    private var session: WiretapWebSocketSession? = null
    private var connectionJob: Job? = null
    private val exceptionHandler = CoroutineExceptionHandler { _, _ -> }

    fun selectServer(index: Int) {
        if (!isConnected.value && !isConnecting.value) {
            selectedServerIndex.value = index
            wsUrl.value = wsServers[index].first
        }
    }

    fun toggleConnection() {
        if (isConnected.value) {
            disconnect()
        } else if (!isConnecting.value) {
            connect()
        }
    }

    private fun connect() {
        isConnecting.value = true
        messageLog.clear()
        messageLog.add(WsLogEntry("SYS", "Connecting to ${wsUrl.value} ..."))
        connectionJob = viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            try {
                client.webSocket(wsUrl.value) {
                    val wrapped = this.wiretapWrap()
                    session = wrapped
                    isConnected.value = true
                    isConnecting.value = false
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
                    isConnected.value = false
                    session = null
                    messageLog.add(WsLogEntry("SYS", "Connection closed"))
                }
            } catch (e: Exception) {
                isConnecting.value = false
                isConnected.value = false
                session = null
                messageLog.add(WsLogEntry("SYS", "Error: ${e.message}"))
            }
        }
    }

    private fun disconnect() {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            try {
                session?.markClosed(1000, "User disconnected")
                session?.delegate?.close()
            } catch (_: Exception) {
                // Ignore close errors
            }
            connectionJob?.cancel()
            session = null
            isConnected.value = false
            messageLog.add(WsLogEntry("SYS", "Disconnected"))
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || !isConnected.value) return
        val currentSession = session
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            try {
                currentSession?.send(Frame.Text(text))
                messageLog.add(WsLogEntry("SENT", text))
            } catch (e: Exception) {
                messageLog.add(WsLogEntry("SYS", "Send failed: ${e.message}"))
            }
        }
    }
}
