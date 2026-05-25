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
import dev.skymansandy.wiretap.data.db.room.dao.RulesDao
import dev.skymansandy.wiretap.data.db.room.entity.RuleEntity
import dev.skymansandy.wiretap.domain.model.RuleAction
import dev.skymansandy.wiretap.domain.model.WiretapRule
import dev.skymansandy.wiretap.domain.model.matchers.BodyMatcher
import dev.skymansandy.wiretap.domain.model.matchers.HeaderMatcher
import dev.skymansandy.wiretap.domain.model.matchers.UrlMatcher
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest

class RuleRepositoryImplTest : DescribeSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    val dao = mock<RulesDao>(MockMode.autoUnit)
    val repo = RuleRepositoryImpl(dao)

    describe("flowAll") {
        it("maps entities from the dao to domain rules") {
            runTest {
                every { dao.getAll() } returns flowOf(listOf(ruleEntity(id = 1)))

                repo.flowAll().test {
                    val rules = awaitItem()
                    rules.size shouldBe 1
                    rules[0].id shouldBe 1L
                    awaitComplete()
                }
            }
        }
    }

    describe("flowForQuery") {
        it("delegates the query string to the dao") {
            runTest {
                every { dao.search("token") } returns flowOf(listOf(ruleEntity(id = 5)))

                repo.flowForQuery("token").test {
                    awaitItem().first().id shouldBe 5L
                    awaitComplete()
                }
            }
        }
    }

    describe("flowById") {
        it("maps entity and propagates null") {
            runTest {
                every { dao.flowById(7) } returns flowOf(ruleEntity(id = 7))
                every { dao.flowById(8) } returns flowOf<RuleEntity?>(null)

                repo.flowById(7).test { awaitItem()?.id shouldBe 7L; awaitComplete() }
                repo.flowById(8).test { awaitItem() shouldBe null; awaitComplete() }
            }
        }
    }

    describe("getById") {
        it("delegates to dao and maps null pass-through") {
            runTest {
                everySuspend { dao.getById(3) } returns ruleEntity(id = 3)
                everySuspend { dao.getById(99) } returns null

                repo.getById(3)?.id shouldBe 3L
                repo.getById(99) shouldBe null
            }
        }
    }

    describe("getEnabledRules") {
        it("maps every entity returned by the dao") {
            runTest {
                everySuspend { dao.getEnabledRules() } returns listOf(ruleEntity(1), ruleEntity(2))

                val rules = repo.getEnabledRules()
                rules.size shouldBe 2
                rules.map { it.id } shouldBe listOf(1L, 2L)
            }
        }
    }

    describe("addRule") {
        it("delegates the serialized entity to the dao") {
            runTest {
                repo.addRule(rule(id = 0))
                verifySuspend { dao.insert(any()) }
            }
        }
    }

    describe("setEnabled") {
        it("true serializes to 1") {
            runTest {
                repo.setEnabled(id = 5, enabled = true)
                verifySuspend { dao.updateEnabled(enabled = 1L, id = 5) }
            }
        }

        it("false serializes to 0") {
            runTest {
                repo.setEnabled(id = 5, enabled = false)
                verifySuspend { dao.updateEnabled(enabled = 0L, id = 5) }
            }
        }
    }

    describe("deleteById") {
        it("delegates") {
            runTest {
                repo.deleteById(11)
                verifySuspend { dao.deleteById(11) }
            }
        }
    }

    describe("deleteAll") {
        it("delegates") {
            runTest {
                repo.deleteAll()
                verifySuspend { dao.deleteAll() }
            }
        }
    }

    describe("updateRule") {
        it("passes Mock action fields through and nulls throttle columns") {
            runTest {
                val mockRule = rule(
                    id = 7,
                    urlMatcher = UrlMatcher.Contains("/api"),
                    headerMatchers = listOf(HeaderMatcher.KeyExists("Authorization")),
                    bodyMatcher = BodyMatcher.Contains("token"),
                    action = RuleAction.Mock(
                        responseCode = 418,
                        responseBody = "tea",
                        responseHeaders = mapOf("X-A" to "1"),
                    ),
                )

                repo.updateRule(mockRule)

                verifySuspend {
                    dao.update(
                        method = "GET",
                        urlMatcherType = "Contains",
                        urlPattern = "/api",
                        headerMatchers = any(), // serializer-format-dependent
                        bodyMatcherType = "Contains",
                        bodyPattern = "token",
                        action = "Mock",
                        mockResponseCode = 418L,
                        mockResponseBody = "tea",
                        mockResponseHeaders = "X-A: 1",
                        throttleDelayMs = null,
                        throttleDelayMaxMs = null,
                        enabled = 1L,
                        id = 7L,
                    )
                }
            }
        }

        it("passes Throttle action fields and nulls mock columns") {
            runTest {
                val throttleRule = rule(
                    id = 8,
                    action = RuleAction.Throttle(delayMs = 100, delayMaxMs = 500),
                )

                repo.updateRule(throttleRule)

                verifySuspend {
                    dao.update(
                        method = any(),
                        urlMatcherType = any(),
                        urlPattern = any(),
                        headerMatchers = any(),
                        bodyMatcherType = any(),
                        bodyPattern = any(),
                        action = "Throttle",
                        mockResponseCode = null,
                        mockResponseBody = null,
                        mockResponseHeaders = null,
                        throttleDelayMs = 100L,
                        throttleDelayMaxMs = 500L,
                        enabled = 1L,
                        id = 8L,
                    )
                }
            }
        }

        it("passes MockAndThrottle fields") {
            runTest {
                val combo = rule(
                    id = 9,
                    action = RuleAction.MockAndThrottle(
                        responseCode = 503,
                        responseBody = "down",
                        responseHeaders = mapOf("Retry-After" to "30"),
                        delayMs = 250,
                        delayMaxMs = 1000,
                    ),
                )

                repo.updateRule(combo)

                verifySuspend {
                    dao.update(
                        method = any(),
                        urlMatcherType = any(),
                        urlPattern = any(),
                        headerMatchers = any(),
                        bodyMatcherType = any(),
                        bodyPattern = any(),
                        action = "MockAndThrottle",
                        mockResponseCode = 503L,
                        mockResponseBody = "down",
                        mockResponseHeaders = "Retry-After: 30",
                        throttleDelayMs = 250L,
                        throttleDelayMaxMs = 1000L,
                        enabled = 1L,
                        id = 9L,
                    )
                }
            }
        }

        it("serializes empty header list to null") {
            runTest {
                val ruleNoHeaders = rule(id = 10, headerMatchers = emptyList())

                repo.updateRule(ruleNoHeaders)

                verifySuspend {
                    dao.update(
                        method = any(),
                        urlMatcherType = any(),
                        urlPattern = any(),
                        headerMatchers = null,
                        bodyMatcherType = any(),
                        bodyPattern = any(),
                        action = any(),
                        mockResponseCode = any(),
                        mockResponseBody = any(),
                        mockResponseHeaders = any(),
                        throttleDelayMs = any(),
                        throttleDelayMaxMs = any(),
                        enabled = any(),
                        id = 10L,
                    )
                }
            }
        }
    }
})

private fun rule(
    id: Long,
    urlMatcher: UrlMatcher? = null,
    headerMatchers: List<HeaderMatcher> = emptyList(),
    bodyMatcher: BodyMatcher? = null,
    action: RuleAction = RuleAction.Mock(),
) = WiretapRule(
    id = id,
    method = "GET",
    urlMatcher = urlMatcher,
    headerMatchers = headerMatchers,
    bodyMatcher = bodyMatcher,
    action = action,
    enabled = true,
    createdAt = 0,
)

private fun ruleEntity(id: Long) = RuleEntity(
    id = id,
    method = "GET",
    action = "Mock",
    mockResponseCode = 200L,
    enabled = 1L,
    createdAt = 0,
)
