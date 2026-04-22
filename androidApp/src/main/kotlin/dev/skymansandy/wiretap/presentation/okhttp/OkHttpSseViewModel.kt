package dev.skymansandy.wiretap.presentation.okhttp

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.skymansandy.wiretap.helper.markers.ExperimentalWiretapSseApi
import dev.skymansandy.wiretap.okhttp.sse.wiretapped
import dev.skymansandy.wiretapsample.model.SampleMessage
import dev.skymansandy.wiretapsample.model.SampleMessage.MessageType
import dev.skymansandy.wiretapsample.model.SseSampleActions
import dev.skymansandy.wiretapsample.model.sseServers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources

@OptIn(ExperimentalWiretapSseApi::class)
internal class OkHttpSseViewModel(
    private val client: OkHttpClient,
) : ViewModel(), SseSampleActions {

    private val _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _isConnecting = MutableStateFlow(false)
    override val isConnecting: StateFlow<Boolean> = _isConnecting.asStateFlow()

    private val _selectedServerIndex = MutableStateFlow(0)
    override val selectedServerIndex: StateFlow<Int> = _selectedServerIndex.asStateFlow()

    override val servers: List<Pair<String, String>> = sseServers

    override val eventLog: SnapshotStateList<SampleMessage> = mutableStateListOf()

    private var eventSource: EventSource? = null
    private var sseUrl = sseServers[0].first

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

        val request = Request.Builder()
            .url(sseUrl)
            .header("Accept", "text/event-stream")
            .header("User-Agent", "WiretapSample/1.0")
            .build()
        val listener = object : EventSourceListener() {

            override fun onOpen(eventSource: EventSource, response: Response) {
                viewModelScope.launch(Dispatchers.Main) {
                    _isConnecting.value = false
                    _isConnected.value = true
                    eventLog.add(SampleMessage(MessageType.System, "Connected!"))
                }
            }

            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                viewModelScope.launch(Dispatchers.Main) {
                    val text = buildString {
                        type?.let { append("[$it] ") }
                        append(data)
                        id?.let { append(" (id=$it)") }
                    }
                    eventLog.add(SampleMessage(MessageType.Received, text))
                }
            }

            override fun onClosed(eventSource: EventSource) {
                viewModelScope.launch(Dispatchers.Main) {
                    _isConnected.value = false
                    this@OkHttpSseViewModel.eventSource = null
                    eventLog.add(SampleMessage(MessageType.System, "Connection closed"))
                }
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                viewModelScope.launch(Dispatchers.Main) {
                    _isConnecting.value = false
                    _isConnected.value = false
                    this@OkHttpSseViewModel.eventSource = null
                    eventLog.add(SampleMessage(MessageType.System, "Error: ${t?.message}"))
                }
            }
        }

        val factory = EventSources.createFactory(client)
        eventSource = factory.newEventSource(request, listener.wiretapped())
    }

    private fun disconnect() {
        eventSource?.cancel()
        eventSource = null
        _isConnected.value = false
        eventLog.add(SampleMessage(MessageType.System, "Disconnected"))
    }

    override fun onCleared() {
        super.onCleared()
        eventSource?.cancel()
    }
}
