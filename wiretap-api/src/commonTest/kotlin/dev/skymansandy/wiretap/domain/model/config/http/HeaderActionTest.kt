/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.domain.model.config.http

import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.maps.shouldNotContainKey
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class HeaderActionTest {

    private val headers = mapOf(
        "Authorization" to "Bearer secret",
        "Cookie" to "sid=abc",
        "Accept" to "application/json",
    )

    @Test
    fun `applyHeaderAction keeps every header when action returns Keep`() {
        val result = headers.applyHeaderAction { HeaderAction.Keep }

        result shouldContainExactly headers
    }

    @Test
    fun `applyHeaderAction omits headers whose action is Skip`() {
        val result = headers.applyHeaderAction { key ->
            if (key == "Cookie") HeaderAction.Skip else HeaderAction.Keep
        }

        result shouldNotContainKey "Cookie"
        result["Authorization"] shouldBe "Bearer secret"
        result["Accept"] shouldBe "application/json"
    }

    @Test
    fun `applyHeaderAction replaces value with default mask`() {
        val result = headers.applyHeaderAction { key ->
            if (key == "Authorization") HeaderAction.Mask() else HeaderAction.Keep
        }

        result["Authorization"] shouldBe "***"
    }

    @Test
    fun `applyHeaderAction uses caller-provided mask string`() {
        val result = headers.applyHeaderAction { key ->
            if (key == "Authorization") HeaderAction.Mask("REDACTED") else HeaderAction.Keep
        }

        result["Authorization"] shouldBe "REDACTED"
    }

    @Test
    fun `applyHeaderAction can mix Keep Skip and Mask in one pass`() {
        val result = headers.applyHeaderAction { key ->
            when (key) {
                "Authorization" -> HeaderAction.Mask()
                "Cookie" -> HeaderAction.Skip
                else -> HeaderAction.Keep
            }
        }

        result shouldContainExactly mapOf(
            "Authorization" to "***",
            "Accept" to "application/json",
        )
    }

    @Test
    fun `applyHeaderAction on empty map returns empty map`() {
        val result = emptyMap<String, String>().applyHeaderAction { HeaderAction.Keep }

        result shouldBe emptyMap()
    }

    @Test
    fun `applyHeaderAction never mutates the source map`() {
        val source = mutableMapOf("X-Test" to "value")

        source.applyHeaderAction { HeaderAction.Skip }

        source shouldContainExactly mapOf("X-Test" to "value")
    }

    @Test
    fun `applyHeaderAction passes the original casing of the key to the action`() {
        var seenKey: String? = null
        mapOf("X-Custom-Header" to "v").applyHeaderAction { key ->
            seenKey = key
            HeaderAction.Keep
        }

        seenKey shouldBe "X-Custom-Header"
    }
}
