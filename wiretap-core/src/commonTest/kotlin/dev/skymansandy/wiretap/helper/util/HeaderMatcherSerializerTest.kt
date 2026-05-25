/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.util

import dev.skymansandy.wiretap.domain.model.matchers.HeaderMatcher
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class HeaderMatcherSerializerTest : DescribeSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    describe("serialize") {
        it("empty list returns empty string") {
            HeaderMatcherSerializer.serialize(emptyList()) shouldBe ""
        }

        it("KeyExists serializes with a trailing empty value field") {
            val serialized = HeaderMatcherSerializer.serialize(
                listOf(HeaderMatcher.KeyExists("Authorization")),
            )

            serialized shouldBe "K\tAuthorization\t"
        }
    }

    describe("deserialize") {
        it("empty string returns empty list") {
            HeaderMatcherSerializer.deserialize("") shouldBe emptyList()
        }

        it("blank string returns empty list") {
            HeaderMatcherSerializer.deserialize("   \n  \t  ") shouldBe emptyList()
        }

        it("discards lines with unknown type marker") {
            val raw = listOf(
                "K\tAuthorization\t",
                "X\tFoo\tBar",
                "VE\tContent-Type\tapplication/json",
            ).joinToString("\n")

            val result = HeaderMatcherSerializer.deserialize(raw)

            result.size shouldBe 2
            result[0].shouldBeInstanceOf<HeaderMatcher.KeyExists>()
            result[1].shouldBeInstanceOf<HeaderMatcher.ValueExact>()
        }

        it("skips entries with fewer than two fields") {
            val raw = listOf(
                "VE\tContent-Type\tapplication/json",
                "K",
                "VC\tUser-Agent\tAndroid",
            ).joinToString("\n")

            val result = HeaderMatcherSerializer.deserialize(raw)

            result.size shouldBe 2
        }

        it("treats missing value column as empty string") {
            val result = HeaderMatcherSerializer.deserialize("VE\tContent-Type")

            result shouldBe listOf(HeaderMatcher.ValueExact("Content-Type", ""))
        }
    }

    describe("round-trip") {
        it("KeyExists preserves the key") {
            val matchers = listOf(HeaderMatcher.KeyExists("Authorization"))

            val roundTripped = HeaderMatcherSerializer.deserialize(
                HeaderMatcherSerializer.serialize(matchers),
            )

            roundTripped shouldBe matchers
        }

        it("ValueExact preserves key and value") {
            val matchers = listOf(HeaderMatcher.ValueExact("Content-Type", "application/json"))

            val roundTripped = HeaderMatcherSerializer.deserialize(
                HeaderMatcherSerializer.serialize(matchers),
            )

            roundTripped shouldBe matchers
        }

        it("ValueContains preserves key and value") {
            val matchers = listOf(HeaderMatcher.ValueContains("User-Agent", "Android"))

            val roundTripped = HeaderMatcherSerializer.deserialize(
                HeaderMatcherSerializer.serialize(matchers),
            )

            roundTripped shouldBe matchers
        }

        it("ValueRegex preserves key and pattern") {
            val matchers = listOf(HeaderMatcher.ValueRegex("X-Trace-Id", "[a-f0-9]{32}"))

            val roundTripped = HeaderMatcherSerializer.deserialize(
                HeaderMatcherSerializer.serialize(matchers),
            )

            roundTripped shouldBe matchers
        }

        it("mixed-type list round-trips and preserves order") {
            val matchers = listOf(
                HeaderMatcher.KeyExists("Authorization"),
                HeaderMatcher.ValueExact("Content-Type", "application/json"),
                HeaderMatcher.ValueContains("User-Agent", "Mobile"),
                HeaderMatcher.ValueRegex("X-Trace-Id", "[a-f0-9]+"),
            )

            val roundTripped = HeaderMatcherSerializer.deserialize(
                HeaderMatcherSerializer.serialize(matchers),
            )

            roundTripped shouldBe matchers
        }
    }
})
