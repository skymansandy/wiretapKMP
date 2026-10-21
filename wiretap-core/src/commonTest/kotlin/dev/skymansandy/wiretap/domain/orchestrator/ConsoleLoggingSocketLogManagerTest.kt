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
import dev.skymansandy.wiretap.domain.model.SocketConnection
import dev.skymansandy.wiretap.domain.model.SocketContentType
import dev.skymansandy.wiretap.domain.model.SocketMessage
import dev.skymansandy.wiretap.domain.model.SocketMessageType
import dev.skymansandy.wiretap.domain.model.SocketStatus
import dev.skymansandy.wiretap.helper.logger.WiretapLogger
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest

class ConsoleLoggingSocketLogManagerTest : DescribeSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    val delegate = mock<SocketLogManager>(MockMode.autoUnit)
    val logger = mock<WiretapLogger>(MockMode.autoUnit)
    val manager: SocketLogManager = ConsoleLoggingSocketLogManager(delegate, logger)

    describe("createSocket") {
        it("returns the delegate's id and prints the connection stamped with it") {
            runTest {
                val entry = socketConnection()
                everySuspend { delegate.createSocket(entry) } returns 42L

                val id = manager.createSocket(entry)

                id shouldBe 42L
                verify { logger.logSocket(matches<SocketConnection> { it.id == 42L && it.url == "wss://x" }) }
            }
        }
    }

    describe("updateSocket") {
        it("records through the delegate and prints the connection") {
            runTest {
                val entry = socketConnection(id = 1, status = SocketStatus.Closed)

                manager.updateSocket(entry)

                verifySuspend { delegate.updateSocket(entry) }
                verify { logger.logSocket(entry) }
            }
        }
    }

    describe("logSocketMsg") {
        it("records through the delegate and prints the frame") {
            runTest {
                val message = socketMessage()

                manager.logSocketMsg(message)

                verifySuspend { delegate.logSocketMsg(message) }
                verify { logger.logSocketMessage(message) }
            }
        }
    }
})

private fun socketConnection(
    id: Long = 0,
    status: SocketStatus = SocketStatus.Open,
) = SocketConnection(
    id = id,
    url = "wss://x",
    status = status,
    timestamp = 0,
)

private fun socketMessage() = SocketMessage(
    socketId = 9,
    direction = SocketMessageType.Sent,
    contentType = SocketContentType.Text,
    content = "ping",
    byteCount = 4,
    timestamp = 0,
)
