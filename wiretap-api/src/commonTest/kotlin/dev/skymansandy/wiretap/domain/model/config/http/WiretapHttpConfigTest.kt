/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.domain.model.config.http

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.test.Test

class WiretapHttpConfigTest {

    @Test
    fun `defaults are sensible`() {
        val config = WiretapHttpConfig()

        config.enabled shouldBe true
        config.shouldLog("any-url", "GET") shouldBe true
        config.headerAction("Authorization") shouldBe HeaderAction.Keep
        config.logRetention shouldBe LogRetention.Forever
        config.maxContentLength shouldBe WiretapHttpConfig.MAX_CONTENT_LENGTH
    }

    @Test
    fun `maxContentLength accepts values in range`() {
        val config = WiretapHttpConfig().apply { maxContentLength = 1024 }

        config.maxContentLength shouldBe 1024
    }

    @Test
    fun `maxContentLength clamps negative values to zero`() {
        val config = WiretapHttpConfig().apply { maxContentLength = -1 }

        config.maxContentLength shouldBe 0
    }

    @Test
    fun `maxContentLength clamps values above MAX_CONTENT_LENGTH`() {
        val config = WiretapHttpConfig().apply {
            maxContentLength = WiretapHttpConfig.MAX_CONTENT_LENGTH + 1
        }

        config.maxContentLength shouldBe WiretapHttpConfig.MAX_CONTENT_LENGTH
    }

    @Test
    fun `maxContentLength allows zero to disable body logging`() {
        val config = WiretapHttpConfig().apply { maxContentLength = 0 }

        config.maxContentLength shouldBe 0
    }

    @Test
    fun `MAX_CONTENT_LENGTH constant equals 500 KB`() {
        WiretapHttpConfig.MAX_CONTENT_LENGTH shouldBe 500 * 1024
    }

    @Test
    fun `shouldLog can be overridden to filter requests`() {
        val config = WiretapHttpConfig().apply {
            shouldLog = { url, _ -> url.contains("/api/") }
        }

        config.shouldLog("https://example.com/api/users", "GET") shouldBe true
        config.shouldLog("https://example.com/static/main.css", "GET") shouldBe false
    }

    @Test
    fun `headerAction can be overridden to mask specific headers`() {
        val config = WiretapHttpConfig().apply {
            headerAction = { key ->
                if (key.equals("Authorization", ignoreCase = true)) HeaderAction.Mask("X")
                else HeaderAction.Keep
            }
        }

        val masked = config.headerAction("authorization")
        masked.shouldBeInstanceOf<HeaderAction.Mask>()
        masked.mask shouldBe "X"

        config.headerAction("Accept") shouldBe HeaderAction.Keep
    }
}
