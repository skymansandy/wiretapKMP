/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.domain.orchestrator

import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.matches
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verifySuspend
import dev.skymansandy.wiretap.domain.model.HttpLog
import dev.skymansandy.wiretap.helper.logger.WiretapLogger
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.flowOf

class ConsoleLoggingHttpLogManagerTest : DescribeSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    val delegate = mock<HttpLogManager>(MockMode.autoUnit)
    val logger = mock<WiretapLogger>(MockMode.autoUnit)
    val manager: HttpLogManager = ConsoleLoggingHttpLogManager(delegate, logger)

    describe("logHttp") {
        it("records through the delegate and prints the entry") {
            val entry = httpLogEntry()

            manager.logHttp(entry)

            verifySuspend { delegate.logHttp(entry) }
            verify { logger.logHttp(entry) }
        }
    }

    describe("logHttpAndGetId") {
        it("returns the delegate's id and prints the entry stamped with it") {
            val entry = httpLogEntry()
            everySuspend { delegate.logHttpAndGetId(entry) } returns 99L

            val id = manager.logHttpAndGetId(entry)

            id shouldBe 99L
            verify { logger.logHttp(matches<HttpLog> { it.id == 99L && it.url == "https://x" }) }
        }
    }

    describe("updateHttp") {
        it("records through the delegate and prints the entry") {
            val entry = httpLogEntry()

            manager.updateHttp(entry)

            verifySuspend { delegate.updateHttp(entry) }
            verify { logger.logHttp(entry) }
        }
    }

    describe("reads") {
        it("pass straight through without printing") {
            every { delegate.flowHttpLogs() } returns flowOf(emptyList())

            manager.flowHttpLogs()

            verify { delegate.flowHttpLogs() }
            verify(mode = dev.mokkery.verify.VerifyMode.exactly(0)) { logger.logHttp(any()) }
        }
    }
})

private fun httpLogEntry() = HttpLog(
    url = "https://x",
    method = "GET",
    timestamp = 0,
)
