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
import dev.skymansandy.wiretap.domain.model.SocketConnection
import dev.skymansandy.wiretap.domain.model.SocketContentType
import dev.skymansandy.wiretap.domain.model.SocketMessage
import dev.skymansandy.wiretap.domain.model.SocketMessageType
import dev.skymansandy.wiretap.domain.model.SocketStatus
import dev.skymansandy.wiretap.domain.repository.SocketRepository
import dev.skymansandy.wiretap.helper.launcher.WiretapNotificationHook
import dev.skymansandy.wiretap.helper.logger.WiretapLogger
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest

class SocketLogManagerImplTest : DescribeSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    val repository = mock<SocketRepository>(MockMode.autoUnit)
    val logger = mock<WiretapLogger>(MockMode.autoUnit)
    val hook = mock<WiretapNotificationHook>(MockMode.autoUnit)
    val manager = SocketLogManagerImpl(repository, logger, hook)

    describe("flowAllSockets") {
        it("delegates to repository") {
            runTest {
                every { repository.flowAll() } returns flowOf(emptyList())
                manager.flowAllSockets()
                verify { repository.flowAll() }
            }
        }
    }

    describe("flowSocketById") {
        it("delegates to repository") {
            runTest {
                every { repository.flowById(7) } returns flowOf(null)
                manager.flowSocketById(7)
                verify { repository.flowById(7) }
            }
        }
    }

    describe("flowSocketMessagesById") {
        it("delegates to repository") {
            runTest {
                every { repository.flowMessagesForId(7) } returns flowOf(emptyList())
                manager.flowSocketMessagesById(7)
                verify { repository.flowMessagesForId(7) }
            }
        }
    }

    describe("flowPagedSocketsForSearchQuery") {
        it("delegates query through to repository") {
            runTest {
                every { repository.flowForSearchQuery("q") } returns flowOf()
                manager.flowPagedSocketsForSearchQuery("q")
                verify { repository.flowForSearchQuery("q") }
            }
        }
    }

    describe("getSocketById") {
        it("delegates to repository") {
            runTest {
                everySuspend { repository.getById(5) } returns connection(id = 5)
                manager.getSocketById(5)?.id shouldBe 5L
            }
        }
    }

    describe("createSocket") {
        it("logs, copies the assigned id into the entry, and notifies hook") {
            runTest {
                val entry = connection(url = "wss://x")
                everySuspend { repository.logNew(entry) } returns 42L

                val id = manager.createSocket(entry)

                id shouldBe 42L
                verify { logger.logSocket(matches<SocketConnection> { it.id == 42L && it.url == "wss://x" }) }
                verify { hook.onNewSocketConnection(matches<SocketConnection> { it.id == 42L }) }
            }
        }
    }

    describe("updateSocket") {
        it("forwards through repository, logger, hook") {
            runTest {
                val entry = connection(id = 1, status = SocketStatus.Open)

                manager.updateSocket(entry)

                verifySuspend { repository.update(entry) }
                verify { logger.logSocket(entry) }
                verify { hook.onNewSocketConnection(entry) }
            }
        }

        it("with Closed status evicts the cached entry") {
            runTest {
                // Seed cache.
                everySuspend { repository.logNew(any()) } returns 9L
                val open = connection(id = 0, url = "wss://x", status = SocketStatus.Open)
                manager.createSocket(open)

                // Close it.
                manager.updateSocket(connection(id = 9, url = "wss://x", status = SocketStatus.Closed))

                // Now if we logSocketMsg with a missing entry and no cache, no reopen should fire.
                everySuspend { repository.getById(9) } returns null
                manager.logSocketMsg(message(socketId = 9, content = "x"))

                // markReopened was never called — proof the cache was evicted.
                verifySuspend(mode = dev.mokkery.verify.VerifyMode.exactly(0)) {
                    repository.markReopened(any())
                }
            }
        }
    }

    describe("logSocketMsg") {
        it("without active cache inserts the message and notifies after re-fetch") {
            runTest {
                val msg = message(socketId = 9, content = "hi")
                // First lookup before logging — entry exists (already persisted), so no reopen flow.
                // Second lookup after logging is used for the hook notification.
                everySuspend { repository.getById(9) } returns connection(id = 9, url = "wss://x")

                manager.logSocketMsg(msg)

                verifySuspend { repository.logSocketMsg(msg) }
                verify { logger.logSocketMessage(msg) }
                verify { hook.onNewSocketMessage(any(), msg) }
            }
        }

        it("with active cache re-opens connection when entry is missing") {
            runTest {
                val cached = connection(id = 9, url = "wss://x", status = SocketStatus.Open)

                // Seed the active-connections cache by going through createSocket.
                everySuspend { repository.logNew(any()) } returns 9L
                manager.createSocket(cached.copy(id = 0))

                // Now the entry is missing on first lookup (cleared) but the cache says it's still active.
                // Use sequential answers: first call returns null (triggers reopen), second returns the
                // re-persisted entry used for the hook notification.
                everySuspend { repository.getById(9) } sequentially {
                    returns(null)
                    returns(connection(id = 9, url = "wss://x"))
                }

                manager.logSocketMsg(message(socketId = 9, content = "ping"))

                verifySuspend { repository.markReopened(matches<SocketConnection> { it.id == 9L && it.historyCleared }) }
                verify { hook.onNewSocketConnection(matches<SocketConnection> { it.id == 9L && it.historyCleared }) }
            }
        }

        it("with no cache and missing entry still inserts the message but skips hook") {
            runTest {
                // Entry missing and no cached entry — should still log the message itself.
                everySuspend { repository.getById(9) } returns null

                manager.logSocketMsg(message(socketId = 9, content = "hi"))

                verifySuspend { repository.logSocketMsg(any()) }
                verify { logger.logSocketMessage(any()) }
            }
        }
    }

    describe("clearLogs") {
        it("delegates to repository and notifies hook") {
            runTest {
                manager.clearLogs()

                verifySuspend { repository.clearAll() }
                verify { hook.onClearSocketLogs() }
            }
        }
    }
})

private fun connection(
    id: Long = 0,
    url: String = "wss://x",
    status: SocketStatus = SocketStatus.Open,
) = SocketConnection(
    id = id,
    url = url,
    status = status,
    timestamp = 0,
)

private fun message(socketId: Long, content: String) = SocketMessage(
    socketId = socketId,
    direction = SocketMessageType.Sent,
    contentType = SocketContentType.Text,
    content = content,
    byteCount = content.length.toLong(),
    timestamp = 0,
)
