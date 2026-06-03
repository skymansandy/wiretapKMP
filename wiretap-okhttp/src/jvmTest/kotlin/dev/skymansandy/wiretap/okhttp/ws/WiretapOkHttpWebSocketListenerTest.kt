/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.okhttp.ws

import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.matches
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.exactly
import dev.mokkery.verifySuspend
import dev.skymansandy.wiretap.domain.model.SocketConnection
import dev.skymansandy.wiretap.domain.model.SocketContentType
import dev.skymansandy.wiretap.domain.model.SocketMessage
import dev.skymansandy.wiretap.domain.model.SocketMessageType
import dev.skymansandy.wiretap.domain.model.SocketStatus
import dev.skymansandy.wiretap.domain.model.config.ws.BinaryFrameDecoding
import dev.skymansandy.wiretap.domain.orchestrator.SocketLogManager
import dev.skymansandy.wiretap.okhttp.testing.installTestKoin
import dev.skymansandy.wiretap.okhttp.testing.teardownTestKoin
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString.Companion.toByteString
import kotlin.time.Duration.Companion.seconds

class WiretapOkHttpWebSocketListenerTest : DescribeSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    val socketLogManager = mock<SocketLogManager>(MockMode.autoUnit)
    val delegate = mock<WebSocketListener>(MockMode.autoUnit)

    val request = Request.Builder().url("http://test.local/ws").build()
    val response = Response.Builder()
        .request(request)
        .protocol(Protocol.HTTP_1_1)
        .code(101)
        .message("Switching Protocols")
        .build()
    val webSocket = mock<WebSocket>(MockMode.autoUnit)

    beforeEach {
        installTestKoin(socketLogManager)
        every { webSocket.request() } returns request
        everySuspend { socketLogManager.createSocket(any()) } returns 100L
    }

    afterEach {
        teardownTestKoin()
    }

    describe("onOpen") {
        it("creates a SocketConnection with Open status and forwards the call") {
            val listener = delegate.wiretapped { enabled = true }

            listener.onOpen(webSocket, response)

            verifySuspend {
                socketLogManager.createSocket(
                    matches<SocketConnection> {
                        it.status == SocketStatus.Open && it.url == "http://test.local/ws"
                    },
                )
            }
        }

        it("does not log anything when enabled = false") {
            val listener = delegate.wiretapped { enabled = false }

            listener.onOpen(webSocket, response)

            verifySuspend(mode = exactly(0)) { socketLogManager.createSocket(any()) }
        }
    }

    describe("onMessage(String)") {
        it("logs an inbound Text frame") {
            val listener = delegate.wiretapped { enabled = true }
            listener.onOpen(webSocket, response)

            listener.onMessage(webSocket, "hello")

            eventually(5.seconds) {
                verifySuspend {
                    socketLogManager.logSocketMsg(
                        matches<SocketMessage> {
                            it.socketId == 100L &&
                                it.direction == SocketMessageType.Received &&
                                it.contentType == SocketContentType.Text &&
                                it.content == "hello"
                        },
                    )
                }
            }
        }
    }

    describe("onMessage(ByteString)") {
        it("logs an inbound Binary frame with the byte count") {
            val listener = delegate.wiretapped { enabled = true }
            listener.onOpen(webSocket, response)

            listener.onMessage(webSocket, byteArrayOf(1, 2, 3, 4).toByteString())

            eventually(5.seconds) {
                verifySuspend {
                    socketLogManager.logSocketMsg(
                        matches<SocketMessage> {
                            it.direction == SocketMessageType.Received &&
                                it.contentType == SocketContentType.Binary &&
                                it.byteCount == 4L
                        },
                    )
                }
            }
        }

        it("auto-decodes a text-over-binary payload as text") {
            val listener = delegate.wiretapped { }
            listener.onOpen(webSocket, response)

            listener.onMessage(webSocket, "{\"hub\":\"chat\"}".encodeToByteArray().toByteString())

            eventually(5.seconds) {
                verifySuspend {
                    socketLogManager.logSocketMsg(
                        matches<SocketMessage> {
                            it.contentType == SocketContentType.Binary &&
                                it.content == "{\"hub\":\"chat\"}"
                        },
                    )
                }
            }
        }

        it("forces UTF-8 decode when binaryDecoding is Utf8") {
            val listener = delegate.wiretapped { binaryDecoding = BinaryFrameDecoding.Utf8 }
            listener.onOpen(webSocket, response)

            listener.onMessage(webSocket, "forced".encodeToByteArray().toByteString())

            eventually(5.seconds) {
                verifySuspend {
                    socketLogManager.logSocketMsg(
                        matches<SocketMessage> {
                            it.contentType == SocketContentType.Binary &&
                                it.content == "forced"
                        },
                    )
                }
            }
        }

        it("renders the placeholder when binaryDecoding is Placeholder") {
            val listener = delegate.wiretapped { binaryDecoding = BinaryFrameDecoding.Placeholder }
            listener.onOpen(webSocket, response)

            listener.onMessage(webSocket, "hello".encodeToByteArray().toByteString())

            eventually(5.seconds) {
                verifySuspend {
                    socketLogManager.logSocketMsg(
                        matches<SocketMessage> {
                            it.contentType == SocketContentType.Binary &&
                                it.content == "[Binary: 5 bytes]"
                        },
                    )
                }
            }
        }

        it("delegates to a user-supplied decoder when binaryDecoding is Custom") {
            val listener = delegate.wiretapped {
                binaryDecoding = BinaryFrameDecoding.Custom { bytes -> "size=${bytes.size}" }
            }
            listener.onOpen(webSocket, response)

            listener.onMessage(webSocket, byteArrayOf(7, 8, 9).toByteString())

            eventually(5.seconds) {
                verifySuspend {
                    socketLogManager.logSocketMsg(
                        matches<SocketMessage> {
                            it.contentType == SocketContentType.Binary &&
                                it.content == "size=3"
                        },
                    )
                }
            }
        }
    }

    describe("onClosing") {
        it("updates the connection to Closing with code and reason") {
            val listener = delegate.wiretapped { enabled = true }
            listener.onOpen(webSocket, response)

            listener.onClosing(webSocket, code = 1001, reason = "going away")

            eventually(5.seconds) {
                verifySuspend {
                    socketLogManager.updateSocket(
                        matches<SocketConnection> {
                            it.id == 100L &&
                                it.status == SocketStatus.Closing &&
                                it.closeCode == 1001 &&
                                it.closeReason == "going away"
                        },
                    )
                }
            }
        }
    }

    describe("onClosed") {
        it("updates the connection to Closed with code and reason") {
            val listener = delegate.wiretapped { enabled = true }
            listener.onOpen(webSocket, response)

            listener.onClosed(webSocket, code = 1000, reason = "normal")

            eventually(5.seconds) {
                verifySuspend {
                    socketLogManager.updateSocket(
                        matches<SocketConnection> {
                            it.id == 100L &&
                                it.status == SocketStatus.Closed &&
                                it.closeCode == 1000 &&
                                it.closeReason == "normal"
                        },
                    )
                }
            }
        }
    }

    describe("onFailure") {
        it("updates the connection to Failed and records the message") {
            val listener = delegate.wiretapped { enabled = true }
            listener.onOpen(webSocket, response)

            listener.onFailure(webSocket, RuntimeException("kaboom"), null)

            eventually(5.seconds) {
                verifySuspend {
                    socketLogManager.updateSocket(
                        matches<SocketConnection> {
                            it.id == 100L &&
                                it.status == SocketStatus.Failed &&
                                it.failureMessage == "kaboom"
                        },
                    )
                }
            }
        }
    }
})
