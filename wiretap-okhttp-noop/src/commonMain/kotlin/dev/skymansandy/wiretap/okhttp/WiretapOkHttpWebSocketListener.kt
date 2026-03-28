package dev.skymansandy.wiretap.okhttp

import okhttp3.WebSocketListener

/**
 * No-op WebSocket listener for release builds.
 * Pure pass-through to the delegate listener.
 */
class WiretapOkHttpWebSocketListener(
    private val delegate: WebSocketListener,
) : WebSocketListener() {

    override fun onOpen(webSocket: okhttp3.WebSocket, response: okhttp3.Response) {
        delegate.onOpen(webSocket, response)
    }

    override fun onMessage(webSocket: okhttp3.WebSocket, text: String) {
        delegate.onMessage(webSocket, text)
    }

    override fun onMessage(webSocket: okhttp3.WebSocket, bytes: okio.ByteString) {
        delegate.onMessage(webSocket, bytes)
    }

    override fun onClosing(webSocket: okhttp3.WebSocket, code: Int, reason: String) {
        delegate.onClosing(webSocket, code, reason)
    }

    override fun onClosed(webSocket: okhttp3.WebSocket, code: Int, reason: String) {
        delegate.onClosed(webSocket, code, reason)
    }

    override fun onFailure(webSocket: okhttp3.WebSocket, t: Throwable, response: okhttp3.Response?) {
        delegate.onFailure(webSocket, t, response)
    }
}

/**
 * No-op: returns the listener as-is (wrapped in [WiretapOkHttpWebSocketListener] for API parity).
 */
fun WebSocketListener.wiretapped(): WiretapOkHttpWebSocketListener =
    WiretapOkHttpWebSocketListener(this)
