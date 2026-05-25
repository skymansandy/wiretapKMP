/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.domain.orchestrator

import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.answering.sequentially
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.matches
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verifySuspend
import dev.skymansandy.wiretap.domain.model.SseConnection
import dev.skymansandy.wiretap.domain.model.SseEvent
import dev.skymansandy.wiretap.domain.model.SseStatus
import dev.skymansandy.wiretap.domain.repository.SseRepository
import dev.skymansandy.wiretap.helper.launcher.WiretapNotificationHook
import dev.skymansandy.wiretap.helper.logger.WiretapLogger
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest

class SseLogManagerImplTest : DescribeSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    val repository = mock<SseRepository>(MockMode.autoUnit)
    val logger = mock<WiretapLogger>(MockMode.autoUnit)
    val hook = mock<WiretapNotificationHook>(MockMode.autoUnit)
    val manager = SseLogManagerImpl(repository, logger, hook)

    describe("flowAllConnections") {
        it("delegates to repository") {
            runTest {
                every { repository.flowAll() } returns flowOf(emptyList())
                manager.flowAllConnections()
                verify { repository.flowAll() }
            }
        }
    }

    describe("flowConnectionById") {
        it("delegates to repository") {
            runTest {
                every { repository.flowById(7) } returns flowOf(null)
                manager.flowConnectionById(7)
                verify { repository.flowById(7) }
            }
        }
    }

    describe("flowEventsById") {
        it("delegates to repository") {
            runTest {
                every { repository.flowEventsForId(7) } returns flowOf(emptyList())
                manager.flowEventsById(7)
                verify { repository.flowEventsForId(7) }
            }
        }
    }

    describe("flowPagedConnectionsForSearchQuery") {
        it("passes query through") {
            runTest {
                every { repository.flowForSearchQuery("q") } returns flowOf()
                manager.flowPagedConnectionsForSearchQuery("q")
                verify { repository.flowForSearchQuery("q") }
            }
        }
    }

    describe("getConnectionById") {
        it("delegates to repository") {
            runTest {
                everySuspend { repository.getById(5) } returns conn(id = 5)
                manager.getConnectionById(5)?.id shouldBe 5L
            }
        }
    }

    describe("createConnection") {
        it("assigns id and notifies logger and hook") {
            runTest {
                val entry = conn(url = "https://x/events")
                everySuspend { repository.logNew(entry) } returns 42L

                val id = manager.createConnection(entry)

                id shouldBe 42L
                verify { logger.logSse(matches<SseConnection> { it.id == 42L && it.url == "https://x/events" }) }
                verify { hook.onNewSseConnection(matches<SseConnection> { it.id == 42L }) }
            }
        }
    }

    describe("updateConnection") {
        it("forwards through repository, logger, hook") {
            runTest {
                val entry = conn(id = 1, status = SseStatus.Open)

                manager.updateConnection(entry)

                verifySuspend { repository.update(entry) }
                verify { logger.logSse(entry) }
                verify { hook.onNewSseConnection(entry) }
            }
        }

        it("with Closed status evicts cached entry") {
            runTest {
                // Seed cache.
                everySuspend { repository.logNew(any()) } returns 9L
                manager.createConnection(conn(id = 0, status = SseStatus.Open))

                // Close it.
                manager.updateConnection(conn(id = 9, status = SseStatus.Closed))

                // Now log an event with the entry missing and no cache — markReopened must NOT fire.
                everySuspend { repository.getById(9) } returns null
                manager.logEvent(event(connectionId = 9, data = "data"))

                verifySuspend(mode = dev.mokkery.verify.VerifyMode.exactly(0)) {
                    repository.markReopened(any())
                }
            }
        }
    }

    describe("logEvent") {
        it("with existing connection logs and notifies after re-fetch") {
            runTest {
                val event = event(connectionId = 9, data = "payload")
                everySuspend { repository.getById(9) } returns conn(id = 9)

                manager.logEvent(event)

                verifySuspend { repository.logEvent(event) }
                verify { logger.logSseEvent(event) }
                verify { hook.onNewSseEvent(any(), event) }
            }
        }

        it("re-opens cached connection when entry is missing") {
            runTest {
                val cached = conn(id = 9, url = "https://x", status = SseStatus.Open)

                // Seed cache.
                everySuspend { repository.logNew(any()) } returns 9L
                manager.createConnection(cached.copy(id = 0))

                // Entry vanishes — first lookup null triggers reopen, second returns reopened entry
                everySuspend { repository.getById(9) } sequentially {
                    returns(null)
                    returns(conn(id = 9, url = "https://x"))
                }

                manager.logEvent(event(connectionId = 9, data = "payload"))

                verifySuspend { repository.markReopened(matches<SseConnection> { it.id == 9L && it.historyCleared }) }
                verify { hook.onNewSseConnection(matches<SseConnection> { it.id == 9L && it.historyCleared }) }
            }
        }
    }

    describe("clearLogs") {
        it("delegates to repository and notifies hook") {
            runTest {
                manager.clearLogs()

                verifySuspend { repository.clearAll() }
                verify { hook.onClearSseLogs() }
            }
        }
    }
})

private fun conn(
    id: Long = 0,
    url: String = "https://x/events",
    status: SseStatus = SseStatus.Open,
) = SseConnection(id = id, url = url, status = status, timestamp = 0)

private fun event(connectionId: Long, data: String) = SseEvent(
    connectionId = connectionId,
    data = data,
    byteCount = data.length.toLong(),
    timestamp = 0,
)
