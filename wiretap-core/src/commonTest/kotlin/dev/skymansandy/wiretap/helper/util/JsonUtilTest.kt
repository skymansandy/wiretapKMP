/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.util

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class JsonUtilTest {

    @Test
    fun `object literal with braces is JSON-like`() {
        looksLikeJson("""{"a":1}""") shouldBe true
    }

    @Test
    fun `array literal with brackets is JSON-like`() {
        looksLikeJson("""[1,2,3]""") shouldBe true
    }

    @Test
    fun `surrounding whitespace is tolerated`() {
        looksLikeJson("   \n  { \"a\":1 }   \n") shouldBe true
    }

    @Test
    fun `mismatched open and close are not JSON-like`() {
        looksLikeJson("""{ "a": 1 ]""") shouldBe false
        looksLikeJson("""[ 1, 2, 3 }""") shouldBe false
    }

    @Test
    fun `plain text is not JSON-like`() {
        looksLikeJson("hello world") shouldBe false
    }

    @Test
    fun `empty string is not JSON-like`() {
        looksLikeJson("") shouldBe false
    }

    @Test
    fun `blank string is not JSON-like`() {
        looksLikeJson("   \t   ") shouldBe false
    }

    @Test
    fun `single brace or bracket is not JSON-like`() {
        looksLikeJson("{") shouldBe false
        looksLikeJson("[") shouldBe false
    }
}
