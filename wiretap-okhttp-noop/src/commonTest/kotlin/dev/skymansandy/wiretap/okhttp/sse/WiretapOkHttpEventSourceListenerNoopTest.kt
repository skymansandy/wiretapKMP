/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.okhttp.sse

import dev.skymansandy.wiretap.helper.markers.ExperimentalWiretapSseApi
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener

@OptIn(ExperimentalWiretapSseApi::class)
class WiretapOkHttpEventSourceListenerNoopTest : DescribeSpec({
    describe("noop EventSourceListener") {
        it("forwards every callback to the delegate without touching Wiretap") {
            val delegate = RecordingListener()
            val listener = (delegate as EventSourceListener).wiretapped { enabled = true }

            val request = Request.Builder().url("http://test.local/sse").build()
            val response = Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .build()
            val eventSource = StubEventSource(request)

            listener.onOpen(eventSource, response)
            listener.onEvent(eventSource, id = "1", type = "msg", data = "hi")
            listener.onClosed(eventSource)
            listener.onFailure(eventSource, RuntimeException("boom"), null)

            delegate.calls shouldBe listOf("onOpen", "onEvent:1:msg:hi", "onClosed", "onFailure:boom")
        }

        it("accepts both wiretapped() overloads without error") {
            val delegate = RecordingListener()

            @Suppress("DEPRECATION")
            (delegate as EventSourceListener).wiretapped(enabled = true)
            (delegate as EventSourceListener).wiretapped { enabled = true }
        }
    }
})

private class RecordingListener : EventSourceListener() {

    val calls = mutableListOf<String>()

    override fun onOpen(eventSource: EventSource, response: Response) {
        calls += "onOpen"
    }

    override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
        calls += "onEvent:$id:$type:$data"
    }

    override fun onClosed(eventSource: EventSource) {
        calls += "onClosed"
    }

    override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
        calls += "onFailure:${t?.message}"
    }
}

private class StubEventSource(private val request: Request) : EventSource {

    override fun request(): Request = request

    override fun cancel() = Unit
}
