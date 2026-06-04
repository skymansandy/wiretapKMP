/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.domain.model.config.ws

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class WiretapWsConfigTest : DescribeSpec({

    describe("defaults") {
        it("enables logging out of the box") {
            WiretapWsConfig().enabled shouldBe true
        }

        it("decodes binary frames using the Auto heuristic") {
            WiretapWsConfig().binaryDecoding shouldBe BinaryFrameDecoding.Auto
        }
    }

    describe("enabled override") {
        it("can be flipped off") {
            val config = WiretapWsConfig().apply { enabled = false }

            config.enabled shouldBe false
        }
    }

    describe("binaryDecoding override") {
        it("accepts Utf8") {
            val config = WiretapWsConfig().apply { binaryDecoding = BinaryFrameDecoding.Utf8 }

            config.binaryDecoding shouldBe BinaryFrameDecoding.Utf8
        }

        it("accepts Placeholder") {
            val config = WiretapWsConfig().apply { binaryDecoding = BinaryFrameDecoding.Placeholder }

            config.binaryDecoding shouldBe BinaryFrameDecoding.Placeholder
        }
    }
})
