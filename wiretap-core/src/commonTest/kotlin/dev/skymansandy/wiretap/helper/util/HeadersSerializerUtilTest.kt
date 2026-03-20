package dev.skymansandy.wiretap.helper.util

import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldBeEmpty
import kotlin.test.Test

class HeadersSerializerUtilTest {

    @Test
    fun `serialize empty map returns empty string`() {
        HeadersSerializerUtil.serialize(emptyMap()).shouldBeEmpty()
    }

    @Test
    fun `serialize single header`() {
        val headers = mapOf("Content-Type" to "application/json")

        val result = HeadersSerializerUtil.serialize(headers)

        result shouldBe "Content-Type: application/json"
    }

    @Test
    fun `serialize multiple headers joined by newline`() {
        val headers = mapOf(
            "Content-Type" to "application/json",
            "Accept" to "text/plain",
        )

        val result = HeadersSerializerUtil.serialize(headers)

        result shouldBe "Content-Type: application/json\nAccept: text/plain"
    }

    @Test
    fun `deserialize empty string returns empty map`() {
        HeadersSerializerUtil.deserialize("").shouldBeEmpty()
    }

    @Test
    fun `deserialize blank string returns empty map`() {
        HeadersSerializerUtil.deserialize("   ").shouldBeEmpty()
    }

    @Test
    fun `deserialize single header`() {
        val result = HeadersSerializerUtil.deserialize("Content-Type: application/json")

        result shouldContainExactly mapOf("Content-Type" to "application/json")
    }

    @Test
    fun `deserialize multiple headers`() {
        val result = HeadersSerializerUtil.deserialize("Content-Type: application/json\nAccept: text/plain")

        result shouldContainExactly mapOf(
            "Content-Type" to "application/json",
            "Accept" to "text/plain",
        )
    }

    @Test
    fun `deserialize handles header values containing colons`() {
        val result = HeadersSerializerUtil.deserialize("Location: https://example.com:8080/path")

        result shouldContainExactly mapOf("Location" to "https://example.com:8080/path")
    }

    @Test
    fun `deserialize skips lines without colon`() {
        val result = HeadersSerializerUtil.deserialize("Content-Type: application/json\nmalformed-line\nAccept: text/plain")

        result shouldContainExactly mapOf(
            "Content-Type" to "application/json",
            "Accept" to "text/plain",
        )
    }

    @Test
    fun `roundtrip serialization preserves data`() {
        val original = mapOf(
            "Content-Type" to "application/json",
            "Authorization" to "Bearer token123",
            "X-Custom" to "value",
        )

        val serialized = HeadersSerializerUtil.serialize(original)
        val deserialized = HeadersSerializerUtil.deserialize(serialized)

        deserialized shouldContainExactly original
    }
}
