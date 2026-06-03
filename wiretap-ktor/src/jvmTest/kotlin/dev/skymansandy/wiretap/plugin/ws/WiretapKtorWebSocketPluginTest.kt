/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.plugin.ws

import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.matches
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.atLeast
import dev.mokkery.verify.VerifyMode.Companion.exactly
import dev.mokkery.verifySuspend
import dev.skymansandy.wiretap.domain.model.SocketConnection
import dev.skymansandy.wiretap.domain.model.SocketContentType
import dev.skymansandy.wiretap.domain.model.SocketMessage
import dev.skymansandy.wiretap.domain.model.SocketMessageType
import dev.skymansandy.wiretap.domain.model.SocketStatus
import dev.skymansandy.wiretap.domain.orchestrator.SocketLogManager
import dev.skymansandy.wiretap.domain.orchestrator.SseLogManager
import dev.skymansandy.wiretap.ktor.testing.EmbeddedTestServer
import dev.skymansandy.wiretap.ktor.testing.installTestKoin
import dev.skymansandy.wiretap.ktor.testing.teardownTestKoin
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.get
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlin.time.Duration.Companion.seconds
import io.ktor.server.websocket.webSocket as serverWebSocket

class WiretapKtorWebSocketPluginTest : DescribeSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    val socketLogManager = mock<SocketLogManager>(MockMode.autoUnit)
    val sseLogManager = mock<SseLogManager>(MockMode.autoUnit)

    lateinit var server: EmbeddedTestServer
    lateinit var client: HttpClient

    fun newClient(enabled: Boolean = true): HttpClient = HttpClient(Java) {
        install(WebSockets)
        install(WiretapKtorWebSocketPlugin) { this.enabled = enabled }
    }

    beforeEach {
        installTestKoin(socketLogManager, sseLogManager)
        everySuspend { socketLogManager.createSocket(any()) } returns 100L
        server = EmbeddedTestServer {
            serverWebSocket("/echo") {
                for (frame in incoming) {
                    if (frame is Frame.Text) send(Frame.Text(frame.readText()))
                }
            }
            get("/plain") { call.respondText("hello") }
        }
        client = newClient()
    }

    afterEach {
        runBlocking { client.close() }
        server.stop()
        teardownTestKoin()
    }

    describe("end-to-end roundtrip") {
        it("creates a SocketConnection on upgrade and logs sent + received frames") {
            runTest(timeout = 10.seconds) {
                client.webSocket("ws://127.0.0.1:${server.port}/echo") {
                    send(Frame.Text("ping"))
                    val reply = (incoming.receive() as Frame.Text).readText()
                    reply shouldBe "ping"
                }

                eventually(5.seconds) {
                    verifySuspend(mode = atLeast(1)) {
                        socketLogManager.createSocket(
                            matches<SocketConnection> {
                                it.status == SocketStatus.Open &&
                                    it.url == "ws://127.0.0.1:${server.port}/echo"
                            },
                        )
                    }
                    verifySuspend(mode = atLeast(1)) {
                        socketLogManager.logSocketMsg(
                            matches<SocketMessage> {
                                it.socketId == 100L &&
                                    it.direction == SocketMessageType.Sent &&
                                    it.contentType == SocketContentType.Text &&
                                    it.content == "ping"
                            },
                        )
                    }
                    verifySuspend(mode = atLeast(1)) {
                        socketLogManager.logSocketMsg(
                            matches<SocketMessage> {
                                it.direction == SocketMessageType.Received && it.content == "ping"
                            },
                        )
                    }
                }
            }
        }
    }

    describe("transparency (fixes #51)") {
        it("captures frames even when a third-party class owns the webSocket{} block") {
            runTest(timeout = 10.seconds) {
                // Simulates the SignalRKore pattern: a library wraps webSocket{} internally
                // and the user never calls wiretapped(). The plugin should still log frames.
                val library = SocketLibrary(client, "ws://127.0.0.1:${server.port}/echo")
                val reply = library.sendAndAwait("from-library")

                reply shouldBe "from-library"
                eventually(5.seconds) {
                    verifySuspend(mode = atLeast(1)) {
                        socketLogManager.logSocketMsg(
                            matches<SocketMessage> {
                                it.direction == SocketMessageType.Sent && it.content == "from-library"
                            },
                        )
                    }
                    verifySuspend(mode = atLeast(1)) {
                        socketLogManager.logSocketMsg(
                            matches<SocketMessage> {
                                it.direction == SocketMessageType.Received && it.content == "from-library"
                            },
                        )
                    }
                }
            }
        }
    }

    describe("plugin disabled") {
        it("skips all socket-log interactions when enabled = false") {
            runTest(timeout = 10.seconds) {
                runBlocking { client.close() }
                client = newClient(enabled = false)

                client.webSocket("ws://127.0.0.1:${server.port}/echo") {
                    send(Frame.Text("ignored"))
                    incoming.receive()
                }

                verifySuspend(mode = exactly(0)) { socketLogManager.createSocket(any()) }
                verifySuspend(mode = exactly(0)) { socketLogManager.logSocketMsg(any()) }
            }
        }
    }

    describe("plain HTTP passthrough") {
        it("does not create a SocketConnection for a non-upgrade response") {
            runTest(timeout = 10.seconds) {
                val response = client.get("http://127.0.0.1:${server.port}/plain")

                response.status.value shouldBe 200
                verifySuspend(mode = exactly(0)) { socketLogManager.createSocket(any()) }
            }
        }
    }

    describe("concurrent sessions") {
        it("creates a distinct SocketConnection per session") {
            runTest(timeout = 10.seconds) {
                coroutineScope {
                    repeat(3) { index ->
                        launch {
                            client.webSocket("ws://127.0.0.1:${server.port}/echo") {
                                send(Frame.Text("hello-$index"))
                                incoming.receive()
                            }
                        }
                    }
                }

                eventually(5.seconds) {
                    verifySuspend(mode = exactly(3)) { socketLogManager.createSocket(any()) }
                }
            }
        }
    }
})

/**
 * Simulates a third-party WebSocket library (e.g. SignalRKore) that takes a
 * Ktor [HttpClient] and internally manages the `webSocket {}` block. The user
 * never sees the raw session, so they can't call `wiretapped()`. The plugin
 * must still capture frames.
 */
private class SocketLibrary(
    private val client: HttpClient,
    private val url: String,
) {
    suspend fun sendAndAwait(payload: String): String {
        var reply = ""
        client.webSocket(url) {
            send(Frame.Text(payload))
            reply = (incoming.receive() as Frame.Text).readText()
        }
        return reply
    }
}
