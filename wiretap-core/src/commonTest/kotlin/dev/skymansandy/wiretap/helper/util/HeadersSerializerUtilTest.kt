/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.util

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class HeadersSerializerUtilTest : DescribeSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    describe("serialize") {
        it("empty map returns empty string") {
            HeadersSerializerUtil.serialize(emptyMap()) shouldBe ""
        }

        it("joins entries with newline and colon-space") {
            val serialized = HeadersSerializerUtil.serialize(
                linkedMapOf(
                    "Content-Type" to "application/json",
                    "Accept" to "text/plain",
                ),
            )

            serialized shouldBe "Content-Type: application/json\nAccept: text/plain"
        }
    }

    describe("deserialize") {
        it("empty string returns empty map") {
            HeadersSerializerUtil.deserialize("") shouldBe emptyMap()
        }

        it("blank string returns empty map") {
            HeadersSerializerUtil.deserialize("   \n   ") shouldBe emptyMap()
        }

        it("ignores lines without colon") {
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

        it("trims surrounding whitespace from keys and values") {
            val raw = "  X-Custom  :   value-with-padding   "

            HeadersSerializerUtil.deserialize(raw) shouldBe mapOf("X-Custom" to "value-with-padding")
        }

        it("preserves additional colons in the value") {
            val raw = "Authorization: Bearer abc:def:ghi"

            HeadersSerializerUtil.deserialize(raw) shouldBe mapOf(
                "Authorization" to "Bearer abc:def:ghi",
            )
        }

        it("keeps the last value when keys collide") {
            val raw = """
                Set-Cookie: a=1
                Set-Cookie: b=2
            """.trimIndent()

            HeadersSerializerUtil.deserialize(raw) shouldBe mapOf("Set-Cookie" to "b=2")
        }
    }

    describe("round-trip") {
        it("preserves keys and values") {
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
    }
})
