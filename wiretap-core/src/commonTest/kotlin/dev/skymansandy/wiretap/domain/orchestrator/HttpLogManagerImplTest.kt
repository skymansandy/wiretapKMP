/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.domain.orchestrator

import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.matches
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verifySuspend
import dev.skymansandy.wiretap.domain.model.HttpLog
import dev.skymansandy.wiretap.domain.model.HttpLogFilter
import dev.skymansandy.wiretap.domain.repository.HttpRepository
import dev.skymansandy.wiretap.helper.launcher.WiretapNotificationHook
import dev.skymansandy.wiretap.helper.logger.WiretapLogger
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.flowOf

class HttpLogManagerImplTest : DescribeSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    val repository = mock<HttpRepository>(MockMode.autoUnit)
    val logger = mock<WiretapLogger>(MockMode.autoUnit)
    val hook = mock<WiretapNotificationHook>(MockMode.autoUnit)
    val manager = HttpLogManagerImpl(repository, logger, hook)

    describe("flowHttpLogs") {
        it("delegates to repository") {
            every { repository.flowAll() } returns flowOf(emptyList())

            manager.flowHttpLogs()

            verify { repository.flowAll() }
        }
    }

    describe("flowDistinctHosts") {
        it("delegates to repository") {
            every { repository.flowDistinctHosts() } returns flowOf(emptyList())

            manager.flowDistinctHosts()

            verify { repository.flowDistinctHosts() }
        }
    }

    describe("flowHttpLogById") {
        it("delegates to repository") {
            every { repository.flowById(7) } returns flowOf(null)

            manager.flowHttpLogById(7)

            verify { repository.flowById(7) }
        }
    }

    describe("flowPagedHttpLogsForSearchQuery") {
        it("passes query and filter through") {
            val filter = HttpLogFilter()
            every { repository.flowPagesLogs("q", filter) } returns flowOf()

            manager.flowPagedHttpLogsForSearchQuery("q", filter)

            verify { repository.flowPagesLogs("q", filter) }
        }
    }

    describe("logHttp") {
        it("calls repository save, logger and hook in order") {
            val entry = httpLog(url = "https://x")

            manager.logHttp(entry)

            verifySuspend { repository.save(entry) }
            verify { logger.logHttp(entry) }
            verify { hook.onNewHttpLog(entry) }
        }
    }

    describe("logHttpAndGetId") {
        it("returns the id and forwards a copy with id to logger and hook") {
            val original = httpLog(url = "https://x")
            everySuspend { repository.saveAndGetId(original) } returns 99L

            val id = manager.logHttpAndGetId(original)

            id shouldBe 99L
            verify { logger.logHttp(matches<HttpLog> { it.id == 99L && it.url == "https://x" }) }
            verify { hook.onNewHttpLog(matches<HttpLog> { it.id == 99L && it.url == "https://x" }) }
        }
    }

    describe("updateHttp") {
        it("delegates to repository, logger and hook") {
            val entry = httpLog(url = "https://x")

            manager.updateHttp(entry)

            verifySuspend { repository.update(entry) }
            verify { logger.logHttp(entry) }
            verify { hook.onNewHttpLog(entry) }
        }
    }

    describe("getHttpLogById") {
        it("delegates to repository") {
            everySuspend { repository.getById(7) } returns httpLog().copy(id = 7)

            manager.getHttpLogById(7)?.id shouldBe 7L
        }
    }

    describe("deleteHttpLog") {
        it("notifies hook with the deleted id") {
            manager.deleteHttpLog(11)

            verifySuspend { repository.deleteById(11) }
            verify { hook.onDeleteHttpLog(11) }
        }
    }

    describe("clearHttpLogs") {
        it("clears the repository and notifies the hook") {
            manager.clearHttpLogs()

            verifySuspend { repository.clearAll() }
            verify { hook.onClearHttpLogs() }
        }
    }

    describe("purgeHttpLogsOlderThan") {
        it("delegates timestamp to repository") {
            manager.purgeHttpLogsOlderThan(cutoffMs = 100L)

            verifySuspend { repository.deleteOlderThan(100L) }
        }
    }

    describe("markHttpCancelledIfInProgress") {
        it("delegates to repository") {
            manager.markHttpCancelledIfInProgress(5)

            verifySuspend { repository.markCancelledIfInProgress(5) }
        }
    }
})

private fun httpLog(url: String = "https://x") = HttpLog(
    url = url,
    method = "GET",
    timestamp = 0,
)
