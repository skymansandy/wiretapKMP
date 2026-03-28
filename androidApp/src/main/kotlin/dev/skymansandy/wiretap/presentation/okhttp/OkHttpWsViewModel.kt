package dev.skymansandy.wiretap.presentation.okhttp

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.skymansandy.wiretap.okhttp.wiretapped
import dev.skymansandy.wiretapsample.model.SampleMessage
import dev.skymansandy.wiretapsample.model.SampleMessage.MessageType
import dev.skymansandy.wiretapsample.model.WsSampleActions
import dev.skymansandy.wiretapsample.model.wsServers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

internal class OkHttpWsViewModel(
    private val client: OkHttpClient,
) : ViewModel(), WsSampleActions {

    private val _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _isConnecting = MutableStateFlow(false)
    override val isConnecting: StateFlow<Boolean> = _isConnecting.asStateFlow()

    private val _selectedServerIndex = MutableStateFlow(0)
    override val selectedServerIndex: StateFlow<Int> = _selectedServerIndex.asStateFlow()

    override val servers: List<Pair<String, String>> = wsServers

    override val messageLog: SnapshotStateList<SampleMessage> = mutableStateListOf()
    private var webSocket: WebSocket? = null
    private var wsUrl = wsServers[0].first

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

        val request = Request.Builder().url(wsUrl).build()
        val listener: WebSocketListener = object : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: Response) {
                viewModelScope.launch(Dispatchers.Main) {
                    _isConnecting.value = false
                    _isConnected.value = true
                    messageLog.add(SampleMessage(MessageType.System, "Connected!"))
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                viewModelScope.launch(Dispatchers.Main) {
                    messageLog.add(SampleMessage(MessageType.Received, text))
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
                viewModelScope.launch(Dispatchers.Main) {
                    _isConnected.value = false
                    this@OkHttpWsViewModel.webSocket = null
                    messageLog.add(SampleMessage(MessageType.System, "Connection closing: $code $reason"))
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                viewModelScope.launch(Dispatchers.Main) {
                    _isConnected.value = false
                    this@OkHttpWsViewModel.webSocket = null
                    messageLog.add(SampleMessage(MessageType.System, "Connection closed"))
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                viewModelScope.launch(Dispatchers.Main) {
                    _isConnecting.value = false
                    _isConnected.value = false
                    this@OkHttpWsViewModel.webSocket = null
                    messageLog.add(SampleMessage(MessageType.System, "Error: ${t.message}"))
                }
            }
        }

        webSocket = client.newWebSocket(request, listener.wiretapped())
    }

    private fun disconnect() {
        webSocket?.close(1000, "User disconnected")
        webSocket = null
        _isConnected.value = false
        messageLog.add(SampleMessage(MessageType.System, "Disconnected"))
    }

    override fun sendMessage(text: String) {
        if (text.isBlank() || !_isConnected.value) return
        val sent = webSocket?.send(text) ?: false
        if (sent) {
            messageLog.add(SampleMessage(MessageType.Sent, text))
        } else {
            messageLog.add(SampleMessage(MessageType.System, "Send failed"))
        }
    }

    override fun onCleared() {
        super.onCleared()
        webSocket?.close(1000, null)
    }
}
