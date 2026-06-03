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
    }

    describe("enabled override") {
        it("can be flipped off") {
            val config = WiretapWsConfig().apply { enabled = false }

            config.enabled shouldBe false
        }
    }
})
