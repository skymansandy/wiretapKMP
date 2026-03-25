package dev.skymansandy.wiretap.data.repository

import app.cash.turbine.test
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import dev.skymansandy.wiretap.data.db.room.dao.HttpLogsDao
import dev.skymansandy.wiretap.data.db.room.entity.HttpLogEntity
import dev.skymansandy.wiretap.httpLogEntry
import dev.skymansandy.wiretap.toHttpLogListProjection
import dev.skymansandy.wiretap.toRoomEntity
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class HttpRepositoryImplTest {

    private lateinit var httpLogsDao: HttpLogsDao
    private lateinit var repository: HttpRepositoryImpl

    @BeforeTest
    fun setup() {
        httpLogsDao = mock<HttpLogsDao>()
        repository = HttpRepositoryImpl(httpLogsDao)
    }

    @Test
    fun `save delegates to dao`() = runTest {
        everySuspend { httpLogsDao.insert(any<HttpLogEntity>()) } returns 1L

        repository.save(httpLogEntry())

        verifySuspend { httpLogsDao.insert(any<HttpLogEntity>()) }
    }

    @Test
    fun `saveAndGetId returns id from dao`() = runTest {
        everySuspend { httpLogsDao.insert(any<HttpLogEntity>()) } returns 42L

        val id = repository.saveAndGetId(httpLogEntry())

        id shouldBe 42L
    }

    @Test
    fun `update delegates to dao`() = runTest {
        everySuspend {
            httpLogsDao.update(
                responseCode = any(),
                responseHeaders = any(),
                responseBody = any(),
                durationMs = any(),
                source = any(),
                matchedRuleId = any(),
                protocol = any(),
                remoteAddress = any(),
                tlsProtocol = any(),
                cipherSuite = any(),
                certificateCn = any(),
                issuerCn = any(),
                certificateExpiry = any(),
                id = any(),
            )
        } returns Unit

        repository.update(httpLogEntry(id = 5))

        verifySuspend {
            httpLogsDao.update(
                responseCode = any(),
                responseHeaders = any(),
                responseBody = any(),
                durationMs = any(),
                source = any(),
                matchedRuleId = any(),
                protocol = any(),
                remoteAddress = any(),
                tlsProtocol = any(),
                cipherSuite = any(),
                certificateCn = any(),
                issuerCn = any(),
                certificateExpiry = any(),
                id = any(),
            )
        }
    }

    @Test
    fun `getAll returns flow from dao`() = runTest {
        val projections = listOf(
            httpLogEntry(id = 1).toHttpLogListProjection(),
            httpLogEntry(id = 2).toHttpLogListProjection(),
        )
        every { httpLogsDao.getAllNetworkLogs() } returns flowOf(projections)

        repository.getAll().test {
            val items = awaitItem()
            items[0].id shouldBe 1L
            items[1].id shouldBe 2L
            awaitComplete()
        }
    }

    @Test
    fun `getById returns entry from dao`() = runTest {
        val roomEntity = httpLogEntry(id = 1).toRoomEntity()
        everySuspend { httpLogsDao.getById(1L) } returns roomEntity

        val result = repository.getById(1L)
        result?.id shouldBe 1L
    }

    @Test
    fun `getById returns null when not found`() = runTest {
        everySuspend { httpLogsDao.getById(999L) } returns null

        repository.getById(999L).shouldBeNull()
    }

    @Test
    fun `deleteById delegates to dao`() = runTest {
        everySuspend { httpLogsDao.deleteById(1L) } returns Unit

        repository.deleteById(1L)

        verifySuspend { httpLogsDao.deleteById(1L) }
    }

    @Test
    fun `deleteOlderThan delegates to dao`() = runTest {
        val timestamp = 5000L
        everySuspend { httpLogsDao.deleteOlderThan(timestamp) } returns Unit

        repository.deleteOlderThan(timestamp)

        verifySuspend { httpLogsDao.deleteOlderThan(timestamp) }
    }

    @Test
    fun `clearAll delegates to dao`() = runTest {
        everySuspend { httpLogsDao.deleteAll() } returns Unit

        repository.clearAll()

        verifySuspend { httpLogsDao.deleteAll() }
    }
}
