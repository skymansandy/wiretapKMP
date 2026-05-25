/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.domain.model.config.http

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class WiretapHttpConfigTest : DescribeSpec({

    describe("defaults") {
        it("are sensible") {
            val config = WiretapHttpConfig()

            config.enabled shouldBe true
            config.shouldLog("any-url", "GET") shouldBe true
            config.headerAction("Authorization") shouldBe HeaderAction.Keep
            config.logRetention shouldBe LogRetention.Forever
            config.maxContentLength shouldBe WiretapHttpConfig.MAX_CONTENT_LENGTH
        }
    }

    describe("maxContentLength") {
        it("accepts values in range") {
            val config = WiretapHttpConfig().apply { maxContentLength = 1024 }

            config.maxContentLength shouldBe 1024
        }

        it("clamps negative values to zero") {
            val config = WiretapHttpConfig().apply { maxContentLength = -1 }

            config.maxContentLength shouldBe 0
        }

        it("clamps values above MAX_CONTENT_LENGTH") {
            val config = WiretapHttpConfig().apply {
                maxContentLength = WiretapHttpConfig.MAX_CONTENT_LENGTH + 1
            }

            config.maxContentLength shouldBe WiretapHttpConfig.MAX_CONTENT_LENGTH
        }

        it("allows zero to disable body logging") {
            val config = WiretapHttpConfig().apply { maxContentLength = 0 }

            config.maxContentLength shouldBe 0
        }
    }

    describe("MAX_CONTENT_LENGTH constant") {
        it("equals 500 KB") {
            WiretapHttpConfig.MAX_CONTENT_LENGTH shouldBe 500 * 1024
        }
    }

    describe("shouldLog") {
        it("can be overridden to filter requests") {
            val config = WiretapHttpConfig().apply {
                shouldLog = { url, _ -> url.contains("/api/") }
            }

            config.shouldLog("https://example.com/api/users", "GET") shouldBe true
            config.shouldLog("https://example.com/static/main.css", "GET") shouldBe false
        }
    }

    describe("headerAction") {
        it("can be overridden to mask specific headers") {
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
})
