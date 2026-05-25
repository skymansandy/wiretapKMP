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
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.test.Test

class RuleEntityMapperTest {

    // ---- URL matcher round-trips --------------------------------------------

    @Test
    fun `UrlMatcher Exact round-trips`() {
        val rule = baseRule(urlMatcher = UrlMatcher.Exact("https://api.example.com"))
        rule.toRoomEntity().toDomain().urlMatcher shouldBe UrlMatcher.Exact("https://api.example.com")
    }

    @Test
    fun `UrlMatcher Contains round-trips`() {
        val rule = baseRule(urlMatcher = UrlMatcher.Contains("/users"))
        rule.toRoomEntity().toDomain().urlMatcher shouldBe UrlMatcher.Contains("/users")
    }

    @Test
    fun `UrlMatcher Regex round-trips`() {
        val rule = baseRule(urlMatcher = UrlMatcher.Regex("""/api/v\d+/.*"""))
        rule.toRoomEntity().toDomain().urlMatcher shouldBe UrlMatcher.Regex("""/api/v\d+/.*""")
    }

    @Test
    fun `null UrlMatcher round-trips as null`() {
        val rule = baseRule(urlMatcher = null)
        rule.toRoomEntity().toDomain().urlMatcher shouldBe null
    }

    @Test
    fun `entity with only urlMatcherType but no pattern deserializes to null`() {
        val entity = baseEntity().copy(urlMatcherType = "Exact", urlPattern = null)
        entity.toDomain().urlMatcher shouldBe null
    }

    @Test
    fun `entity with only urlPattern but no type deserializes to null`() {
        val entity = baseEntity().copy(urlMatcherType = null, urlPattern = "/api")
        entity.toDomain().urlMatcher shouldBe null
    }

    // ---- Body matcher round-trips -------------------------------------------

    @Test
    fun `BodyMatcher Exact round-trips`() {
        val rule = baseRule(bodyMatcher = BodyMatcher.Exact("hello"))
        rule.toRoomEntity().toDomain().bodyMatcher shouldBe BodyMatcher.Exact("hello")
    }

    @Test
    fun `BodyMatcher Contains round-trips`() {
        val rule = baseRule(bodyMatcher = BodyMatcher.Contains("token"))
        rule.toRoomEntity().toDomain().bodyMatcher shouldBe BodyMatcher.Contains("token")
    }

    @Test
    fun `BodyMatcher Regex round-trips`() {
        val rule = baseRule(bodyMatcher = BodyMatcher.Regex("[0-9]+"))
        rule.toRoomEntity().toDomain().bodyMatcher shouldBe BodyMatcher.Regex("[0-9]+")
    }

    @Test
    fun `null BodyMatcher round-trips as null`() {
        baseRule(bodyMatcher = null).toRoomEntity().toDomain().bodyMatcher shouldBe null
    }

    // ---- Header matchers ----------------------------------------------------

    @Test
    fun `empty header matchers serialize to null and back to empty list`() {
        val rule = baseRule(headerMatchers = emptyList())
        val entity = rule.toRoomEntity()

        entity.headerMatchers shouldBe null
        entity.toDomain().headerMatchers shouldBe emptyList()
    }

    @Test
    fun `header matcher list round-trips`() {
        val matchers = listOf(
            HeaderMatcher.KeyExists("Authorization"),
            HeaderMatcher.ValueExact("Content-Type", "application/json"),
            HeaderMatcher.ValueContains("User-Agent", "Android"),
            HeaderMatcher.ValueRegex("X-Trace-Id", "[a-f0-9]{32}"),
        )

        val roundTripped = baseRule(headerMatchers = matchers).toRoomEntity().toDomain()

        roundTripped.headerMatchers shouldBe matchers
    }

    // ---- RuleAction round-trips ---------------------------------------------

    @Test
    fun `RuleAction Mock round-trips with explicit fields`() {
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

    @Test
    fun `RuleAction Mock with null body and null headers round-trips`() {
        val rule = baseRule(action = RuleAction.Mock(responseCode = 204))

        val action = rule.toRoomEntity().toDomain().action
        action.shouldBeInstanceOf<RuleAction.Mock>()
        action.responseCode shouldBe 204
        action.responseBody shouldBe null
        action.responseHeaders shouldBe null
    }

    @Test
    fun `Mock entity with null mockResponseCode defaults to 200 on deserialize`() {
        val entity = baseEntity().copy(action = "Mock", mockResponseCode = null)

        val action = entity.toDomain().action
        action.shouldBeInstanceOf<RuleAction.Mock>()
        action.responseCode shouldBe 200
    }

    @Test
    fun `RuleAction Throttle round-trips with min and max delay`() {
        val rule = baseRule(action = RuleAction.Throttle(delayMs = 100, delayMaxMs = 500))

        val action = rule.toRoomEntity().toDomain().action
        action.shouldBeInstanceOf<RuleAction.Throttle>()
        action.delayMs shouldBe 100L
        action.delayMaxMs shouldBe 500L
    }

    @Test
    fun `RuleAction Throttle round-trips with null max delay`() {
        val rule = baseRule(action = RuleAction.Throttle(delayMs = 250, delayMaxMs = null))

        val action = rule.toRoomEntity().toDomain().action
        action.shouldBeInstanceOf<RuleAction.Throttle>()
        action.delayMs shouldBe 250L
        action.delayMaxMs shouldBe null
    }

    @Test
    fun `Throttle entity with null delay defaults to zero on deserialize`() {
        val entity = baseEntity().copy(action = "Throttle", throttleDelayMs = null)

        val action = entity.toDomain().action
        action.shouldBeInstanceOf<RuleAction.Throttle>()
        action.delayMs shouldBe 0L
    }

    @Test
    fun `RuleAction MockAndThrottle round-trips all fields`() {
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

    @Test
    fun `MockAndThrottle entity with null delay defaults to 1000 on deserialize`() {
        val entity = baseEntity().copy(
            action = "MockAndThrottle",
            mockResponseCode = 500,
            throttleDelayMs = null,
        )

        val action = entity.toDomain().action
        action.shouldBeInstanceOf<RuleAction.MockAndThrottle>()
        action.delayMs shouldBe 1000L
    }

    @Test
    fun `entity columns for throttle are nulled for Mock action`() {
        val entity = baseRule(action = RuleAction.Mock(responseCode = 200)).toRoomEntity()

        entity.throttleDelayMs shouldBe null
        entity.throttleDelayMaxMs shouldBe null
    }

    @Test
    fun `entity columns for mock are nulled for Throttle action`() {
        val entity = baseRule(action = RuleAction.Throttle(delayMs = 10)).toRoomEntity()

        entity.mockResponseCode shouldBe null
        entity.mockResponseBody shouldBe null
        entity.mockResponseHeaders shouldBe null
    }

    // ---- enabled flag -------------------------------------------------------

    @Test
    fun `enabled true serializes to 1 and deserializes back`() {
        val entity = baseRule(enabled = true).toRoomEntity()
        entity.enabled shouldBe 1L
        entity.toDomain().enabled shouldBe true
    }

    @Test
    fun `enabled false serializes to 0 and deserializes back`() {
        val entity = baseRule(enabled = false).toRoomEntity()
        entity.enabled shouldBe 0L
        entity.toDomain().enabled shouldBe false
    }

    // ---- helpers ------------------------------------------------------------

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
}
