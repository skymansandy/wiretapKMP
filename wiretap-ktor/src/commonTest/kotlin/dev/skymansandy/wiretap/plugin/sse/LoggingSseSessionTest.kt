/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.plugin.sse

import dev.mokkery.MockMode
import dev.mokkery.matcher.matches
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.exactly
import dev.mokkery.verifySuspend
import dev.skymansandy.wiretap.domain.model.SseConnection
import dev.skymansandy.wiretap.domain.model.SseEvent
import dev.skymansandy.wiretap.domain.model.SseStatus
import dev.skymansandy.wiretap.domain.orchestrator.SseLogManager
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.ktor.client.plugins.sse.SSESession
import io.ktor.sse.ServerSentEvent
import io.ktor.utils.io.InternalAPI
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.seconds

class LoggingSseSessionTest : DescribeSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    val sseLogManager = mock<SseLogManager>(MockMode.autoUnit)

    fun newWrapper(
        events: Flow<ServerSentEvent>,
        connectionId: Long,
        cause: Throwable? = null,
        url: String = "https://example.com/stream",
    ): LoggingSseSession {
        val delegate = FakeSseSession(
            incomingFlow = if (cause == null) {
                events
            } else {
                flow {
                    events.collect { emit(it) }
                    throw cause
                }
            },
        )
        return LoggingSseSession(
            delegate = delegate,
            connectionId = connectionId,
            url = url,
            sseLogManager = sseLogManager,
        )
    }

    describe("incoming flow") {
        // Tests use distinct connection ids so verify matchers stay precise even
        // if Kotest's InstancePerLeaf isolation doesn't reset the mock state on
        // every platform (observed on iosSimulatorArm64).
        it("logs each emitted event with its data and byteCount") {
            runTest {
                val id = 100L
                val wrapper = newWrapper(
                    connectionId = id,
                    events = flow {
                        emit(ServerSentEvent(data = "first", event = "msg", id = "1"))
                        emit(ServerSentEvent(data = "second"))
                    },
                )

                wrapper.incoming.toList()

                eventually(5.seconds) {
                    verifySuspend {
                        sseLogManager.logEvent(
                            matches<SseEvent> {
                                it.connectionId == id &&
                                    it.data == "first" &&
                                    it.eventType == "msg" &&
                                    it.eventId == "1" &&
                                    it.byteCount == 5L
                            },
                        )
                    }
                    verifySuspend {
                        sseLogManager.logEvent(
                            matches<SseEvent> {
                                it.connectionId == id &&
                                    it.data == "second" &&
                                    it.eventType == null &&
                                    it.byteCount == 6L
                            },
                        )
                    }
                }
            }
        }

        it("skips events whose data is null") {
            runTest {
                val id = 200L
                val wrapper = newWrapper(
                    connectionId = id,
                    events = flow { emit(ServerSentEvent(data = null)) },
                )

                wrapper.incoming.toList()

                verifySuspend(mode = exactly(0)) {
                    sseLogManager.logEvent(matches<SseEvent> { it.connectionId == id })
                }
            }
        }

        it("marks the connection Closed when the flow completes normally") {
            runTest {
                val id = 300L
                val wrapper = newWrapper(
                    connectionId = id,
                    events = flow { emit(ServerSentEvent(data = "x")) },
                )

                wrapper.incoming.toList()

                eventually(5.seconds) {
                    verifySuspend(mode = exactly(1)) {
                        sseLogManager.updateConnection(
                            matches<SseConnection> {
                                it.id == id && it.status == SseStatus.Closed
                            },
                        )
                    }
                }
            }
        }

        it("marks the connection Failed with the cause message when the flow throws") {
            runTest {
                val id = 400L
                val wrapper = newWrapper(
                    connectionId = id,
                    events = flow { emit(ServerSentEvent(data = "ok")) },
                    cause = IllegalStateException("stream broke"),
                )

                runCatching { wrapper.incoming.toList() }

                eventually(5.seconds) {
                    verifySuspend {
                        sseLogManager.updateConnection(
                            matches<SseConnection> {
                                it.id == id &&
                                    it.status == SseStatus.Failed &&
                                    it.failureMessage == "stream broke"
                            },
                        )
                    }
                }
            }
        }
    }
})

@OptIn(InternalAPI::class)
internal class FakeSseSession(
    private val incomingFlow: Flow<ServerSentEvent>,
    job: CompletableJob = Job(),
) : SSESession {

    override val coroutineContext: CoroutineContext = job + Dispatchers.Unconfined
    override val incoming: Flow<ServerSentEvent> = incomingFlow

    override fun bodyBuffer(): ByteArray = ByteArray(0)
}
