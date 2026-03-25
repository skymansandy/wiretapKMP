package dev.skymansandy.wiretap.data.repository

import app.cash.turbine.test
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import dev.skymansandy.wiretap.data.db.room.dao.RulesDao
import dev.skymansandy.wiretap.data.db.room.entity.RuleEntity
import dev.skymansandy.wiretap.toRoomEntity
import dev.skymansandy.wiretap.wiretapRule
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class RuleRepositoryImplTest {

    private lateinit var rulesDao: RulesDao
    private lateinit var repository: RuleRepositoryImpl

    @BeforeTest
    fun setup() {
        rulesDao = mock<RulesDao>()
        repository = RuleRepositoryImpl(rulesDao)
    }

    @Test
    fun `addRule delegates to dao`() = runTest {
        everySuspend { rulesDao.insert(any<RuleEntity>()) } returns Unit

        repository.addRule(wiretapRule())

        verifySuspend { rulesDao.insert(any<RuleEntity>()) }
    }

    @Test
    fun `updateRule delegates to dao`() = runTest {
        everySuspend {
            rulesDao.update(
                method = any(),
                urlMatcherType = any(),
                urlPattern = any(),
                headerMatchers = any(),
                bodyMatcherType = any(),
                bodyPattern = any(),
                action = any(),
                mockResponseCode = any(),
                mockResponseBody = any(),
                mockResponseHeaders = any(),
                throttleDelayMs = any(),
                throttleDelayMaxMs = any(),
                enabled = any(),
                id = any(),
            )
        } returns Unit

        repository.updateRule(wiretapRule(id = 5))

        verifySuspend {
            rulesDao.update(
                method = any(),
                urlMatcherType = any(),
                urlPattern = any(),
                headerMatchers = any(),
                bodyMatcherType = any(),
                bodyPattern = any(),
                action = any(),
                mockResponseCode = any(),
                mockResponseBody = any(),
                mockResponseHeaders = any(),
                throttleDelayMs = any(),
                throttleDelayMaxMs = any(),
                enabled = any(),
                id = any(),
            )
        }
    }

    @Test
    fun `getAll returns flow from dao`() = runTest {
        val roomEntities = listOf(wiretapRule(id = 1).toRoomEntity(), wiretapRule(id = 2).toRoomEntity())
        every { rulesDao.getAll() } returns flowOf(roomEntities)

        repository.getAll().test {
            awaitItem() shouldHaveSize 2
            awaitComplete()
        }
    }

    @Test
    fun `search returns flow from dao`() = runTest {
        val roomEntities = listOf(wiretapRule(id = 1).toRoomEntity())
        every { rulesDao.search("test") } returns flowOf(roomEntities)

        repository.search("test").test {
            awaitItem() shouldHaveSize 1
            awaitComplete()
        }
    }

    @Test
    fun `search returns empty flow when no matches`() = runTest {
        every { rulesDao.search("nonexistent") } returns flowOf(emptyList())

        repository.search("nonexistent").test {
            awaitItem().shouldBeEmpty()
            awaitComplete()
        }
    }

    @Test
    fun `getById returns rule from dao`() = runTest {
        everySuspend { rulesDao.getById(1L) } returns wiretapRule(id = 1).toRoomEntity()

        val result = repository.getById(1L)
        result?.id shouldBe 1L
    }

    @Test
    fun `getById returns null when not found`() = runTest {
        everySuspend { rulesDao.getById(999L) } returns null

        repository.getById(999L).shouldBeNull()
    }

    @Test
    fun `getEnabledRules delegates to dao`() = runTest {
        everySuspend { rulesDao.getEnabledRules() } returns listOf(wiretapRule(id = 1).toRoomEntity())

        val result = repository.getEnabledRules()
        result shouldHaveSize 1
        result[0].id shouldBe 1L
    }

    @Test
    fun `setEnabled delegates to dao`() = runTest {
        everySuspend { rulesDao.updateEnabled(enabled = 0L, id = 1L) } returns Unit

        repository.setEnabled(1L, false)

        verifySuspend { rulesDao.updateEnabled(enabled = 0L, id = 1L) }
    }

    @Test
    fun `deleteById delegates to dao`() = runTest {
        everySuspend { rulesDao.deleteById(1L) } returns Unit

        repository.deleteById(1L)

        verifySuspend { rulesDao.deleteById(1L) }
    }

    @Test
    fun `deleteAll delegates to dao`() = runTest {
        everySuspend { rulesDao.deleteAll() } returns Unit

        repository.deleteAll()

        verifySuspend { rulesDao.deleteAll() }
    }
}
