/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.domain.usecase

import dev.skymansandy.wiretap.domain.model.RuleAction
import dev.skymansandy.wiretap.domain.model.WiretapRule
import dev.skymansandy.wiretap.domain.model.matchers.BodyMatcher
import dev.skymansandy.wiretap.domain.model.matchers.HeaderMatcher
import dev.skymansandy.wiretap.domain.model.matchers.UrlMatcher
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class RuleMatcherTest {

    // ---- matchesMethod ------------------------------------------------------

    @Test
    fun `matchesMethod wildcard matches any request method`() {
        RuleMatcher.matchesMethod(requestMethod = "GET", ruleMethod = "*") shouldBe true
        RuleMatcher.matchesMethod(requestMethod = "POST", ruleMethod = "*") shouldBe true
    }

    @Test
    fun `matchesMethod is case-insensitive for exact match`() {
        RuleMatcher.matchesMethod(requestMethod = "get", ruleMethod = "GET") shouldBe true
        RuleMatcher.matchesMethod(requestMethod = "GET", ruleMethod = "get") shouldBe true
    }

    @Test
    fun `matchesMethod returns false when methods differ`() {
        RuleMatcher.matchesMethod(requestMethod = "GET", ruleMethod = "POST") shouldBe false
    }

    // ---- matchesUrl ---------------------------------------------------------

    @Test
    fun `matchesUrl Exact is case-insensitive`() {
        RuleMatcher.matchesUrl(UrlMatcher.Exact("https://Example.COM"), "HTTPS://example.com") shouldBe true
    }

    @Test
    fun `matchesUrl Exact rejects substring`() {
        RuleMatcher.matchesUrl(UrlMatcher.Exact("https://example.com"), "https://example.com/path") shouldBe false
    }

    @Test
    fun `matchesUrl Contains is case-insensitive`() {
        RuleMatcher.matchesUrl(UrlMatcher.Contains("/USERS"), "https://example.com/users/1") shouldBe true
    }

    @Test
    fun `matchesUrl Contains returns false when substring absent`() {
        RuleMatcher.matchesUrl(UrlMatcher.Contains("/admin"), "https://example.com/users") shouldBe false
    }

    @Test
    fun `matchesUrl Regex matches`() {
        RuleMatcher.matchesUrl(UrlMatcher.Regex("""/api/v\d+/.*"""), "https://example.com/api/v2/users") shouldBe true
    }

    @Test
    fun `matchesUrl Regex with invalid pattern returns false`() {
        RuleMatcher.matchesUrl(UrlMatcher.Regex("["), "https://example.com") shouldBe false
    }

    // ---- matchesHeader ------------------------------------------------------

    @Test
    fun `matchesHeader KeyExists is case-insensitive on the key`() {
        val headers = mapOf("Authorization" to "Bearer abc")
        RuleMatcher.matchesHeader(HeaderMatcher.KeyExists("authorization"), headers) shouldBe true
    }

    @Test
    fun `matchesHeader KeyExists returns false when key missing`() {
        RuleMatcher.matchesHeader(HeaderMatcher.KeyExists("X-Missing"), mapOf("Accept" to "*")) shouldBe false
    }

    @Test
    fun `matchesHeader ValueExact is case-insensitive on the value`() {
        val headers = mapOf("Content-Type" to "application/JSON")
        RuleMatcher.matchesHeader(HeaderMatcher.ValueExact("content-type", "APPLICATION/json"), headers) shouldBe true
    }

    @Test
    fun `matchesHeader ValueExact returns false when key missing`() {
        RuleMatcher.matchesHeader(HeaderMatcher.ValueExact("Content-Type", "x"), emptyMap()) shouldBe false
    }

    @Test
    fun `matchesHeader ValueContains is case-insensitive`() {
        val headers = mapOf("User-Agent" to "Mozilla / Android Native")
        RuleMatcher.matchesHeader(HeaderMatcher.ValueContains("user-agent", "android"), headers) shouldBe true
    }

    @Test
    fun `matchesHeader ValueRegex matches`() {
        val headers = mapOf("X-Trace-Id" to "abc123def456")
        RuleMatcher.matchesHeader(HeaderMatcher.ValueRegex("X-Trace-Id", "[a-f0-9]+"), headers) shouldBe true
    }

    @Test
    fun `matchesHeader ValueRegex with invalid pattern returns false`() {
        RuleMatcher.matchesHeader(HeaderMatcher.ValueRegex("X", "["), mapOf("X" to "value")) shouldBe false
    }

    @Test
    fun `matchesHeader ValueRegex returns false when key missing`() {
        RuleMatcher.matchesHeader(HeaderMatcher.ValueRegex("X", ".*"), emptyMap()) shouldBe false
    }

    // ---- matchesBody --------------------------------------------------------

    @Test
    fun `matchesBody Exact is case-insensitive`() {
        RuleMatcher.matchesBody(BodyMatcher.Exact("HELLO"), "hello") shouldBe true
    }

    @Test
    fun `matchesBody Exact returns false for null body`() {
        RuleMatcher.matchesBody(BodyMatcher.Exact("x"), null) shouldBe false
    }

    @Test
    fun `matchesBody Contains is case-insensitive`() {
        RuleMatcher.matchesBody(BodyMatcher.Contains("TOKEN"), "auth token here") shouldBe true
    }

    @Test
    fun `matchesBody Regex matches`() {
        RuleMatcher.matchesBody(BodyMatcher.Regex("[0-9]+"), "id=42") shouldBe true
    }

    @Test
    fun `matchesBody Regex with invalid pattern returns false`() {
        RuleMatcher.matchesBody(BodyMatcher.Regex("["), "anything") shouldBe false
    }

    @Test
    fun `matchesBody Regex on null body returns false`() {
        RuleMatcher.matchesBody(BodyMatcher.Regex(".*"), null) shouldBe false
    }

    // ---- matchesAllCriteria -------------------------------------------------

    @Test
    fun `matchesAllCriteria returns false when rule has no criteria at all`() {
        val rule = ruleWith()

        RuleMatcher.matchesAllCriteria(rule, "https://example.com", emptyMap(), null) shouldBe false
    }

    @Test
    fun `matchesAllCriteria returns true when only url matches and matches`() {
        val rule = ruleWith(urlMatcher = UrlMatcher.Contains("/api"))

        RuleMatcher.matchesAllCriteria(rule, "https://example.com/api", emptyMap(), null) shouldBe true
    }

    @Test
    fun `matchesAllCriteria short-circuits when url does not match`() {
        val rule = ruleWith(urlMatcher = UrlMatcher.Exact("https://other"))

        RuleMatcher.matchesAllCriteria(rule, "https://example.com", emptyMap(), null) shouldBe false
    }

    @Test
    fun `matchesAllCriteria requires every header matcher to match`() {
        val rule = ruleWith(
            urlMatcher = UrlMatcher.Contains("/api"),
            headerMatchers = listOf(
                HeaderMatcher.KeyExists("Authorization"),
                HeaderMatcher.ValueExact("Content-Type", "application/json"),
            ),
        )

        RuleMatcher.matchesAllCriteria(
            rule,
            "https://example.com/api",
            mapOf("Authorization" to "Bearer abc", "Content-Type" to "application/json"),
            null,
        ) shouldBe true

        RuleMatcher.matchesAllCriteria(
            rule,
            "https://example.com/api",
            mapOf("Authorization" to "Bearer abc"),
            null,
        ) shouldBe false
    }

    @Test
    fun `matchesAllCriteria evaluates body matcher when present`() {
        val rule = ruleWith(
            urlMatcher = UrlMatcher.Contains("/api"),
            bodyMatcher = BodyMatcher.Contains("token"),
        )

        RuleMatcher.matchesAllCriteria(rule, "https://example.com/api", emptyMap(), "auth token") shouldBe true
        RuleMatcher.matchesAllCriteria(rule, "https://example.com/api", emptyMap(), "no match") shouldBe false
    }

    // ---- methodsOverlap -----------------------------------------------------

    @Test
    fun `methodsOverlap is true when either side is wildcard`() {
        RuleMatcher.methodsOverlap("*", "GET") shouldBe true
        RuleMatcher.methodsOverlap("POST", "*") shouldBe true
    }

    @Test
    fun `methodsOverlap is true for case-insensitive equality`() {
        RuleMatcher.methodsOverlap("GET", "get") shouldBe true
    }

    @Test
    fun `methodsOverlap is false for distinct methods`() {
        RuleMatcher.methodsOverlap("GET", "POST") shouldBe false
    }

    // ---- urlMatchersOverlap (patternsOverlap matrix) ------------------------

    @Test
    fun `urlMatchersOverlap is true when either side is null`() {
        RuleMatcher.urlMatchersOverlap(null, UrlMatcher.Exact("x")) shouldBe true
        RuleMatcher.urlMatchersOverlap(UrlMatcher.Exact("x"), null) shouldBe true
        RuleMatcher.urlMatchersOverlap(null, null) shouldBe true
    }

    @Test
    fun `urlMatchersOverlap Exact and Exact match only on identical pattern`() {
        RuleMatcher.urlMatchersOverlap(UrlMatcher.Exact("a"), UrlMatcher.Exact("a")) shouldBe true
        RuleMatcher.urlMatchersOverlap(UrlMatcher.Exact("a"), UrlMatcher.Exact("b")) shouldBe false
    }

    @Test
    fun `urlMatchersOverlap Contains and Contains always overlap`() {
        RuleMatcher.urlMatchersOverlap(UrlMatcher.Contains("a"), UrlMatcher.Contains("zz")) shouldBe true
    }

    @Test
    fun `urlMatchersOverlap Exact contains Contains pattern means overlap`() {
        RuleMatcher.urlMatchersOverlap(UrlMatcher.Exact("/api/users"), UrlMatcher.Contains("users")) shouldBe true
        RuleMatcher.urlMatchersOverlap(UrlMatcher.Exact("/api/users"), UrlMatcher.Contains("admin")) shouldBe false
    }

    @Test
    fun `urlMatchersOverlap Contains and Exact swap arguments`() {
        RuleMatcher.urlMatchersOverlap(UrlMatcher.Contains("users"), UrlMatcher.Exact("/api/users")) shouldBe true
        RuleMatcher.urlMatchersOverlap(UrlMatcher.Contains("admin"), UrlMatcher.Exact("/api/users")) shouldBe false
    }

    @Test
    fun `urlMatchersOverlap any Regex always overlaps`() {
        RuleMatcher.urlMatchersOverlap(UrlMatcher.Regex(".*"), UrlMatcher.Exact("x")) shouldBe true
        RuleMatcher.urlMatchersOverlap(UrlMatcher.Exact("x"), UrlMatcher.Regex(".*")) shouldBe true
        RuleMatcher.urlMatchersOverlap(UrlMatcher.Regex(".*"), UrlMatcher.Regex(".*")) shouldBe true
    }

    // ---- bodyMatchersOverlap ------------------------------------------------

    @Test
    fun `bodyMatchersOverlap mirrors urlMatchersOverlap rules`() {
        RuleMatcher.bodyMatchersOverlap(null, BodyMatcher.Exact("a")) shouldBe true
        RuleMatcher.bodyMatchersOverlap(BodyMatcher.Exact("a"), BodyMatcher.Exact("a")) shouldBe true
        RuleMatcher.bodyMatchersOverlap(BodyMatcher.Exact("a"), BodyMatcher.Exact("b")) shouldBe false
        RuleMatcher.bodyMatchersOverlap(BodyMatcher.Exact("hello world"), BodyMatcher.Contains("world")) shouldBe true
        RuleMatcher.bodyMatchersOverlap(BodyMatcher.Regex(".*"), BodyMatcher.Exact("anything")) shouldBe true
    }

    // ---- headerMatchersOverlap ----------------------------------------------

    @Test
    fun `headerMatchersOverlap is true when either side is empty`() {
        RuleMatcher.headerMatchersOverlap(emptyList(), listOf(HeaderMatcher.KeyExists("X"))) shouldBe true
        RuleMatcher.headerMatchersOverlap(listOf(HeaderMatcher.KeyExists("X")), emptyList()) shouldBe true
    }

    @Test
    fun `headerMatchersOverlap is true when there are no shared keys`() {
        val a = listOf(HeaderMatcher.ValueExact("Authorization", "abc"))
        val b = listOf(HeaderMatcher.ValueExact("Accept", "json"))

        RuleMatcher.headerMatchersOverlap(a, b) shouldBe true
    }

    @Test
    fun `headerMatchersOverlap on shared key checks all pairs`() {
        val a = listOf(HeaderMatcher.ValueExact("Authorization", "abc"))
        val sameValue = listOf(HeaderMatcher.ValueExact("authorization", "ABC"))
        val differentValue = listOf(HeaderMatcher.ValueExact("authorization", "xyz"))

        RuleMatcher.headerMatchersOverlap(a, sameValue) shouldBe true
        RuleMatcher.headerMatchersOverlap(a, differentValue) shouldBe false
    }

    @Test
    fun `headerMatchersOverlap with KeyExists on either side overlaps`() {
        val keyExists = listOf(HeaderMatcher.KeyExists("Authorization"))
        val valueExact = listOf(HeaderMatcher.ValueExact("Authorization", "abc"))

        RuleMatcher.headerMatchersOverlap(keyExists, valueExact) shouldBe true
        RuleMatcher.headerMatchersOverlap(valueExact, keyExists) shouldBe true
    }

    // ---- rulesOverlap (composition) ----------------------------------------

    @Test
    fun `rulesOverlap is true when every dimension overlaps`() {
        val a = ruleWith(
            method = "GET",
            urlMatcher = UrlMatcher.Contains("/api"),
            headerMatchers = listOf(HeaderMatcher.KeyExists("Authorization")),
            bodyMatcher = null,
        )
        val b = ruleWith(
            method = "*",
            urlMatcher = UrlMatcher.Exact("https://example.com/api/users"),
            headerMatchers = emptyList(),
            bodyMatcher = null,
        )

        RuleMatcher.rulesOverlap(a, b) shouldBe true
    }

    @Test
    fun `rulesOverlap is false when methods are distinct and non-wildcard`() {
        val a = ruleWith(method = "GET", urlMatcher = UrlMatcher.Contains("/api"))
        val b = ruleWith(method = "POST", urlMatcher = UrlMatcher.Contains("/api"))

        RuleMatcher.rulesOverlap(a, b) shouldBe false
    }

    @Test
    fun `rulesOverlap is false when url matchers don't overlap`() {
        val a = ruleWith(method = "*", urlMatcher = UrlMatcher.Exact("https://a"))
        val b = ruleWith(method = "*", urlMatcher = UrlMatcher.Exact("https://b"))

        RuleMatcher.rulesOverlap(a, b) shouldBe false
    }

    @Test
    fun `rulesOverlap is false when body matchers conflict on the same matcher type`() {
        val a = ruleWith(method = "*", bodyMatcher = BodyMatcher.Exact("payload1"))
        val b = ruleWith(method = "*", bodyMatcher = BodyMatcher.Exact("payload2"))

        RuleMatcher.rulesOverlap(a, b) shouldBe false
    }

    // ---- helpers ------------------------------------------------------------

    private fun ruleWith(
        method: String = "*",
        urlMatcher: UrlMatcher? = null,
        headerMatchers: List<HeaderMatcher> = emptyList(),
        bodyMatcher: BodyMatcher? = null,
    ) = WiretapRule(
        method = method,
        urlMatcher = urlMatcher,
        headerMatchers = headerMatchers,
        bodyMatcher = bodyMatcher,
        action = RuleAction.Mock(),
    )
}
