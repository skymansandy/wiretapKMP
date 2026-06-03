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

class LoggingReceiveChannelTest : DescribeSpec({

    describe("receive()") {
        it("invokes logAction with the received frame and returns it unchanged") {
            runTest {
                val source = Channel<Frame>(Channel.UNLIMITED)
                val logged = mutableListOf<Frame>()
                val wrapped = LoggingReceiveChannel(
                    delegate = source,
                    logAction = { logged += it },
                    onChannelClosed = { },
                )

                source.send(Frame.Text("hello"))
                val received = wrapped.receive() as Frame.Text

                received.readText() shouldBe "hello"
                logged.size shouldBe 1
                (logged.single() as Frame.Text).readText() shouldBe "hello"
            }
        }
    }

    describe("iterator") {
        it("logs every iterated frame and calls onChannelClosed when the source closes") {
            runTest {
                val source = Channel<Frame>(Channel.UNLIMITED)
                val logged = mutableListOf<Frame>()
                var closeCause: Throwable? = Throwable("sentinel")
                var closedCalled = false
                val wrapped = LoggingReceiveChannel(
                    delegate = source,
                    logAction = { logged += it },
                    onChannelClosed = { cause ->
                        closedCalled = true
                        closeCause = cause
                    },
                )

                source.send(Frame.Text("a"))
                source.send(Frame.Text("b"))
                source.close()

                val collected = mutableListOf<String>()
                for (frame in wrapped) {
                    collected += (frame as Frame.Text).readText()
                }

                collected shouldBe listOf("a", "b")
                logged.size shouldBe 2
                closedCalled shouldBe true
                closeCause shouldBe null
            }
        }
    }

    describe("receiveCatching") {
        it("propagates the close cause via onChannelClosed") {
            runTest {
                val source = Channel<Frame>(Channel.UNLIMITED)
                var observed: Throwable? = null
                val wrapped = LoggingReceiveChannel(
                    delegate = source,
                    logAction = { },
                    onChannelClosed = { cause -> observed = cause },
                )

                val boom = IllegalStateException("dead")
                source.close(boom)
                val result = wrapped.receiveCatching()

                result.isClosed shouldBe true
                observed shouldBe boom
            }
        }
    }
})
