/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.util

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import kotlin.test.Test

class BodyTruncationUtilTest {

    @Test
    fun `maxLength of zero returns null even for non-null body`() {
        "anything".truncateBody(0) shouldBe null
    }

    @Test
    fun `maxLength of zero returns null for null body`() {
        null.truncateBody(0) shouldBe null
    }

    @Test
    fun `null body with positive maxLength returns null`() {
        null.truncateBody(100) shouldBe null
    }

    @Test
    fun `body shorter than maxLength is returned unchanged`() {
        "short".truncateBody(100) shouldBe "short"
    }

    @Test
    fun `body equal to maxLength is returned unchanged`() {
        val body = "1234567890"
        body.truncateBody(body.length) shouldBe body
    }

    @Test
    fun `body longer than maxLength is truncated and gets a suffix`() {
        val body = "0123456789ABCDEF"

        val truncated = body.truncateBody(5)

        truncated shouldStartWith "01234"
        truncated!!.length shouldBe ("01234".length + "\n\n--- [Wiretap] Body truncated (exceeded 5 chars) ---".length)
    }

    @Test
    fun `truncation suffix mentions the limit value`() {
        val truncated = "aaaaaaaaaa".truncateBody(3)

        truncated!!.contains("(exceeded 3 chars)") shouldBe true
    }

    @Test
    fun `empty body and maxLength positive returns empty body`() {
        "".truncateBody(100) shouldBe ""
    }
}
