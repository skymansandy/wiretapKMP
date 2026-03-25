package dev.skymansandy.wiretap.domain.usecase

import dev.skymansandy.wiretap.domain.model.BodyMatcher
import dev.skymansandy.wiretap.domain.model.HeaderMatcher
import dev.skymansandy.wiretap.domain.model.UrlMatcher
import dev.skymansandy.wiretap.wiretapRule
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import kotlin.test.Test

class RuleMatcherTest {

    // region matchesMethod

    @Test
    fun `matchesMethod - wildcard matches any method`() {
        RuleMatcher.matchesMethod("GET", "*").shouldBeTrue()
        RuleMatcher.matchesMethod("POST", "*").shouldBeTrue()
        RuleMatcher.matchesMethod("DELETE", "*").shouldBeTrue()
    }

    @Test
    fun `matchesMethod - exact match is case insensitive`() {
        RuleMatcher.matchesMethod("GET", "get").shouldBeTrue()
        RuleMatcher.matchesMethod("get", "GET").shouldBeTrue()
        RuleMatcher.matchesMethod("Post", "POST").shouldBeTrue()
    }

    @Test
    fun `matchesMethod - different methods do not match`() {
        RuleMatcher.matchesMethod("GET", "POST").shouldBeFalse()
        RuleMatcher.matchesMethod("PUT", "DELETE").shouldBeFalse()
    }

    // endregion

    // region matchesUrl

    @Test
    fun `matchesUrl - exact match is case insensitive`() {
        val matcher = UrlMatcher.Exact("https://api.example.com/users")
        RuleMatcher.matchesUrl(matcher, "https://api.example.com/users").shouldBeTrue()
        RuleMatcher.matchesUrl(matcher, "HTTPS://API.EXAMPLE.COM/USERS").shouldBeTrue()
        RuleMatcher.matchesUrl(matcher, "https://api.example.com/posts").shouldBeFalse()
    }

    @Test
    fun `matchesUrl - contains match is case insensitive`() {
        val matcher = UrlMatcher.Contains("example.com")
        RuleMatcher.matchesUrl(matcher, "https://example.com/users").shouldBeTrue()
        RuleMatcher.matchesUrl(matcher, "https://EXAMPLE.COM/users").shouldBeTrue()
        RuleMatcher.matchesUrl(matcher, "https://other.com/users").shouldBeFalse()
    }

    @Test
    fun `matchesUrl - regex match`() {
        val matcher = UrlMatcher.Regex("""/users/\d+""")
        RuleMatcher.matchesUrl(matcher, "https://api.example.com/users/123").shouldBeTrue()
        RuleMatcher.matchesUrl(matcher, "https://api.example.com/users/abc").shouldBeFalse()
    }

    @Test
    fun `matchesUrl - invalid regex returns false`() {
        val matcher = UrlMatcher.Regex("[invalid")
        RuleMatcher.matchesUrl(matcher, "anything").shouldBeFalse()
    }

    // endregion

    // region matchesHeader

    @Test
    fun `matchesHeader - key exists case insensitive`() {
        val matcher = HeaderMatcher.KeyExists("Content-Type")
        val headers = mapOf("content-type" to "application/json")

        RuleMatcher.matchesHeader(matcher, headers).shouldBeTrue()
        RuleMatcher.matchesHeader(matcher, emptyMap()).shouldBeFalse()
    }

    @Test
    fun `matchesHeader - value exact match case insensitive`() {
        val matcher = HeaderMatcher.ValueExact("Content-Type", "application/json")
        val headers = mapOf("Content-Type" to "APPLICATION/JSON")

        RuleMatcher.matchesHeader(matcher, headers).shouldBeTrue()
        RuleMatcher.matchesHeader(matcher, mapOf("Content-Type" to "text/plain")).shouldBeFalse()
    }

    @Test
    fun `matchesHeader - value contains match`() {
        val matcher = HeaderMatcher.ValueContains("Accept", "json")
        val headers = mapOf("Accept" to "application/json")

        RuleMatcher.matchesHeader(matcher, headers).shouldBeTrue()
        RuleMatcher.matchesHeader(matcher, mapOf("Accept" to "text/plain")).shouldBeFalse()
    }

    @Test
    fun `matchesHeader - value regex match`() {
        val matcher = HeaderMatcher.ValueRegex("Authorization", "Bearer .+")
        val headers = mapOf("Authorization" to "Bearer abc123")

        RuleMatcher.matchesHeader(matcher, headers).shouldBeTrue()
        RuleMatcher.matchesHeader(matcher, mapOf("Authorization" to "Basic abc")).shouldBeFalse()
    }

    @Test
    fun `matchesHeader - value exact returns false when header missing`() {
        val matcher = HeaderMatcher.ValueExact("X-Custom", "value")
        RuleMatcher.matchesHeader(matcher, emptyMap()).shouldBeFalse()
    }

    @Test
    fun `matchesHeader - value regex returns false for invalid regex`() {
        val matcher = HeaderMatcher.ValueRegex("Accept", "[invalid")
        RuleMatcher.matchesHeader(matcher, mapOf("Accept" to "anything")).shouldBeFalse()
    }

    // endregion

    // region matchesBody

    @Test
    fun `matchesBody - exact match is case insensitive`() {
        val matcher = BodyMatcher.Exact("hello world")
        RuleMatcher.matchesBody(matcher, "Hello World").shouldBeTrue()
        RuleMatcher.matchesBody(matcher, "hello").shouldBeFalse()
        RuleMatcher.matchesBody(matcher, null).shouldBeFalse()
    }

    @Test
    fun `matchesBody - contains match`() {
        val matcher = BodyMatcher.Contains("error")
        RuleMatcher.matchesBody(matcher, """{"message":"Error occurred"}""").shouldBeTrue()
        RuleMatcher.matchesBody(matcher, """{"message":"success"}""").shouldBeFalse()
        RuleMatcher.matchesBody(matcher, null).shouldBeFalse()
    }

    @Test
    fun `matchesBody - regex match`() {
        val matcher = BodyMatcher.Regex(""""id":\s*\d+""")
        RuleMatcher.matchesBody(matcher, """{"id": 42}""").shouldBeTrue()
        RuleMatcher.matchesBody(matcher, """{"id": "abc"}""").shouldBeFalse()
        RuleMatcher.matchesBody(matcher, null).shouldBeFalse()
    }

    @Test
    fun `matchesBody - invalid regex returns false`() {
        val matcher = BodyMatcher.Regex("[invalid")
        RuleMatcher.matchesBody(matcher, "anything").shouldBeFalse()
    }

    // endregion

    // region matchesAllCriteria

    @Test
    fun `matchesAllCriteria - no criteria returns false`() {
        val rule = wiretapRule(urlMatcher = null, headerMatchers = emptyList(), bodyMatcher = null)
        RuleMatcher.matchesAllCriteria(rule, "https://example.com", emptyMap(), null).shouldBeFalse()
    }

    @Test
    fun `matchesAllCriteria - url only`() {
        val rule = wiretapRule(urlMatcher = UrlMatcher.Contains("example"))
        RuleMatcher.matchesAllCriteria(rule, "https://example.com", emptyMap(), null).shouldBeTrue()
        RuleMatcher.matchesAllCriteria(rule, "https://other.com", emptyMap(), null).shouldBeFalse()
    }

    @Test
    fun `matchesAllCriteria - all criteria must match AND logic`() {
        val rule = wiretapRule(
            urlMatcher = UrlMatcher.Contains("example"),
            headerMatchers = listOf(HeaderMatcher.KeyExists("Authorization")),
            bodyMatcher = BodyMatcher.Contains("user"),
        )

        // All match
        RuleMatcher.matchesAllCriteria(
            rule,
            "https://example.com",
            mapOf("Authorization" to "Bearer token"),
            """{"user":"john"}""",
        ).shouldBeTrue()

        // URL doesn't match
        RuleMatcher.matchesAllCriteria(
            rule,
            "https://other.com",
            mapOf("Authorization" to "Bearer token"),
            """{"user":"john"}""",
        ).shouldBeFalse()

        // Header doesn't match
        RuleMatcher.matchesAllCriteria(
            rule,
            "https://example.com",
            emptyMap(),
            """{"user":"john"}""",
        ).shouldBeFalse()

        // Body doesn't match
        RuleMatcher.matchesAllCriteria(
            rule,
            "https://example.com",
            mapOf("Authorization" to "Bearer token"),
            """{"data":"value"}""",
        ).shouldBeFalse()
    }

    // endregion

    // region methodsOverlap

    @Test
    fun `methodsOverlap - wildcard always overlaps`() {
        RuleMatcher.methodsOverlap("*", "GET").shouldBeTrue()
        RuleMatcher.methodsOverlap("POST", "*").shouldBeTrue()
        RuleMatcher.methodsOverlap("*", "*").shouldBeTrue()
    }

    @Test
    fun `methodsOverlap - same method overlaps case insensitive`() {
        RuleMatcher.methodsOverlap("GET", "get").shouldBeTrue()
    }

    @Test
    fun `methodsOverlap - different methods do not overlap`() {
        RuleMatcher.methodsOverlap("GET", "POST").shouldBeFalse()
    }

    // endregion

    // region urlMatchersOverlap

    @Test
    fun `urlMatchersOverlap - null matchers always overlap`() {
        RuleMatcher.urlMatchersOverlap(null, null).shouldBeTrue()
        RuleMatcher.urlMatchersOverlap(null, UrlMatcher.Exact("x")).shouldBeTrue()
        RuleMatcher.urlMatchersOverlap(UrlMatcher.Exact("x"), null).shouldBeTrue()
    }

    @Test
    fun `urlMatchersOverlap - exact vs exact same pattern overlaps`() {
        RuleMatcher.urlMatchersOverlap(
            UrlMatcher.Exact("https://api.com/users"),
            UrlMatcher.Exact("https://api.com/users"),
        ).shouldBeTrue()
    }

    @Test
    fun `urlMatchersOverlap - exact vs exact different patterns do not overlap`() {
        RuleMatcher.urlMatchersOverlap(
            UrlMatcher.Exact("https://api.com/users"),
            UrlMatcher.Exact("https://api.com/posts"),
        ).shouldBeFalse()
    }

    @Test
    fun `urlMatchersOverlap - contains vs contains with substring overlap`() {
        RuleMatcher.urlMatchersOverlap(
            UrlMatcher.Contains("api.com"),
            UrlMatcher.Contains("api"),
        ).shouldBeTrue()
    }

    @Test
    fun `urlMatchersOverlap - exact vs contains overlap when exact contains the pattern`() {
        RuleMatcher.urlMatchersOverlap(
            UrlMatcher.Exact("https://api.com/users"),
            UrlMatcher.Contains("api.com"),
        ).shouldBeTrue()
    }

    @Test
    fun `urlMatchersOverlap - regex always conservatively overlaps`() {
        RuleMatcher.urlMatchersOverlap(
            UrlMatcher.Regex(".*"),
            UrlMatcher.Exact("anything"),
        ).shouldBeTrue()
    }

    // endregion

    // region bodyMatchersOverlap

    @Test
    fun `bodyMatchersOverlap - null matchers always overlap`() {
        RuleMatcher.bodyMatchersOverlap(null, null).shouldBeTrue()
        RuleMatcher.bodyMatchersOverlap(null, BodyMatcher.Exact("x")).shouldBeTrue()
    }

    @Test
    fun `bodyMatchersOverlap - exact vs exact same overlaps`() {
        RuleMatcher.bodyMatchersOverlap(
            BodyMatcher.Exact("hello"),
            BodyMatcher.Exact("hello"),
        ).shouldBeTrue()
    }

    @Test
    fun `bodyMatchersOverlap - exact vs exact different do not overlap`() {
        RuleMatcher.bodyMatchersOverlap(
            BodyMatcher.Exact("hello"),
            BodyMatcher.Exact("world"),
        ).shouldBeFalse()
    }

    @Test
    fun `bodyMatchersOverlap - contains vs contains with substring overlap`() {
        RuleMatcher.bodyMatchersOverlap(
            BodyMatcher.Contains("hello world"),
            BodyMatcher.Contains("hello"),
        ).shouldBeTrue()
    }

    @Test
    fun `bodyMatchersOverlap - regex always conservatively overlaps`() {
        RuleMatcher.bodyMatchersOverlap(
            BodyMatcher.Regex(".*"),
            BodyMatcher.Contains("anything"),
        ).shouldBeTrue()
    }

    // endregion

    // region headerMatchersOverlap

    @Test
    fun `headerMatchersOverlap - empty lists always overlap`() {
        RuleMatcher.headerMatchersOverlap(emptyList(), emptyList()).shouldBeTrue()
        RuleMatcher.headerMatchersOverlap(
            emptyList(),
            listOf(HeaderMatcher.KeyExists("X-Key")),
        ).shouldBeTrue()
        RuleMatcher.headerMatchersOverlap(
            listOf(HeaderMatcher.KeyExists("X-Key")),
            emptyList(),
        ).shouldBeTrue()
    }

    @Test
    fun `headerMatchersOverlap - same key exact values match`() {
        RuleMatcher.headerMatchersOverlap(
            listOf(HeaderMatcher.ValueExact("Content-Type", "application/json")),
            listOf(HeaderMatcher.ValueExact("Content-Type", "application/json")),
        ).shouldBeTrue()
    }

    @Test
    fun `headerMatchersOverlap - same key different exact values do not overlap`() {
        RuleMatcher.headerMatchersOverlap(
            listOf(HeaderMatcher.ValueExact("Content-Type", "application/json")),
            listOf(HeaderMatcher.ValueExact("Content-Type", "text/plain")),
        ).shouldBeFalse()
    }

    @Test
    fun `headerMatchersOverlap - different keys always overlap`() {
        RuleMatcher.headerMatchersOverlap(
            listOf(HeaderMatcher.ValueExact("Content-Type", "application/json")),
            listOf(HeaderMatcher.ValueExact("Accept", "text/plain")),
        ).shouldBeTrue()
    }

    @Test
    fun `headerMatchersOverlap - keyExists overlaps with value matcher on same key`() {
        RuleMatcher.headerMatchersOverlap(
            listOf(HeaderMatcher.KeyExists("Authorization")),
            listOf(HeaderMatcher.ValueExact("Authorization", "Bearer token")),
        ).shouldBeTrue()
    }

    @Test
    fun `headerMatchersOverlap - contains vs exact overlap when exact contains pattern`() {
        RuleMatcher.headerMatchersOverlap(
            listOf(HeaderMatcher.ValueContains("Accept", "json")),
            listOf(HeaderMatcher.ValueExact("Accept", "application/json")),
        ).shouldBeTrue()
    }

    @Test
    fun `headerMatchersOverlap - contains vs exact no overlap when exact does not contain pattern`() {
        RuleMatcher.headerMatchersOverlap(
            listOf(HeaderMatcher.ValueContains("Accept", "xml")),
            listOf(HeaderMatcher.ValueExact("Accept", "application/json")),
        ).shouldBeFalse()
    }

    @Test
    fun `headerMatchersOverlap - case insensitive key matching`() {
        RuleMatcher.headerMatchersOverlap(
            listOf(HeaderMatcher.ValueExact("content-type", "application/json")),
            listOf(HeaderMatcher.ValueExact("Content-Type", "text/plain")),
        ).shouldBeFalse()
    }

    // endregion

    // region rulesOverlap

    @Test
    fun `rulesOverlap - same rules overlap`() {
        val rule = wiretapRule(
            method = "GET",
            urlMatcher = UrlMatcher.Contains("api.com"),
        )
        RuleMatcher.rulesOverlap(rule, rule).shouldBeTrue()
    }

    @Test
    fun `rulesOverlap - different methods do not overlap`() {
        val a = wiretapRule(method = "GET", urlMatcher = UrlMatcher.Contains("api.com"))
        val b = wiretapRule(method = "POST", urlMatcher = UrlMatcher.Contains("api.com"))
        RuleMatcher.rulesOverlap(a, b).shouldBeFalse()
    }

    @Test
    fun `rulesOverlap - different urls do not overlap`() {
        val a = wiretapRule(method = "GET", urlMatcher = UrlMatcher.Exact("https://a.com"))
        val b = wiretapRule(method = "GET", urlMatcher = UrlMatcher.Exact("https://b.com"))
        RuleMatcher.rulesOverlap(a, b).shouldBeFalse()
    }

    @Test
    fun `rulesOverlap - different headers do not overlap`() {
        val a = wiretapRule(
            method = "GET",
            urlMatcher = UrlMatcher.Contains("api.com"),
            headerMatchers = listOf(HeaderMatcher.ValueExact("Content-Type", "application/json")),
        )
        val b = wiretapRule(
            method = "GET",
            urlMatcher = UrlMatcher.Contains("api.com"),
            headerMatchers = listOf(HeaderMatcher.ValueExact("Content-Type", "text/plain")),
        )
        RuleMatcher.rulesOverlap(a, b).shouldBeFalse()
    }

    @Test
    fun `rulesOverlap - same headers overlap`() {
        val a = wiretapRule(
            method = "GET",
            urlMatcher = UrlMatcher.Contains("api.com"),
            headerMatchers = listOf(HeaderMatcher.ValueExact("Content-Type", "application/json")),
        )
        val b = wiretapRule(
            method = "GET",
            urlMatcher = UrlMatcher.Contains("api.com"),
            headerMatchers = listOf(HeaderMatcher.ValueExact("Content-Type", "application/json")),
        )
        RuleMatcher.rulesOverlap(a, b).shouldBeTrue()
    }

    // endregion
}
