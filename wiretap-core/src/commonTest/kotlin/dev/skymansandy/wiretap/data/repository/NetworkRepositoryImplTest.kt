package dev.skymansandy.wiretap.data.repository

import app.cash.turbine.test
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import dev.skymansandy.wiretap.data.db.dao.NetworkDao
import dev.skymansandy.wiretap.httpLogEntry
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class NetworkRepositoryImplTest {

    private lateinit var networkDao: NetworkDao
    private lateinit var repository: NetworkRepositoryImpl

    @BeforeTest
    fun setup() {
        networkDao = mock<NetworkDao>()
        repository = NetworkRepositoryImpl(networkDao)
    }

    @Test
    fun `save delegates to dao`() = runTest {
        val entry = httpLogEntry()
        everySuspend { networkDao.insert(entry) } returns Unit

        repository.save(entry)

        verifySuspend { networkDao.insert(entry) }
    }

    @Test
    fun `saveAndGetId returns id from dao`() = runTest {
        val entry = httpLogEntry()
        everySuspend { networkDao.insertAndGetId(entry) } returns 42L

        val id = repository.saveAndGetId(entry)

        id shouldBe 42L
    }

    @Test
    fun `update delegates to dao`() = runTest {
        val entry = httpLogEntry(id = 5)
        everySuspend { networkDao.update(entry) } returns Unit

        repository.update(entry)

        verifySuspend { networkDao.update(entry) }
    }

    @Test
    fun `getAll returns flow from dao`() = runTest {
        val entries = listOf(httpLogEntry(id = 1), httpLogEntry(id = 2))
        every { networkDao.getAll() } returns flowOf(entries)

        repository.getAll().test {
            awaitItem() shouldBe entries
            awaitComplete()
        }
    }

    @Test
    fun `getById returns entry from dao`() = runTest {
        val entry = httpLogEntry(id = 1)
        everySuspend { networkDao.getById(1L) } returns entry

        repository.getById(1L) shouldBe entry
    }

    @Test
    fun `getById returns null when not found`() = runTest {
        everySuspend { networkDao.getById(999L) } returns null

        repository.getById(999L).shouldBeNull()
    }

    @Test
    fun `deleteById delegates to dao`() = runTest {
        everySuspend { networkDao.deleteById(1L) } returns Unit

        repository.deleteById(1L)

        verifySuspend { networkDao.deleteById(1L) }
    }

    @Test
    fun `deleteOlderThan delegates to dao`() = runTest {
        val timestamp = 5000L
        everySuspend { networkDao.deleteOlderThan(timestamp) } returns Unit

        repository.deleteOlderThan(timestamp)

        verifySuspend { networkDao.deleteOlderThan(timestamp) }
    }

    @Test
    fun `clearAll delegates to dao`() = runTest {
        everySuspend { networkDao.deleteAll() } returns Unit

        repository.clearAll()

        verifySuspend { networkDao.deleteAll() }
    }
}
