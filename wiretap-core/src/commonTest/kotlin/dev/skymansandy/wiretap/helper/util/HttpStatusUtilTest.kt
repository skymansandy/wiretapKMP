/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.util

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class HttpStatusUtilTest {

    @Test
    fun `well-known 2xx codes resolve to their reason phrase`() {
        httpStatusReason(200) shouldBe "OK"
        httpStatusReason(201) shouldBe "Created"
        httpStatusReason(204) shouldBe "No Content"
    }

    @Test
    fun `well-known 4xx codes resolve to their reason phrase`() {
        httpStatusReason(400) shouldBe "Bad Request"
        httpStatusReason(401) shouldBe "Unauthorized"
        httpStatusReason(404) shouldBe "Not Found"
        httpStatusReason(418) shouldBe "I'm a Teapot"
    }

    @Test
    fun `well-known 5xx codes resolve to their reason phrase`() {
        httpStatusReason(500) shouldBe "Internal Server Error"
        httpStatusReason(502) shouldBe "Bad Gateway"
    }

    @Test
    fun `unknown status code returns null`() {
        httpStatusReason(999) shouldBe null
    }

    @Test
    fun `negative status code returns null`() {
        httpStatusReason(-1) shouldBe null
    }

    @Test
    fun `zero status code returns null`() {
        httpStatusReason(0) shouldBe null
    }
}
