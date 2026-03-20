package dev.skymansandy.wiretap.config

import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class HeaderActionTest {

    @Test
    fun `applyHeaderAction - Keep preserves header`() {
        val headers = mapOf("Content-Type" to "application/json")

        val result = headers.applyHeaderAction { HeaderAction.Keep }

        result shouldContainExactly headers
    }

    @Test
    fun `applyHeaderAction - Skip removes header`() {
        val headers = mapOf(
            "Content-Type" to "application/json",
            "Authorization" to "Bearer secret",
        )

        val result = headers.applyHeaderAction { key ->
            if (key == "Authorization") HeaderAction.Skip else HeaderAction.Keep
        }

        result shouldContainExactly mapOf("Content-Type" to "application/json")
    }

    @Test
    fun `applyHeaderAction - Mask replaces value with default mask`() {
        val headers = mapOf("Authorization" to "Bearer secret-token")

        val result = headers.applyHeaderAction { HeaderAction.Mask() }

        result shouldContainExactly mapOf("Authorization" to "***")
    }

    @Test
    fun `applyHeaderAction - Mask replaces value with custom mask`() {
        val headers = mapOf("Authorization" to "Bearer secret-token")

        val result = headers.applyHeaderAction { HeaderAction.Mask("[REDACTED]") }

        result shouldContainExactly mapOf("Authorization" to "[REDACTED]")
    }

    @Test
    fun `applyHeaderAction - mixed actions`() {
        val headers = mapOf(
            "Content-Type" to "application/json",
            "Authorization" to "Bearer secret",
            "Cookie" to "session=abc123",
        )

        val result = headers.applyHeaderAction { key ->
            when {
                key.equals("Authorization", ignoreCase = true) -> HeaderAction.Mask()
                key.equals("Cookie", ignoreCase = true) -> HeaderAction.Skip
                else -> HeaderAction.Keep
            }
        }

        result shouldContainExactly mapOf(
            "Content-Type" to "application/json",
            "Authorization" to "***",
        )
    }

    @Test
    fun `applyHeaderAction - empty map returns empty map`() {
        val result = emptyMap<String, String>().applyHeaderAction { HeaderAction.Keep }

        result.shouldBeEmpty()
    }

    @Test
    fun `applyHeaderAction - skip all headers returns empty map`() {
        val headers = mapOf(
            "Content-Type" to "application/json",
            "Accept" to "text/plain",
        )

        val result = headers.applyHeaderAction { HeaderAction.Skip }

        result.shouldBeEmpty()
    }

    @Test
    fun `Mask default value is triple asterisk`() {
        HeaderAction.Mask().mask shouldBe "***"
    }
}
