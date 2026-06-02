/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.okhttp.sse

import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.matches
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.exactly
import dev.mokkery.verifySuspend
import dev.skymansandy.wiretap.domain.model.SseConnection
import dev.skymansandy.wiretap.domain.model.SseEvent
import dev.skymansandy.wiretap.domain.model.SseStatus
import dev.skymansandy.wiretap.domain.orchestrator.SseLogManager
import dev.skymansandy.wiretap.helper.markers.ExperimentalWiretapSseApi
import dev.skymansandy.wiretap.okhttp.testing.installTestKoin
import dev.skymansandy.wiretap.okhttp.testing.teardownTestKoin
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalWiretapSseApi::class)
class WiretapOkHttpEventSourceListenerTest : DescribeSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    val sseLogManager = mock<SseLogManager>(MockMode.autoUnit)
    val delegate = mock<EventSourceListener>(MockMode.autoUnit)

    val request = Request.Builder().url("http://test.local/sse").build()
    val response = Response.Builder()
        .request(request)
        .protocol(Protocol.HTTP_1_1)
        .code(200)
        .message("OK")
        .build()
    val eventSource = mock<EventSource>(MockMode.autoUnit)

    beforeEach {
        installTestKoin(sseLogManager)
        every { eventSource.request() } returns request
        everySuspend { sseLogManager.createConnection(any()) } returns 500L
    }

    afterEach {
        teardownTestKoin()
    }

    describe("onOpen") {
        it("creates an SseConnection with Open status and forwards the call") {
            val listener = delegate.wiretapped { enabled = true }

            listener.onOpen(eventSource, response)

            verifySuspend {
                sseLogManager.createConnection(
                    matches<SseConnection> {
                        it.status == SseStatus.Open && it.url == "http://test.local/sse"
                    },
                )
            }
        }

        it("skips logging entirely when enabled = false") {
            val listener = delegate.wiretapped { enabled = false }

            listener.onOpen(eventSource, response)

            verifySuspend(mode = exactly(0)) { sseLogManager.createConnection(any()) }
        }
    }

    describe("onEvent") {
        it("logs each event with the connection id and event metadata") {
            val listener = delegate.wiretapped { enabled = true }
            listener.onOpen(eventSource, response)

            listener.onEvent(eventSource, id = "42", type = "ping", data = "hello")

            eventually(5.seconds) {
                verifySuspend {
                    sseLogManager.logEvent(
                        matches<SseEvent> {
                            it.connectionId == 500L &&
                                it.data == "hello" &&
                                it.eventType == "ping" &&
                                it.eventId == "42"
                        },
                    )
                }
            }
        }

        it("does not log events when there is no active connection") {
            val listener = delegate.wiretapped { enabled = false }

            listener.onEvent(eventSource, id = "1", type = null, data = "x")

            verifySuspend(mode = exactly(0)) { sseLogManager.logEvent(any()) }
        }
    }

    describe("onClosed") {
        it("updates the connection status to Closed") {
            val listener = delegate.wiretapped { enabled = true }
            listener.onOpen(eventSource, response)

            listener.onClosed(eventSource)

            eventually(5.seconds) {
                verifySuspend {
                    sseLogManager.updateConnection(
                        matches<SseConnection> {
                            it.id == 500L && it.status == SseStatus.Closed
                        },
                    )
                }
            }
        }
    }

    describe("onFailure") {
        it("updates the connection status to Failed and records the message") {
            val listener = delegate.wiretapped { enabled = true }
            listener.onOpen(eventSource, response)

            listener.onFailure(eventSource, RuntimeException("boom"), null)

            eventually(5.seconds) {
                verifySuspend {
                    sseLogManager.updateConnection(
                        matches<SseConnection> {
                            it.id == 500L &&
                                it.status == SseStatus.Failed &&
                                it.failureMessage == "boom"
                        },
                    )
                }
            }
        }
    }
})
