package dev.skymansandy.wiretap.helper.util

import dev.skymansandy.wiretap.domain.model.HeaderMatcher
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class HeaderMatcherSerializerTest {

    @Test
    fun `serialize empty list returns empty string`() {
        HeaderMatcherSerializer.serialize(emptyList()) shouldBe ""
    }

    @Test
    fun `serialize KeyExists`() {
        val matchers = listOf(HeaderMatcher.KeyExists("Authorization"))

        val result = HeaderMatcherSerializer.serialize(matchers)

        result shouldBe "K\tAuthorization\t"
    }

    @Test
    fun `serialize ValueExact`() {
        val matchers = listOf(HeaderMatcher.ValueExact("Content-Type", "application/json"))

        val result = HeaderMatcherSerializer.serialize(matchers)

        result shouldBe "VE\tContent-Type\tapplication/json"
    }

    @Test
    fun `serialize ValueContains`() {
        val matchers = listOf(HeaderMatcher.ValueContains("Accept", "json"))

        val result = HeaderMatcherSerializer.serialize(matchers)

        result shouldBe "VC\tAccept\tjson"
    }

    @Test
    fun `serialize ValueRegex`() {
        val matchers = listOf(HeaderMatcher.ValueRegex("Authorization", "Bearer .+"))

        val result = HeaderMatcherSerializer.serialize(matchers)

        result shouldBe "VR\tAuthorization\tBearer .+"
    }

    @Test
    fun `serialize multiple matchers`() {
        val matchers = listOf(
            HeaderMatcher.KeyExists("X-Key"),
            HeaderMatcher.ValueExact("Content-Type", "text/plain"),
        )

        val result = HeaderMatcherSerializer.serialize(matchers)

        result shouldBe "K\tX-Key\t\nVE\tContent-Type\ttext/plain"
    }

    @Test
    fun `deserialize empty string returns empty list`() {
        HeaderMatcherSerializer.deserialize("").shouldBeEmpty()
    }

    @Test
    fun `deserialize blank string returns empty list`() {
        HeaderMatcherSerializer.deserialize("   ").shouldBeEmpty()
    }

    @Test
    fun `deserialize KeyExists`() {
        val result = HeaderMatcherSerializer.deserialize("K\tAuthorization\t")

        result.shouldContainExactly(HeaderMatcher.KeyExists("Authorization"))
    }

    @Test
    fun `deserialize ValueExact`() {
        val result = HeaderMatcherSerializer.deserialize("VE\tContent-Type\tapplication/json")

        result.shouldContainExactly(HeaderMatcher.ValueExact("Content-Type", "application/json"))
    }

    @Test
    fun `deserialize ValueContains`() {
        val result = HeaderMatcherSerializer.deserialize("VC\tAccept\tjson")

        result.shouldContainExactly(HeaderMatcher.ValueContains("Accept", "json"))
    }

    @Test
    fun `deserialize ValueRegex`() {
        val result = HeaderMatcherSerializer.deserialize("VR\tAuthorization\tBearer .+")

        result.shouldContainExactly(HeaderMatcher.ValueRegex("Authorization", "Bearer .+"))
    }

    @Test
    fun `deserialize skips unknown type`() {
        val result = HeaderMatcherSerializer.deserialize("XX\tkey\tvalue")

        result.shouldBeEmpty()
    }

    @Test
    fun `deserialize skips entries with insufficient parts`() {
        val result = HeaderMatcherSerializer.deserialize("K")

        result.shouldBeEmpty()
    }

    @Test
    fun `roundtrip serialization preserves data`() {
        val original = listOf(
            HeaderMatcher.KeyExists("X-Request-Id"),
            HeaderMatcher.ValueExact("Content-Type", "application/json"),
            HeaderMatcher.ValueContains("Accept", "json"),
            HeaderMatcher.ValueRegex("Authorization", "Bearer .+"),
        )

        val serialized = HeaderMatcherSerializer.serialize(original)
        val deserialized = HeaderMatcherSerializer.deserialize(serialized)

        deserialized.shouldContainExactly(original)
    }
}
