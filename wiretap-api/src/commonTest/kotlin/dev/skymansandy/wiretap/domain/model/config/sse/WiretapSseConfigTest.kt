/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.domain.model.config.sse

import dev.skymansandy.wiretap.helper.markers.ExperimentalWiretapSseApi
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

@OptIn(ExperimentalWiretapSseApi::class)
class WiretapSseConfigTest : DescribeSpec({

    describe("defaults") {
        it("enables logging out of the box") {
            WiretapSseConfig().enabled shouldBe true
        }
    }

    describe("enabled override") {
        it("can be flipped off") {
            val config = WiretapSseConfig().apply { enabled = false }

            config.enabled shouldBe false
        }
    }
})
