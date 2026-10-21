/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.domain.orchestrator

import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.matches
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verifySuspend
import dev.skymansandy.wiretap.domain.model.SseConnection
import dev.skymansandy.wiretap.domain.model.SseEvent
import dev.skymansandy.wiretap.domain.model.SseStatus
import dev.skymansandy.wiretap.helper.logger.WiretapLogger
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest

class ConsoleLoggingSseLogManagerTest : DescribeSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    val delegate = mock<SseLogManager>(MockMode.autoUnit)
    val logger = mock<WiretapLogger>(MockMode.autoUnit)
    val manager: SseLogManager = ConsoleLoggingSseLogManager(delegate, logger)

    describe("createConnection") {
        it("returns the delegate's id and forwards the connection stamped with it") {
            runTest {
                val entry = sseConnection()
                everySuspend { delegate.createConnection(entry) } returns 42L

                val id = manager.createConnection(entry)

                id shouldBe 42L
                verify {
                    logger.logSse(matches<SseConnection> { it.id == 42L && it.url == "https://x/events" })
                }
            }
        }
    }

    describe("updateConnection") {
        it("records through the delegate and forwards the connection") {
            runTest {
                val entry = sseConnection(id = 1, status = SseStatus.Closed)

                manager.updateConnection(entry)

                verifySuspend { delegate.updateConnection(entry) }
                verify { logger.logSse(entry) }
            }
        }
    }

    describe("logEvent") {
        it("records through the delegate and forwards the event") {
            runTest {
                val event = sseEvent()

                manager.logEvent(event)

                verifySuspend { delegate.logEvent(event) }
                verify { logger.logSseEvent(event) }
            }
        }
    }
})

private fun sseConnection(
    id: Long = 0,
    status: SseStatus = SseStatus.Open,
) = SseConnection(id = id, url = "https://x/events", status = status, timestamp = 0)

private fun sseEvent() = SseEvent(
    connectionId = 9,
    data = "tick",
    byteCount = 4,
    timestamp = 0,
)
