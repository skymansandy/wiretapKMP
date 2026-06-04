/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.plugin.ws

import dev.mokkery.MockMode
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
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketExtension
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.readText
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.seconds

class LoggingRawWebSocketSessionTest : DescribeSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    val socketLogManager = mock<SocketLogManager>(MockMode.autoUnit)

    fun newWrapper(
        session: FakeWebSocketSession = FakeWebSocketSession(),
        socketId: Long = 42L,
        url: String = "wss://example.com/ws",
        binaryDecoding: BinaryFrameDecoding = BinaryFrameDecoding.Auto,
    ): Pair<LoggingRawWebSocketSession, FakeWebSocketSession> {
        val wrapper = LoggingRawWebSocketSession(
            delegate = session,
            socketId = socketId,
            url = url,
            socketLogManager = socketLogManager,
            binaryDecoding = binaryDecoding,
        )
        return wrapper to session
    }

    describe("send()") {
        it("logs a Text frame as Sent with the correct byteCount") {
            runTest {
                val (wrapper, fake) = newWrapper()

                wrapper.send(Frame.Text("hello"))

                eventually(5.seconds) {
                    verifySuspend {
                        socketLogManager.logSocketMsg(
                            matches<SocketMessage> {
                                it.socketId == 42L &&
                                    it.direction == SocketMessageType.Sent &&
                                    it.contentType == SocketContentType.Text &&
                                    it.content == "hello" &&
                                    it.byteCount == 5L
                            },
                        )
                    }
                }
                fake.outgoingChannel.tryReceive().getOrNull()
            }
        }

        it("logs a Binary frame with byte count and placeholder content") {
            runTest {
                val (wrapper, _) = newWrapper()
                val payload = ByteArray(7) { it.toByte() }

                wrapper.send(Frame.Binary(true, payload))

                eventually(5.seconds) {
                    verifySuspend {
                        socketLogManager.logSocketMsg(
                            matches<SocketMessage> {
                                it.direction == SocketMessageType.Sent &&
                                    it.contentType == SocketContentType.Binary &&
                                    it.content == "[Binary: 7 bytes]" &&
                                    it.byteCount == 7L
                            },
                        )
                    }
                }
            }
        }

        it("decodes a Binary frame as text when Auto mode sees printable UTF-8") {
            runTest {
                val (wrapper, _) = newWrapper()
                val payload = "{\"hub\":\"chat\"}".encodeToByteArray()

                wrapper.send(Frame.Binary(true, payload))

                eventually(5.seconds) {
                    verifySuspend {
                        socketLogManager.logSocketMsg(
                            matches<SocketMessage> {
                                it.contentType == SocketContentType.Binary &&
                                    it.content == "{\"hub\":\"chat\"}" &&
                                    it.byteCount == payload.size.toLong()
                            },
                        )
                    }
                }
            }
        }

        it("renders placeholder for printable UTF-8 when binaryDecoding is Placeholder") {
            runTest {
                val (wrapper, _) = newWrapper(binaryDecoding = BinaryFrameDecoding.Placeholder)
                val payload = "hello".encodeToByteArray()

                wrapper.send(Frame.Binary(true, payload))

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
        }

        it("forces UTF-8 decode when binaryDecoding is Utf8") {
            runTest {
                val (wrapper, _) = newWrapper(binaryDecoding = BinaryFrameDecoding.Utf8)
                val payload = "forced".encodeToByteArray()

                wrapper.send(Frame.Binary(true, payload))

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
        }

        it("delegates to the user-supplied decoder when binaryDecoding is Custom") {
            runTest {
                val custom = BinaryFrameDecoding.Custom { bytes -> "hex(${bytes.size})" }
                val (wrapper, _) = newWrapper(binaryDecoding = custom)

                wrapper.send(Frame.Binary(true, byteArrayOf(1, 2, 3)))

                eventually(5.seconds) {
                    verifySuspend {
                        socketLogManager.logSocketMsg(
                            matches<SocketMessage> {
                                it.contentType == SocketContentType.Binary &&
                                    it.content == "hex(3)" &&
                                    it.byteCount == 3L
                            },
                        )
                    }
                }
            }
        }

        it("logs Ping and Pong frames as control frames with empty content") {
            runTest {
                val (wrapper, _) = newWrapper()

                wrapper.send(Frame.Ping(byteArrayOf(1, 2, 3)))
                wrapper.send(Frame.Pong(byteArrayOf(4, 5)))

                eventually(5.seconds) {
                    verifySuspend {
                        socketLogManager.logSocketMsg(
                            matches<SocketMessage> {
                                it.contentType == SocketContentType.Ping &&
                                    it.content == "" &&
                                    it.byteCount == 3L
                            },
                        )
                    }
                    verifySuspend {
                        socketLogManager.logSocketMsg(
                            matches<SocketMessage> {
                                it.contentType == SocketContentType.Pong &&
                                    it.byteCount == 2L
                            },
                        )
                    }
                }
            }
        }

        it("logs a Close frame with the code and reason text") {
            runTest {
                val (wrapper, _) = newWrapper()

                wrapper.send(Frame.Close(CloseReason(1000.toShort(), "bye")))

                eventually(5.seconds) {
                    verifySuspend {
                        socketLogManager.logSocketMsg(
                            matches<SocketMessage> {
                                it.contentType == SocketContentType.Close &&
                                    it.content == "1000 bye"
                            },
                        )
                    }
                }
            }
        }
    }

    describe("incoming channel") {
        it("logs frames as Received as the consumer reads them") {
            runTest {
                val (wrapper, fake) = newWrapper()

                fake.incomingChannel.send(Frame.Text("hi"))
                val received = wrapper.incoming.receive()

                received.shouldBeText("hi")
                eventually(5.seconds) {
                    verifySuspend {
                        socketLogManager.logSocketMsg(
                            matches<SocketMessage> {
                                it.direction == SocketMessageType.Received &&
                                    it.contentType == SocketContentType.Text &&
                                    it.content == "hi"
                            },
                        )
                    }
                }
            }
        }

        it("auto-decodes an inbound text-over-binary frame as text") {
            runTest {
                val (wrapper, fake) = newWrapper()
                val payload = "{\"event\":\"ping\"}".encodeToByteArray()

                fake.incomingChannel.send(Frame.Binary(true, payload))
                wrapper.incoming.receive()

                eventually(5.seconds) {
                    verifySuspend {
                        socketLogManager.logSocketMsg(
                            matches<SocketMessage> {
                                it.direction == SocketMessageType.Received &&
                                    it.contentType == SocketContentType.Binary &&
                                    it.content == "{\"event\":\"ping\"}"
                            },
                        )
                    }
                }
            }
        }

        // Closure tests use distinct socket ids and ID-filtered matchers so they
        // stay precise even if Kotest's InstancePerLeaf isolation doesn't reset
        // mock state on every platform (observed on iosSimulatorArm64).
        it("reports a normal closure exactly once when the iterator observes the channel close") {
            runTest {
                val id = 500L
                val (wrapper, fake) = newWrapper(socketId = id)

                fake.incomingChannel.close()
                // Idiomatic webSocket {} consumer — iterates until the channel closes.
                for (ignored in wrapper.incoming) { /* drain */ }

                eventually(5.seconds) {
                    verifySuspend(mode = exactly(1)) {
                        socketLogManager.updateSocket(
                            matches<SocketConnection> {
                                it.id == id && it.status == SocketStatus.Closed
                            },
                        )
                    }
                }
            }
        }

        it("reports a failure when the channel closes with a non-cancellation cause") {
            runTest {
                val id = 600L
                val (wrapper, fake) = newWrapper(socketId = id)
                val boom = RuntimeException("kaboom")

                fake.incomingChannel.close(boom)
                fake.job.completeExceptionally(boom)
                runCatching { wrapper.incoming.receive() }

                eventually(5.seconds) {
                    verifySuspend {
                        socketLogManager.updateSocket(
                            matches<SocketConnection> {
                                it.id == id &&
                                    it.status == SocketStatus.Failed &&
                                    it.failureMessage == "kaboom"
                            },
                        )
                    }
                }
            }
        }
    }

    describe("outgoing channel") {
        it("logs frames as Sent as the consumer writes through outgoing.send()") {
            runTest {
                val (wrapper, fake) = newWrapper()

                wrapper.outgoing.send(Frame.Text("via-outgoing"))

                eventually(5.seconds) {
                    verifySuspend {
                        socketLogManager.logSocketMsg(
                            matches<SocketMessage> {
                                it.direction == SocketMessageType.Sent &&
                                    it.content == "via-outgoing"
                            },
                        )
                    }
                }
                fake.outgoingChannel.tryReceive().getOrNull()
            }
        }
    }

    describe("Job-completion closure guard") {
        it("does NOT report a closure when the delegate's Job completes with no cause while outgoing is still open") {
            runTest {
                val id = 700L
                val (_, fake) = newWrapper(socketId = id)

                fake.job.complete()

                // Give the log scope a chance to fire if the guard fails.
                kotlinx.coroutines.delay(200)
                verifySuspend(mode = exactly(0)) {
                    socketLogManager.updateSocket(matches<SocketConnection> { it.id == id })
                }
            }
        }

        it("reports a single closure even when both the Job and the incoming channel close") {
            runTest {
                val id = 800L
                val (wrapper, fake) = newWrapper(socketId = id)

                fake.incomingChannel.close()
                fake.outgoingChannel.close()
                fake.job.complete()
                runCatching { wrapper.incoming.receive() }

                eventually(5.seconds) {
                    verifySuspend(mode = exactly(1)) {
                        socketLogManager.updateSocket(
                            matches<SocketConnection> {
                                it.id == id && it.status == SocketStatus.Closed
                            },
                        )
                    }
                }
            }
        }
    }
})

private fun Frame.shouldBeText(expected: String) {
    require(this is Frame.Text) { "expected Text frame, got ${this::class.simpleName}" }
    require(readText() == expected) { "expected '$expected', got '${readText()}'" }
}

internal class FakeWebSocketSession(
    val job: CompletableJob = Job(),
    val incomingChannel: Channel<Frame> = Channel(Channel.UNLIMITED),
    val outgoingChannel: Channel<Frame> = Channel(Channel.UNLIMITED),
) : WebSocketSession {

    override val coroutineContext: CoroutineContext = job + Dispatchers.Unconfined
    override var masking: Boolean = true
    override var maxFrameSize: Long = Long.MAX_VALUE
    override val extensions: List<WebSocketExtension<*>> = emptyList()

    override val incoming: ReceiveChannel<Frame> get() = incomingChannel
    override val outgoing: SendChannel<Frame> get() = outgoingChannel

    override suspend fun flush() = Unit

    @Suppress("DEPRECATION_ERROR", "OVERRIDE_DEPRECATION")
    @Deprecated(
        "Use cancel() instead.",
        replaceWith = ReplaceWith("cancel()", "kotlinx.coroutines.cancel"),
        level = DeprecationLevel.ERROR,
    )
    override fun terminate() {
        job.cancel()
    }
}
