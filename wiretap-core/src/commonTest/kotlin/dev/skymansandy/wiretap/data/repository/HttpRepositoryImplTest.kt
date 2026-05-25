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
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import dev.skymansandy.wiretap.data.db.room.dao.HttpLogsDao
import dev.skymansandy.wiretap.data.db.room.entity.HttpLogEntity
import dev.skymansandy.wiretap.data.db.room.entity.HttpLogListProjection
import dev.skymansandy.wiretap.domain.model.HttpLog
import dev.skymansandy.wiretap.domain.model.ResponseSource
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class HttpRepositoryImplTest {

    private val dao = mock<HttpLogsDao>(MockMode.autoUnit)
    private val repo = HttpRepositoryImpl(dao)

    @Test
    fun `flowAll maps projections from the dao to domain models`() = runTest {
        every { dao.getAllNetworkLogs() } returns flowOf(listOf(projection(id = 1, url = "https://x")))

        repo.flowAll().test {
            val logs = awaitItem()
            logs.size shouldBe 1
            logs[0].id shouldBe 1L
            logs[0].url shouldBe "https://x"
            awaitComplete()
        }
    }

    @Test
    fun `flowDistinctHosts passes the dao flow through unchanged`() = runTest {
        every { dao.getDistinctHosts() } returns flowOf(listOf("a.com", "b.com"))

        repo.flowDistinctHosts().test {
            awaitItem() shouldBe listOf("a.com", "b.com")
            awaitComplete()
        }
    }

    @Test
    fun `flowById maps entity to domain and propagates null`() = runTest {
        every { dao.flowById(7) } returns flowOf(entity(id = 7, url = "https://y"))
        every { dao.flowById(8) } returns flowOf<HttpLogEntity?>(null)

        repo.flowById(7).test {
            awaitItem()?.url shouldBe "https://y"
            awaitComplete()
        }
        repo.flowById(8).test {
            awaitItem() shouldBe null
            awaitComplete()
        }
    }

    @Test
    fun `getById delegates and maps entity to domain`() = runTest {
        everySuspend { dao.getById(3) } returns entity(id = 3, url = "https://z")

        val result = repo.getById(3)

        result?.id shouldBe 3L
        result?.url shouldBe "https://z"
    }

    @Test
    fun `getById returns null when dao returns null`() = runTest {
        everySuspend { dao.getById(99) } returns null

        repo.getById(99) shouldBe null
    }

    @Test
    fun `save delegates the entity to the dao`() = runTest {
        everySuspend { dao.insert(any()) } returns 1L

        repo.save(httpLog(url = "https://new"))

        verifySuspend { dao.insert(any()) }
    }

    @Test
    fun `saveAndGetId returns the id from the dao`() = runTest {
        everySuspend { dao.insert(any()) } returns 99L

        val id = repo.saveAndGetId(httpLog())

        id shouldBe 99L
    }

    @Test
    fun `update passes every field through to the dao update query`() = runTest {
        val log = HttpLog(
            id = 5,
            url = "https://x",
            method = "GET",
            responseCode = 200,
            responseHeaders = mapOf("X-A" to "1"),
            responseBody = "body",
            durationMs = 99,
            source = ResponseSource.Mock,
            matchedRuleId = 11L,
            protocol = "HTTP/2",
            remoteAddress = "1.2.3.4",
            tlsProtocol = "TLSv1.3",
            cipherSuite = "X",
            certificateCn = "cn",
            issuerCn = "iss",
            certificateExpiry = "exp",
            timestamp = 0,
        )

        repo.update(log)

        verifySuspend {
            dao.update(
                responseCode = 200,
                responseHeaders = "X-A: 1",
                responseBody = "body",
                durationMs = 99,
                source = "Mock",
                matchedRuleId = 11L,
                protocol = "HTTP/2",
                remoteAddress = "1.2.3.4",
                tlsProtocol = "TLSv1.3",
                cipherSuite = "X",
                certificateCn = "cn",
                issuerCn = "iss",
                certificateExpiry = "exp",
                id = 5,
            )
        }
    }

    @Test
    fun `deleteById delegates to the dao`() = runTest {
        repo.deleteById(42)
        verifySuspend { dao.deleteById(42) }
    }

    @Test
    fun `deleteOlderThan delegates timestamp to the dao`() = runTest {
        repo.deleteOlderThan(1_700_000_000_000L)
        verifySuspend { dao.deleteOlderThan(1_700_000_000_000L) }
    }

    @Test
    fun `clearAll delegates to the dao`() = runTest {
        repo.clearAll()
        verifySuspend { dao.deleteAll() }
    }

    @Test
    fun `markCancelledIfInProgress delegates to the dao`() = runTest {
        repo.markCancelledIfInProgress(7)
        verifySuspend { dao.markCancelledIfInProgress(7) }
    }

    // ---- helpers -----------------------------------------------------------

    private fun httpLog(url: String = "https://x") = HttpLog(
        url = url,
        method = "GET",
        timestamp = 0,
    )

    private fun entity(id: Long, url: String) = HttpLogEntity(
        id = id,
        url = url,
        method = "GET",
        requestHeaders = "",
        responseCode = 200,
        responseHeaders = "",
        durationMs = 0,
        source = "Network",
        timestamp = 0,
    )

    private fun projection(id: Long, url: String) = HttpLogListProjection(
        id = id,
        url = url,
        method = "GET",
        requestHeaders = "",
        responseCode = 200,
        responseHeaders = "",
        responseBodySize = 0,
        durationMs = 0,
        source = "Network",
        timestamp = 0,
        matchedRuleId = null,
        protocol = null,
        remoteAddress = null,
        tlsProtocol = null,
        cipherSuite = null,
        certificateCn = null,
        issuerCn = null,
        certificateExpiry = null,
    )
}
