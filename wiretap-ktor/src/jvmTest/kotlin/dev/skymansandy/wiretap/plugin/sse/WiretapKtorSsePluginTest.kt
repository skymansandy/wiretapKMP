/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.plugin.sse

import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.matches
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.atLeast
import dev.mokkery.verify.VerifyMode.Companion.exactly
import dev.mokkery.verifySuspend
import dev.skymansandy.wiretap.domain.model.SseConnection
import dev.skymansandy.wiretap.domain.model.SseEvent
import dev.skymansandy.wiretap.domain.model.SseStatus
import dev.skymansandy.wiretap.domain.orchestrator.SocketLogManager
import dev.skymansandy.wiretap.domain.orchestrator.SseLogManager
import dev.skymansandy.wiretap.helper.markers.ExperimentalWiretapSseApi
import dev.skymansandy.wiretap.ktor.testing.EmbeddedTestServer
import dev.skymansandy.wiretap.ktor.testing.installTestKoin
import dev.skymansandy.wiretap.ktor.testing.teardownTestKoin
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java
import io.ktor.client.plugins.sse.SSE
import io.ktor.client.plugins.sse.sse
import io.ktor.client.request.get
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.sse.send
import io.ktor.server.sse.sse
import io.ktor.sse.ServerSentEvent
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalWiretapSseApi::class)
class WiretapKtorSsePluginTest : DescribeSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    val socketLogManager = mock<SocketLogManager>(MockMode.autoUnit)
    val sseLogManager = mock<SseLogManager>(MockMode.autoUnit)

    lateinit var server: EmbeddedTestServer
    lateinit var client: HttpClient

    fun newClient(enabled: Boolean = true): HttpClient = HttpClient(Java) {
        install(SSE)
        install(WiretapKtorSsePlugin) { this.enabled = enabled }
    }

    beforeEach {
        installTestKoin(socketLogManager, sseLogManager)
        everySuspend { sseLogManager.createConnection(any()) } returns 500L
        server = EmbeddedTestServer {
            sse("/events") {
                send(ServerSentEvent(data = "first", event = "msg", id = "1"))
                send(ServerSentEvent(data = "second"))
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
        it("creates an SseConnection and logs each incoming event") {
            runTest(timeout = 10.seconds) {
                val events = mutableListOf<String?>()
                client.sse("http://127.0.0.1:${server.port}/events") {
                    incoming.toList().forEach { events += it.data }
                }

                events shouldBe listOf("first", "second")
                eventually(5.seconds) {
                    verifySuspend(mode = atLeast(1)) {
                        sseLogManager.createConnection(
                            matches<SseConnection> { it.status == SseStatus.Open },
                        )
                    }
                    verifySuspend {
                        sseLogManager.logEvent(
                            matches<SseEvent> {
                                it.connectionId == 500L &&
                                    it.data == "first" &&
                                    it.eventType == "msg" &&
                                    it.eventId == "1"
                            },
                        )
                    }
                    verifySuspend {
                        sseLogManager.logEvent(matches<SseEvent> { it.data == "second" })
                    }
                }
            }
        }
    }

    describe("transparency (fixes #51 for SSE)") {
        it("captures events even when a third-party class owns the sse{} block") {
            runTest(timeout = 10.seconds) {
                val library = SseLibrary(client, "http://127.0.0.1:${server.port}/events")
                val events = library.collectAll()

                events shouldBe listOf("first", "second")
                eventually(5.seconds) {
                    verifySuspend(mode = atLeast(1)) {
                        sseLogManager.logEvent(matches<SseEvent> { it.data == "first" })
                    }
                    verifySuspend(mode = atLeast(1)) {
                        sseLogManager.logEvent(matches<SseEvent> { it.data == "second" })
                    }
                }
            }
        }
    }

    describe("plugin disabled") {
        it("skips all sse-log interactions when enabled = false") {
            runTest(timeout = 10.seconds) {
                runBlocking { client.close() }
                client = newClient(enabled = false)

                client.sse("http://127.0.0.1:${server.port}/events") {
                    incoming.toList()
                }

                verifySuspend(mode = exactly(0)) { sseLogManager.createConnection(any()) }
                verifySuspend(mode = exactly(0)) { sseLogManager.logEvent(any()) }
            }
        }
    }

    describe("plain HTTP passthrough") {
        it("does not create an SseConnection for a non-SSE response") {
            runTest(timeout = 10.seconds) {
                val response = client.get("http://127.0.0.1:${server.port}/plain")

                response.status.value shouldBe 200
                verifySuspend(mode = exactly(0)) { sseLogManager.createConnection(any()) }
            }
        }
    }
})

/**
 * Simulates a third-party SSE library that takes a Ktor [HttpClient] and
 * internally manages the `sse {}` block. The user never sees the raw session,
 * so they can't call `wiretapped()`. The plugin must still capture events.
 */
@OptIn(ExperimentalWiretapSseApi::class)
private class SseLibrary(
    private val client: HttpClient,
    private val url: String,
) {
    suspend fun collectAll(): List<String?> {
        val results = mutableListOf<String?>()
        client.sse(url) {
            incoming.toList().forEach { results += it.data }
        }
        return results
    }
}
