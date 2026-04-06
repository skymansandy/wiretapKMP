/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.okhttp

import dev.skymansandy.wiretap.di.WiretapDi
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.koin.core.component.KoinComponent

/**
 * Wraps a consumer's WebSocketListener to log all WebSocket events via Wiretap.
 *
 * In debug builds (with wiretap-okhttp on the classpath), real logging activates.
 * In release builds (wiretap-okhttp-noop or API-only), pure pass-through.
 *
 * Usage — extension function:
 * ```kotlin
 * client.newWebSocket(request, myListener.wiretapped())
 * ```
 *
 * Usage — constructor:
 * ```kotlin
 * val listener = WiretapOkHttpWebSocketListener(myListener)
 * client.newWebSocket(request, listener)
 * ```
 */
class WiretapOkHttpWebSocketListener(
    private val delegate: WebSocketListener,
) : WebSocketListener() {

    private val realListener: WebSocketListener by lazy {
        val factory = try {
            object : KoinComponent {
                override fun getKoin() = WiretapDi.getKoin()
            }.getKoin().getOrNull<WiretapOkHttpWsListenerFactory>()
        } catch (_: Exception) {
            null
        }

        factory?.create(delegate) ?: delegate
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        realListener.onOpen(webSocket, response)
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        realListener.onMessage(webSocket, text)
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        realListener.onMessage(webSocket, bytes)
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        realListener.onClosing(webSocket, code, reason)
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        realListener.onClosed(webSocket, code, reason)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        realListener.onFailure(webSocket, t, response)
    }
}

/**
 * Wraps this [WebSocketListener] with Wiretap logging.
 *
 * ```kotlin
 * client.newWebSocket(request, myListener.wiretapped())
 * ```
 */
fun WebSocketListener.wiretapped(): WiretapOkHttpWebSocketListener =
    WiretapOkHttpWebSocketListener(this)
