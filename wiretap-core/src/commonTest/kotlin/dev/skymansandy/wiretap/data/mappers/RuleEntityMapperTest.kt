/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.data.mappers

import dev.skymansandy.wiretap.data.db.room.entity.RuleEntity
import dev.skymansandy.wiretap.domain.model.RuleAction
import dev.skymansandy.wiretap.domain.model.WiretapRule
import dev.skymansandy.wiretap.domain.model.matchers.BodyMatcher
import dev.skymansandy.wiretap.domain.model.matchers.HeaderMatcher
import dev.skymansandy.wiretap.domain.model.matchers.UrlMatcher
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class RuleEntityMapperTest : DescribeSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    describe("UrlMatcher") {
        it("Exact round-trips") {
            val rule = baseRule(urlMatcher = UrlMatcher.Exact("https://api.example.com"))
            rule.toRoomEntity().toDomain().urlMatcher shouldBe UrlMatcher.Exact("https://api.example.com")
        }

        it("Contains round-trips") {
            val rule = baseRule(urlMatcher = UrlMatcher.Contains("/users"))
            rule.toRoomEntity().toDomain().urlMatcher shouldBe UrlMatcher.Contains("/users")
        }

        it("Regex round-trips") {
            val rule = baseRule(urlMatcher = UrlMatcher.Regex("""/api/v\d+/.*"""))
            rule.toRoomEntity().toDomain().urlMatcher shouldBe UrlMatcher.Regex("""/api/v\d+/.*""")
        }

        it("null UrlMatcher round-trips as null") {
            val rule = baseRule(urlMatcher = null)
            rule.toRoomEntity().toDomain().urlMatcher shouldBe null
        }

        it("entity with only urlMatcherType but no pattern deserializes to null") {
            val entity = baseEntity().copy(urlMatcherType = "Exact", urlPattern = null)
            entity.toDomain().urlMatcher shouldBe null
        }

        it("entity with only urlPattern but no type deserializes to null") {
            val entity = baseEntity().copy(urlMatcherType = null, urlPattern = "/api")
            entity.toDomain().urlMatcher shouldBe null
        }
    }

    describe("BodyMatcher") {
        it("Exact round-trips") {
            val rule = baseRule(bodyMatcher = BodyMatcher.Exact("hello"))
            rule.toRoomEntity().toDomain().bodyMatcher shouldBe BodyMatcher.Exact("hello")
        }

        it("Contains round-trips") {
            val rule = baseRule(bodyMatcher = BodyMatcher.Contains("token"))
            rule.toRoomEntity().toDomain().bodyMatcher shouldBe BodyMatcher.Contains("token")
        }

        it("Regex round-trips") {
            val rule = baseRule(bodyMatcher = BodyMatcher.Regex("[0-9]+"))
            rule.toRoomEntity().toDomain().bodyMatcher shouldBe BodyMatcher.Regex("[0-9]+")
        }

        it("null BodyMatcher round-trips as null") {
            baseRule(bodyMatcher = null).toRoomEntity().toDomain().bodyMatcher shouldBe null
        }
    }

    describe("Header matchers") {
        it("empty header matchers serialize to null and back to empty list") {
            val rule = baseRule(headerMatchers = emptyList())
            val entity = rule.toRoomEntity()

            entity.headerMatchers shouldBe null
            entity.toDomain().headerMatchers shouldBe emptyList()
        }

        it("header matcher list round-trips") {
            val matchers = listOf(
                HeaderMatcher.KeyExists("Authorization"),
                HeaderMatcher.ValueExact("Content-Type", "application/json"),
                HeaderMatcher.ValueContains("User-Agent", "Android"),
                HeaderMatcher.ValueRegex("X-Trace-Id", "[a-f0-9]{32}"),
            )

            val roundTripped = baseRule(headerMatchers = matchers).toRoomEntity().toDomain()

            roundTripped.headerMatchers shouldBe matchers
        }
    }

    describe("RuleAction Mock") {
        it("round-trips with explicit fields") {
            val rule = baseRule(
                action = RuleAction.Mock(
                    responseCode = 418,
                    responseBody = "tea",
                    responseHeaders = mapOf("Content-Type" to "text/plain"),
                ),
            )

            val action = rule.toRoomEntity().toDomain().action
            action.shouldBeInstanceOf<RuleAction.Mock>()
            action.responseCode shouldBe 418
            action.responseBody shouldBe "tea"
            action.responseHeaders shouldBe mapOf("Content-Type" to "text/plain")
        }

        it("with null body and null headers round-trips") {
            val rule = baseRule(action = RuleAction.Mock(responseCode = 204))

            val action = rule.toRoomEntity().toDomain().action
            action.shouldBeInstanceOf<RuleAction.Mock>()
            action.responseCode shouldBe 204
            action.responseBody shouldBe null
            action.responseHeaders shouldBe null
        }

        it("entity with null mockResponseCode defaults to 200 on deserialize") {
            val entity = baseEntity().copy(action = "Mock", mockResponseCode = null)

            val action = entity.toDomain().action
            action.shouldBeInstanceOf<RuleAction.Mock>()
            action.responseCode shouldBe 200
        }
    }

    describe("RuleAction Throttle") {
        it("round-trips with min and max delay") {
            val rule = baseRule(action = RuleAction.Throttle(delayMs = 100, delayMaxMs = 500))

            val action = rule.toRoomEntity().toDomain().action
            action.shouldBeInstanceOf<RuleAction.Throttle>()
            action.delayMs shouldBe 100L
            action.delayMaxMs shouldBe 500L
        }

        it("round-trips with null max delay") {
            val rule = baseRule(action = RuleAction.Throttle(delayMs = 250, delayMaxMs = null))

            val action = rule.toRoomEntity().toDomain().action
            action.shouldBeInstanceOf<RuleAction.Throttle>()
            action.delayMs shouldBe 250L
            action.delayMaxMs shouldBe null
        }

        it("entity with null delay defaults to zero on deserialize") {
            val entity = baseEntity().copy(action = "Throttle", throttleDelayMs = null)

            val action = entity.toDomain().action
            action.shouldBeInstanceOf<RuleAction.Throttle>()
            action.delayMs shouldBe 0L
        }
    }

    describe("RuleAction MockAndThrottle") {
        it("round-trips all fields") {
            val rule = baseRule(
                action = RuleAction.MockAndThrottle(
                    responseCode = 503,
                    responseBody = "down",
                    responseHeaders = mapOf("Retry-After" to "30"),
                    delayMs = 100,
                    delayMaxMs = 1000,
                ),
            )

            val action = rule.toRoomEntity().toDomain().action
            action.shouldBeInstanceOf<RuleAction.MockAndThrottle>()
            action.responseCode shouldBe 503
            action.responseBody shouldBe "down"
            action.responseHeaders shouldBe mapOf("Retry-After" to "30")
            action.delayMs shouldBe 100L
            action.delayMaxMs shouldBe 1000L
        }

        it("entity with null delay defaults to 1000 on deserialize") {
            val entity = baseEntity().copy(
                action = "MockAndThrottle",
                mockResponseCode = 500,
                throttleDelayMs = null,
            )

            val action = entity.toDomain().action
            action.shouldBeInstanceOf<RuleAction.MockAndThrottle>()
            action.delayMs shouldBe 1000L
        }
    }

    describe("entity column nulling by action type") {
        it("throttle columns are nulled for Mock action") {
            val entity = baseRule(action = RuleAction.Mock(responseCode = 200)).toRoomEntity()

            entity.throttleDelayMs shouldBe null
            entity.throttleDelayMaxMs shouldBe null
        }

        it("mock columns are nulled for Throttle action") {
            val entity = baseRule(action = RuleAction.Throttle(delayMs = 10)).toRoomEntity()

            entity.mockResponseCode shouldBe null
            entity.mockResponseBody shouldBe null
            entity.mockResponseHeaders shouldBe null
        }
    }

    describe("enabled flag") {
        it("true serializes to 1 and deserializes back") {
            val entity = baseRule(enabled = true).toRoomEntity()
            entity.enabled shouldBe 1L
            entity.toDomain().enabled shouldBe true
        }

        it("false serializes to 0 and deserializes back") {
            val entity = baseRule(enabled = false).toRoomEntity()
            entity.enabled shouldBe 0L
            entity.toDomain().enabled shouldBe false
        }
    }
})

private fun baseRule(
    urlMatcher: UrlMatcher? = null,
    headerMatchers: List<HeaderMatcher> = emptyList(),
    bodyMatcher: BodyMatcher? = null,
    action: RuleAction = RuleAction.Mock(),
    enabled: Boolean = true,
) = WiretapRule(
    id = 1,
    method = "GET",
    urlMatcher = urlMatcher,
    headerMatchers = headerMatchers,
    bodyMatcher = bodyMatcher,
    action = action,
    enabled = enabled,
    createdAt = 0L,
)

private fun baseEntity() = RuleEntity(
    id = 1,
    method = "GET",
    action = "Mock",
    mockResponseCode = 200L,
    createdAt = 0L,
)
