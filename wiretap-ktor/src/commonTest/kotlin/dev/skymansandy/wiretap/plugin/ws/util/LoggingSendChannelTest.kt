/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.plugin.ws.util

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.test.runTest

class LoggingSendChannelTest : DescribeSpec({

    describe("send()") {
        it("logs the frame before forwarding it to the delegate") {
            runTest {
                val delegate = Channel<Frame>(Channel.UNLIMITED)
                val logged = mutableListOf<Frame>()
                val wrapped = LoggingSendChannel(delegate = delegate, logAction = { logged += it })

                wrapped.send(Frame.Text("payload"))

                logged.size shouldBe 1
                (delegate.tryReceive().getOrNull() as Frame.Text).readText() shouldBe "payload"
            }
        }

        it("logs every frame even when the delegate is closed mid-flight") {
            runTest {
                val delegate = Channel<Frame>(Channel.UNLIMITED)
                val logged = mutableListOf<Frame>()
                val wrapped = LoggingSendChannel(delegate = delegate, logAction = { logged += it })

                wrapped.send(Frame.Text("first"))
                delegate.close()
                val threw = runCatching { wrapped.send(Frame.Text("second")) }.isFailure

                threw shouldBe true
                logged.size shouldBe 2
            }
        }
    }
})
