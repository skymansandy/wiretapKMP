package dev.skymansandy.wiretap.config

import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.test.Test

class WiretapConfigTest {

    @Test
    fun `default enabled is true`() {
        WiretapConfig().enabled.shouldBeTrue()
    }

    @Test
    fun `default shouldLog captures everything`() {
        val config = WiretapConfig()
        config.shouldLog("https://any.url", "GET").shouldBeTrue()
        config.shouldLog("", "POST").shouldBeTrue()
    }

    @Test
    fun `default headerAction is Keep`() {
        val config = WiretapConfig()
        config.headerAction("any-header").shouldBeInstanceOf<HeaderAction.Keep>()
    }

    @Test
    fun `default logRetention is Forever`() {
        val config = WiretapConfig()
        config.logRetention.shouldBeInstanceOf<LogRetention.Forever>()
    }

    @Test
    fun `shouldLog can be customized to filter by url`() {
        val config = WiretapConfig().apply {
            shouldLog = { url, _ -> url.contains("/api/") }
        }

        config.shouldLog("https://example.com/api/users", "GET").shouldBeTrue()
        config.shouldLog("https://example.com/health", "GET") shouldBe false
    }

    @Test
    fun `logRetention Days stores days value`() {
        val retention = LogRetention.Days(7)
        retention.days shouldBe 7
    }
}
