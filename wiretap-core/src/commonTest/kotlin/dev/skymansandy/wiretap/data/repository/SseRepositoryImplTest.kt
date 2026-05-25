/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.data.repository

import app.cash.turbine.test
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.matches
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import dev.skymansandy.wiretap.data.db.room.dao.SseLogsDao
import dev.skymansandy.wiretap.data.db.room.entity.SseEventEntity
import dev.skymansandy.wiretap.data.db.room.entity.SseLogEntity
import dev.skymansandy.wiretap.domain.model.SseConnection
import dev.skymansandy.wiretap.domain.model.SseEvent
import dev.skymansandy.wiretap.domain.model.SseStatus
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest

class SseRepositoryImplTest : DescribeSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    val dao = mock<SseLogsDao>(MockMode.autoUnit)
    val repo = SseRepositoryImpl(dao)

    describe("flowAll") {
        it("maps entities to domain") {
            runTest {
                every { dao.getAllSseLogs() } returns flowOf(listOf(sseEntity(1)))

                repo.flowAll().test {
                    awaitItem().first().id shouldBe 1L
                    awaitComplete()
                }
            }
        }
    }

    describe("flowEventsForId") {
        it("maps every event entity") {
            runTest {
                every { dao.getSseEventsByConnectionId(7) } returns flowOf(
                    listOf(eventEntity(connectionId = 7, data = "payload")),
                )

                repo.flowEventsForId(7).test {
                    val events = awaitItem()
                    events.size shouldBe 1
                    events[0].connectionId shouldBe 7L
                    events[0].data shouldBe "payload"
                    awaitComplete()
                }
            }
        }
    }

    describe("flowById") {
        it("emits on subscription and maps to domain") {
            runTest {
                everySuspend { dao.getSseLogById(3) } returns sseEntity(3)

                repo.flowById(3).test {
                    awaitItem()?.id shouldBe 3L
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    describe("getById") {
        it("returns null when dao returns null") {
            runTest {
                everySuspend { dao.getSseLogById(99) } returns null
                repo.getById(99) shouldBe null
            }
        }
    }

    describe("logNew") {
        it("returns the id from the dao") {
            runTest {
                everySuspend { dao.insertSseLog(any()) } returns 55L

                val id = repo.logNew(SseConnection(url = "x", timestamp = 0))

                id shouldBe 55L
            }
        }
    }

    describe("markReopened") {
        it("upserts via insertSseLogWithId") {
            runTest {
                repo.markReopened(SseConnection(url = "x", timestamp = 0))
                verifySuspend { dao.insertSseLogWithId(any()) }
            }
        }
    }

    describe("update") {
        it("passes populated fields to the dao update query") {
            runTest {
                val conn = SseConnection(
                    id = 5,
                    url = "x",
                    status = SseStatus.Failed,
                    failureMessage = "boom",
                    closedAt = 99L,
                    lastEventId = "evt-1",
                    retryMs = 500L,
                    timestamp = 0,
                )

                repo.update(conn)

                verifySuspend {
                    dao.updateSseLog(
                        status = "Failed",
                        failureMessage = "boom",
                        closedAt = 99L,
                        lastEventId = "evt-1",
                        retryMs = 500L,
                        id = 5,
                    )
                }
            }
        }
    }

    describe("logEvent") {
        it("inserts the event and increments the count") {
            runTest {
                val event = SseEvent(
                    connectionId = 9,
                    eventType = "message",
                    data = "{}",
                    eventId = "evt",
                    retryMs = 100L,
                    byteCount = 2,
                    timestamp = 0,
                )

                repo.logEvent(event)

                verifySuspend {
                    dao.insertSseEvent(
                        matches<SseEventEntity> {
                            it.connectionId == 9L &&
                                it.eventType == "message" &&
                                it.data == "{}" &&
                                it.eventId == "evt" &&
                                it.retryMs == 100L &&
                                it.byteCount == 2L
                        },
                    )
                }
                verifySuspend { dao.incrementSseEventCount(9) }
            }
        }
    }

    describe("clearAll") {
        it("deletes events and logs") {
            runTest {
                repo.clearAll()
                verifySuspend { dao.deleteAllSseEvents() }
                verifySuspend { dao.deleteAllSseLogs() }
            }
        }
    }

    describe("clearClosed") {
        it("deletes closed events and logs") {
            runTest {
                repo.clearClosed()
                verifySuspend { dao.deleteClosedSseEvents() }
                verifySuspend { dao.deleteClosedSseLogs() }
            }
        }
    }
})

private fun sseEntity(id: Long) = SseLogEntity(
    id = id,
    url = "x",
    requestHeaders = "",
    status = "Open",
    timestamp = 0,
)

private fun eventEntity(connectionId: Long, data: String) = SseEventEntity(
    connectionId = connectionId,
    data = data,
    byteCount = data.length.toLong(),
    timestamp = 0,
)
