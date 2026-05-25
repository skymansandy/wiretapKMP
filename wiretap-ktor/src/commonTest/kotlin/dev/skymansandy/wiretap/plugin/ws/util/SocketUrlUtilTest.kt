/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.plugin.ws.util

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SocketUrlUtilTest {

    @Test
    fun `http scheme is replaced with ws`() {
        "http://example.com/socket".toWebSocketUrl() shouldBe "ws://example.com/socket"
    }

    @Test
    fun `https scheme is replaced with wss`() {
        "https://example.com/socket".toWebSocketUrl() shouldBe "wss://example.com/socket"
    }

    @Test
    fun `ws scheme is left unchanged`() {
        "ws://example.com/socket".toWebSocketUrl() shouldBe "ws://example.com/socket"
    }

    @Test
    fun `wss scheme is left unchanged`() {
        "wss://example.com/socket".toWebSocketUrl() shouldBe "wss://example.com/socket"
    }

    @Test
    fun `only the leading scheme prefix is replaced`() {
        "https://x.com/path?ref=https://y".toWebSocketUrl() shouldBe "wss://x.com/path?ref=https://y"
    }

    @Test
    fun `non-http schemes are left unchanged`() {
        "ftp://example.com".toWebSocketUrl() shouldBe "ftp://example.com"
    }

    @Test
    fun `empty string is left unchanged`() {
        "".toWebSocketUrl() shouldBe ""
    }
}
