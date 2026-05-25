/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.ui.model

import dev.skymansandy.wiretap.domain.model.RuleAction
import dev.skymansandy.wiretap.domain.model.WiretapRule
import dev.skymansandy.wiretap.domain.model.matchers.BodyMatcher
import dev.skymansandy.wiretap.domain.model.matchers.HeaderMatcher
import dev.skymansandy.wiretap.domain.model.matchers.UrlMatcher
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class RuleFormExtensionsTest {

    // ---- isRegex / hasValue --------------------------------------------------

    @Test
    fun `UrlMatchMode isRegex only true for Regex variant`() {
        UrlMatchMode.Exact.isRegex() shouldBe false
        UrlMatchMode.Contains.isRegex() shouldBe false
        UrlMatchMode.Regex.isRegex() shouldBe true
    }

    @Test
    fun `BodyMatchMode isRegex only true for Regex variant`() {
        BodyMatchMode.Exact.isRegex() shouldBe false
        BodyMatchMode.Contains.isRegex() shouldBe false
        BodyMatchMode.Regex.isRegex() shouldBe true
    }

    @Test
    fun `HeaderEntryMode isRegex only true for ValueRegex variant`() {
        HeaderEntryMode.KeyExists.isRegex() shouldBe false
        HeaderEntryMode.ValueExact.isRegex() shouldBe false
        HeaderEntryMode.ValueContains.isRegex() shouldBe false
        HeaderEntryMode.ValueRegex.isRegex() shouldBe true
    }

    @Test
    fun `HeaderEntryMode hasValue is false only for KeyExists`() {
        HeaderEntryMode.KeyExists.hasValue() shouldBe false
        HeaderEntryMode.ValueExact.hasValue() shouldBe true
        HeaderEntryMode.ValueContains.hasValue() shouldBe true
        HeaderEntryMode.ValueRegex.hasValue() shouldBe true
    }

    // ---- WiretapRule mode extractors ----------------------------------------

    @Test
    fun `toUrlMode returns matching mode for each UrlMatcher subtype`() {
        ruleWith(url = UrlMatcher.Exact("x")).toUrlMode() shouldBe UrlMatchMode.Exact
        ruleWith(url = UrlMatcher.Contains("x")).toUrlMode() shouldBe UrlMatchMode.Contains
        ruleWith(url = UrlMatcher.Regex("x")).toUrlMode() shouldBe UrlMatchMode.Regex
    }

    @Test
    fun `toUrlMode returns null when rule has no url matcher`() {
        ruleWith(url = null).toUrlMode() shouldBe null
    }

    @Test
    fun `toBodyMode returns matching mode for each BodyMatcher subtype`() {
        ruleWith(body = BodyMatcher.Exact("x")).toBodyMode() shouldBe BodyMatchMode.Exact
        ruleWith(body = BodyMatcher.Contains("x")).toBodyMode() shouldBe BodyMatchMode.Contains
        ruleWith(body = BodyMatcher.Regex("x")).toBodyMode() shouldBe BodyMatchMode.Regex
    }

    @Test
    fun `toBodyMode returns null when rule has no body matcher`() {
        ruleWith(body = null).toBodyMode() shouldBe null
    }

    // ---- HeaderMatcher to HeaderEntry --------------------------------------

    @Test
    fun `KeyExists matcher maps to HeaderEntry with KeyExists mode and blank value`() {
        val entry = HeaderMatcher.KeyExists("Authorization").toEntry()

        entry.key shouldBe "Authorization"
        entry.value shouldBe ""
        entry.mode shouldBe HeaderEntryMode.KeyExists
    }

    @Test
    fun `ValueExact maps key and value with ValueExact mode`() {
        val entry = HeaderMatcher.ValueExact("Content-Type", "application/json").toEntry()

        entry.key shouldBe "Content-Type"
        entry.value shouldBe "application/json"
        entry.mode shouldBe HeaderEntryMode.ValueExact
    }

    @Test
    fun `ValueContains maps key and value with ValueContains mode`() {
        val entry = HeaderMatcher.ValueContains("User-Agent", "Android").toEntry()

        entry.key shouldBe "User-Agent"
        entry.value shouldBe "Android"
        entry.mode shouldBe HeaderEntryMode.ValueContains
    }

    @Test
    fun `ValueRegex maps pattern into the value field with ValueRegex mode`() {
        val entry = HeaderMatcher.ValueRegex("X-Trace-Id", "[a-f0-9]+").toEntry()

        entry.key shouldBe "X-Trace-Id"
        entry.value shouldBe "[a-f0-9]+"
        entry.mode shouldBe HeaderEntryMode.ValueRegex
    }

    // ---- HeaderEntry to HeaderMatcher --------------------------------------

    @Test
    fun `toDomain returns null when key is blank`() {
        HeaderEntry(key = "", value = "v", mode = HeaderEntryMode.ValueExact).toDomain() shouldBe null
        HeaderEntry(key = "   ", value = "v", mode = HeaderEntryMode.ValueExact).toDomain() shouldBe null
    }

    @Test
    fun `toDomain trims key and value`() {
        val result = HeaderEntry(
            key = "  Authorization  ",
            value = "  Bearer abc  ",
            mode = HeaderEntryMode.ValueExact,
        ).toDomain()

        result shouldBe HeaderMatcher.ValueExact("Authorization", "Bearer abc")
    }

    @Test
    fun `toDomain produces KeyExists matcher with trimmed key only`() {
        HeaderEntry(key = "Authorization", value = "ignored", mode = HeaderEntryMode.KeyExists)
            .toDomain() shouldBe HeaderMatcher.KeyExists("Authorization")
    }

    @Test
    fun `toDomain produces every value-mode variant`() {
        HeaderEntry(key = "k", value = "v", mode = HeaderEntryMode.ValueExact)
            .toDomain() shouldBe HeaderMatcher.ValueExact("k", "v")
        HeaderEntry(key = "k", value = "v", mode = HeaderEntryMode.ValueContains)
            .toDomain() shouldBe HeaderMatcher.ValueContains("k", "v")
        HeaderEntry(key = "k", value = "v", mode = HeaderEntryMode.ValueRegex)
            .toDomain() shouldBe HeaderMatcher.ValueRegex("k", "v")
    }

    // ---- testRegex ---------------------------------------------------------

    @Test
    fun `testRegex returns matches true with null error on a valid match`() {
        val result = testRegex("[a-f0-9]+", "deadBEEF")

        result.matches shouldBe true
        result.error shouldBe null
    }

    @Test
    fun `testRegex is case-insensitive`() {
        testRegex("hello", "HELLO WORLD").matches shouldBe true
    }

    @Test
    fun `testRegex returns matches false with null error on no match`() {
        val result = testRegex("[0-9]+", "no digits here")

        result.matches shouldBe false
        result.error shouldBe null
    }

    @Test
    fun `testRegex returns matches false with non-null error for invalid pattern`() {
        val result = testRegex("[", "anything")

        result.matches shouldBe false
        (result.error == null) shouldBe false
    }

    // ---- urlFieldLabel / bodyFieldLabel -------------------------------------

    @Test
    fun `urlFieldLabel returns human-readable label per mode`() {
        urlFieldLabel(UrlMatchMode.Exact) shouldBe "URL is"
        urlFieldLabel(UrlMatchMode.Contains) shouldBe "URL contains"
        urlFieldLabel(UrlMatchMode.Regex) shouldBe "URL looks like"
    }

    @Test
    fun `bodyFieldLabel returns human-readable label per mode`() {
        bodyFieldLabel(BodyMatchMode.Exact) shouldBe "Body is"
        bodyFieldLabel(BodyMatchMode.Contains) shouldBe "Body contains"
        bodyFieldLabel(BodyMatchMode.Regex) shouldBe "Body looks like"
    }

    // ---- warning helpers ----------------------------------------------------

    @Test
    fun `urlWarning is null when mode is null`() {
        urlWarning(null, "") shouldBe null
        urlWarning(null, "value") shouldBe null
    }

    @Test
    fun `urlWarning is null when pattern is non-blank`() {
        urlWarning(UrlMatchMode.Exact, "https://x") shouldBe null
        urlWarning(UrlMatchMode.Contains, "/api") shouldBe null
        urlWarning(UrlMatchMode.Regex, ".*") shouldBe null
    }

    @Test
    fun `urlWarning returns a mode-specific message when pattern is blank`() {
        urlWarning(UrlMatchMode.Exact, "") shouldBe "Enter the exact URL to match"
        urlWarning(UrlMatchMode.Contains, " ") shouldBe "Enter a substring the URL should contain"
        urlWarning(UrlMatchMode.Regex, "\t") shouldBe "Enter a regex pattern to match the URL"
    }

    @Test
    fun `bodyWarning mirrors urlWarning shape`() {
        bodyWarning(null, "") shouldBe null
        bodyWarning(BodyMatchMode.Exact, "value") shouldBe null
        bodyWarning(BodyMatchMode.Exact, "") shouldBe "Enter the exact body to match"
        bodyWarning(BodyMatchMode.Contains, "") shouldBe "Enter a substring the body should contain"
        bodyWarning(BodyMatchMode.Regex, "") shouldBe "Enter a regex pattern to match the body"
    }

    @Test
    fun `headersWarning is null for empty list`() {
        headersWarning(emptyList()) shouldBe null
    }

    @Test
    fun `headersWarning flags blank key first`() {
        val entries = listOf(
            HeaderEntry(key = "  ", value = "v", mode = HeaderEntryMode.ValueExact),
        )

        headersWarning(entries) shouldBe "Header key is missing"
    }

    @Test
    fun `headersWarning is null when KeyExists has blank value`() {
        val entries = listOf(
            HeaderEntry(key = "Authorization", value = "", mode = HeaderEntryMode.KeyExists),
        )

        headersWarning(entries) shouldBe null
    }

    @Test
    fun `headersWarning emits mode-specific message for blank value`() {
        headersWarning(
            listOf(HeaderEntry(key = "Auth", value = "", mode = HeaderEntryMode.ValueExact)),
        ) shouldBe "Enter the exact header value for \"Auth\""

        headersWarning(
            listOf(HeaderEntry(key = "UA", value = "", mode = HeaderEntryMode.ValueContains)),
        ) shouldBe "Enter a substring for header \"UA\""

        headersWarning(
            listOf(HeaderEntry(key = "X", value = "", mode = HeaderEntryMode.ValueRegex)),
        ) shouldBe "Enter a regex pattern for header \"X\""
    }

    @Test
    fun `headersWarning returns null when every entry is valid`() {
        val entries = listOf(
            HeaderEntry(key = "Authorization", value = "abc", mode = HeaderEntryMode.ValueExact),
            HeaderEntry(key = "Accept", value = "", mode = HeaderEntryMode.KeyExists),
        )

        headersWarning(entries) shouldBe null
    }

    // ---- placeholders -------------------------------------------------------

    @Test
    fun `urlPlaceholder returns a mode-specific example`() {
        urlPlaceholder(UrlMatchMode.Exact) shouldBe "https://api.example.com/users/123"
        urlPlaceholder(UrlMatchMode.Contains) shouldBe "/users/"
        urlPlaceholder(UrlMatchMode.Regex) shouldBe "api\\.example\\.com/users/\\d+"
    }

    @Test
    fun `bodyPlaceholder returns a mode-specific example`() {
        bodyPlaceholder(BodyMatchMode.Exact) shouldBe "{\"status\": \"error\"}"
        bodyPlaceholder(BodyMatchMode.Contains) shouldBe "\"error\""
        bodyPlaceholder(BodyMatchMode.Regex) shouldBe "\"id\":\\s*\\d+"
    }

    @Test
    fun `headerValuePlaceholder is empty string for KeyExists mode`() {
        headerValuePlaceholder(HeaderEntryMode.KeyExists) shouldBe ""
    }

    @Test
    fun `headerValuePlaceholder returns mode-specific examples`() {
        headerValuePlaceholder(HeaderEntryMode.ValueExact) shouldBe "Bearer token123"
        headerValuePlaceholder(HeaderEntryMode.ValueContains) shouldBe "Bearer"
        headerValuePlaceholder(HeaderEntryMode.ValueRegex) shouldBe "Bearer\\s+\\S+"
    }

    // ---- ThrottleProfile labels --------------------------------------------

    @Test
    fun `every ThrottleProfile has a non-blank speed text`() {
        ThrottleProfile.entries.forEach { profile ->
            profile.speedText.isNotBlank() shouldBe true
        }
    }

    @Test
    fun `labelText mirrors label`() {
        ThrottleProfile.entries.forEach { profile ->
            profile.labelText shouldBe profile.label
        }
    }

    @Test
    fun `speedText is unique per profile`() {
        ThrottleProfile.entries.map { it.speedText }.toSet().size shouldBe
            ThrottleProfile.entries.size
    }

    // ---- helpers -----------------------------------------------------------

    private fun ruleWith(
        url: UrlMatcher? = null,
        body: BodyMatcher? = null,
    ) = WiretapRule(
        method = "*",
        urlMatcher = url,
        bodyMatcher = body,
        action = RuleAction.Mock(),
    )
}
