/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.util

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class HeadersSerializerUtilTest {

    @Test
    fun `serialize empty map returns empty string`() {
        HeadersSerializerUtil.serialize(emptyMap()) shouldBe ""
    }

    @Test
    fun `deserialize empty string returns empty map`() {
        HeadersSerializerUtil.deserialize("") shouldBe emptyMap()
    }

    @Test
    fun `deserialize blank string returns empty map`() {
        HeadersSerializerUtil.deserialize("   \n   ") shouldBe emptyMap()
    }

    @Test
    fun `serialize joins entries with newline and colon-space`() {
        val serialized = HeadersSerializerUtil.serialize(
            linkedMapOf(
                "Content-Type" to "application/json",
                "Accept" to "text/plain",
            ),
        )

        serialized shouldBe "Content-Type: application/json\nAccept: text/plain"
    }

    @Test
    fun `round-trip preserves keys and values`() {
        val headers = mapOf(
            "Authorization" to "Bearer abc",
            "Accept" to "application/json",
            "User-Agent" to "Wiretap/1.0",
        )

        val roundTripped = HeadersSerializerUtil.deserialize(
            HeadersSerializerUtil.serialize(headers),
        )

        roundTripped shouldBe headers
    }

    @Test
    fun `deserialize ignores lines without colon`() {
        val raw = """
            Content-Type: application/json
            not a header line
            Accept: text/plain
        """.trimIndent()

        HeadersSerializerUtil.deserialize(raw) shouldBe mapOf(
            "Content-Type" to "application/json",
            "Accept" to "text/plain",
        )
    }

    @Test
    fun `deserialize trims surrounding whitespace from keys and values`() {
        val raw = "  X-Custom  :   value-with-padding   "

        HeadersSerializerUtil.deserialize(raw) shouldBe mapOf("X-Custom" to "value-with-padding")
    }

    @Test
    fun `deserialize preserves additional colons in the value`() {
        val raw = "Authorization: Bearer abc:def:ghi"

        HeadersSerializerUtil.deserialize(raw) shouldBe mapOf(
            "Authorization" to "Bearer abc:def:ghi",
        )
    }

    @Test
    fun `deserialize keeps the last value when keys collide`() {
        val raw = """
            Set-Cookie: a=1
            Set-Cookie: b=2
        """.trimIndent()

        HeadersSerializerUtil.deserialize(raw) shouldBe mapOf("Set-Cookie" to "b=2")
    }
}
