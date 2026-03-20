package dev.skymansandy.wiretap.data.repository

import app.cash.turbine.test
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import dev.skymansandy.wiretap.data.db.dao.RuleDao
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

    private lateinit var ruleDao: RuleDao
    private lateinit var repository: RuleRepositoryImpl

    @BeforeTest
    fun setup() {
        ruleDao = mock<RuleDao>()
        repository = RuleRepositoryImpl(ruleDao)
    }

    @Test
    fun `addRule delegates to dao`() = runTest {
        val rule = wiretapRule()
        everySuspend { ruleDao.insert(rule) } returns Unit

        repository.addRule(rule)

        verifySuspend { ruleDao.insert(rule) }
    }

    @Test
    fun `updateRule delegates to dao`() = runTest {
        val rule = wiretapRule(id = 5)
        everySuspend { ruleDao.update(rule) } returns Unit

        repository.updateRule(rule)

        verifySuspend { ruleDao.update(rule) }
    }

    @Test
    fun `getAll returns flow from dao`() = runTest {
        val rules = listOf(wiretapRule(id = 1), wiretapRule(id = 2))
        every { ruleDao.getAll() } returns flowOf(rules)

        repository.getAll().test {
            awaitItem() shouldHaveSize 2
            awaitComplete()
        }
    }

    @Test
    fun `search returns flow from dao`() = runTest {
        val rules = listOf(wiretapRule(id = 1))
        every { ruleDao.search("test") } returns flowOf(rules)

        repository.search("test").test {
            awaitItem() shouldHaveSize 1
            awaitComplete()
        }
    }

    @Test
    fun `search returns empty flow when no matches`() = runTest {
        every { ruleDao.search("nonexistent") } returns flowOf(emptyList())

        repository.search("nonexistent").test {
            awaitItem().shouldBeEmpty()
            awaitComplete()
        }
    }

    @Test
    fun `getById returns rule from dao`() = runTest {
        val rule = wiretapRule(id = 1)
        everySuspend { ruleDao.getById(1L) } returns rule

        repository.getById(1L) shouldBe rule
    }

    @Test
    fun `getById returns null when not found`() = runTest {
        everySuspend { ruleDao.getById(999L) } returns null

        repository.getById(999L).shouldBeNull()
    }

    @Test
    fun `getEnabledRules delegates to dao`() = runTest {
        val rules = listOf(wiretapRule(id = 1, enabled = true))
        everySuspend { ruleDao.getEnabledRules() } returns rules

        repository.getEnabledRules() shouldBe rules
    }

    @Test
    fun `setEnabled delegates to dao`() = runTest {
        everySuspend { ruleDao.updateEnabled(1L, false) } returns Unit

        repository.setEnabled(1L, false)

        verifySuspend { ruleDao.updateEnabled(1L, false) }
    }

    @Test
    fun `deleteById delegates to dao`() = runTest {
        everySuspend { ruleDao.deleteById(1L) } returns Unit

        repository.deleteById(1L)

        verifySuspend { ruleDao.deleteById(1L) }
    }

    @Test
    fun `deleteAll delegates to dao`() = runTest {
        everySuspend { ruleDao.deleteAll() } returns Unit

        repository.deleteAll()

        verifySuspend { ruleDao.deleteAll() }
    }
}
