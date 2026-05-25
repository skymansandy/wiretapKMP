/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.plugin.ws.util

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class SocketUrlUtilTest : DescribeSpec({
    describe("toWebSocketUrl") {
        it("replaces http scheme with ws") {
            "http://example.com/socket".toWebSocketUrl() shouldBe "ws://example.com/socket"
        }

        it("replaces https scheme with wss") {
            "https://example.com/socket".toWebSocketUrl() shouldBe "wss://example.com/socket"
        }

        it("leaves ws scheme unchanged") {
            "ws://example.com/socket".toWebSocketUrl() shouldBe "ws://example.com/socket"
        }

        it("leaves wss scheme unchanged") {
            "wss://example.com/socket".toWebSocketUrl() shouldBe "wss://example.com/socket"
        }

        it("only replaces the leading scheme prefix") {
            "https://x.com/path?ref=https://y".toWebSocketUrl() shouldBe "wss://x.com/path?ref=https://y"
        }

        it("leaves non-http schemes unchanged") {
            "ftp://example.com".toWebSocketUrl() shouldBe "ftp://example.com"
        }

        it("leaves empty string unchanged") {
            "".toWebSocketUrl() shouldBe ""
        }
    }
})
