/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.okhttp.ws

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import okio.ByteString.Companion.toByteString

class WiretapOkHttpWebSocketListenerNoopTest : DescribeSpec({
    describe("noop WebSocketListener") {
        it("forwards every callback to the delegate without touching Wiretap") {
            val delegate = RecordingListener()
            val listener = (delegate as WebSocketListener).wiretapped { enabled = true }

            val request = Request.Builder().url("http://test.local/ws").build()
            val response = Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(101)
                .message("Switching Protocols")
                .build()
            val webSocket = StubWebSocket(request)

            listener.onOpen(webSocket, response)
            listener.onMessage(webSocket, "hello")
            listener.onMessage(webSocket, byteArrayOf(1, 2).toByteString())
            listener.onClosing(webSocket, 1001, "going")
            listener.onClosed(webSocket, 1000, "bye")
            listener.onFailure(webSocket, RuntimeException("kaboom"), null)

            delegate.calls shouldBe listOf(
                "onOpen",
                "onMessage:hello",
                "onMessage:bytes:2",
                "onClosing:1001:going",
                "onClosed:1000:bye",
                "onFailure:kaboom",
            )
        }

        it("accepts both wiretapped() overloads without error") {
            val delegate = RecordingListener()

            @Suppress("DEPRECATION")
            (delegate as WebSocketListener).wiretapped(enabled = true)
            (delegate as WebSocketListener).wiretapped { enabled = true }
        }
    }
})

private class RecordingListener : WebSocketListener() {

    val calls = mutableListOf<String>()

    override fun onOpen(webSocket: WebSocket, response: Response) {
        calls += "onOpen"
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        calls += "onMessage:$text"
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        calls += "onMessage:bytes:${bytes.size}"
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        calls += "onClosing:$code:$reason"
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        calls += "onClosed:$code:$reason"
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        calls += "onFailure:${t.message}"
    }
}

private class StubWebSocket(private val request: Request) : WebSocket {

    override fun request(): Request = request

    override fun queueSize(): Long = 0

    override fun send(text: String): Boolean = true

    override fun send(bytes: ByteString): Boolean = true

    override fun close(code: Int, reason: String?): Boolean = true

    override fun cancel() = Unit
}
