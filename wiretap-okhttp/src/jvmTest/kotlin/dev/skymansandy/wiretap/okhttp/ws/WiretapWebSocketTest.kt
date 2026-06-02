/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.okhttp.ws

import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.matches
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verifySuspend
import dev.skymansandy.wiretap.domain.model.SocketContentType
import dev.skymansandy.wiretap.domain.model.SocketMessage
import dev.skymansandy.wiretap.domain.model.SocketMessageType
import dev.skymansandy.wiretap.domain.orchestrator.SocketLogManager
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import okhttp3.WebSocket
import okio.ByteString.Companion.toByteString
import kotlin.time.Duration.Companion.seconds

class WiretapWebSocketTest : DescribeSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    val socketLogManager = mock<SocketLogManager>(MockMode.autoUnit)
    val delegate = mock<WebSocket>(MockMode.autoUnit)

    describe("send(String)") {
        it("logs an outbound Text frame and delegates to the wrapped WebSocket") {
            every { delegate.send("hello") } returns true
            val wrap = WiretapWebSocket(delegate, socketId = 7L, socketLogManager)

            val result = wrap.send("hello")

            result shouldBe true
            verify { delegate.send("hello") }
            eventually(5.seconds) {
                verifySuspend {
                    socketLogManager.logSocketMsg(
                        matches<SocketMessage> {
                            it.socketId == 7L &&
                                it.direction == SocketMessageType.Sent &&
                                it.contentType == SocketContentType.Text &&
                                it.content == "hello"
                        },
                    )
                }
            }
        }
    }

    describe("send(ByteString)") {
        it("logs an outbound Binary frame and delegates to the wrapped WebSocket") {
            val payload = byteArrayOf(9, 8, 7).toByteString()
            every { delegate.send(payload) } returns true
            val wrap = WiretapWebSocket(delegate, socketId = 7L, socketLogManager)

            val result = wrap.send(payload)

            result shouldBe true
            verify { delegate.send(payload) }
            eventually(5.seconds) {
                verifySuspend {
                    socketLogManager.logSocketMsg(
                        matches<SocketMessage> {
                            it.socketId == 7L &&
                                it.direction == SocketMessageType.Sent &&
                                it.contentType == SocketContentType.Binary &&
                                it.byteCount == 3L
                        },
                    )
                }
            }
        }
    }

    describe("close") {
        it("delegates close to the wrapped WebSocket without logging a frame") {
            every { delegate.close(1000, "bye") } returns true
            val wrap = WiretapWebSocket(delegate, socketId = 7L, socketLogManager)

            val result = wrap.close(1000, "bye")

            result shouldBe true
            verify { delegate.close(1000, "bye") }
        }
    }
})
