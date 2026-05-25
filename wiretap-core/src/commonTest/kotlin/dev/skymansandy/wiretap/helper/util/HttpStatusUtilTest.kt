/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.util

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class HttpStatusUtilTest : DescribeSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    describe("httpStatusReason") {
        it("well-known 2xx codes resolve to their reason phrase") {
            httpStatusReason(200) shouldBe "OK"
            httpStatusReason(201) shouldBe "Created"
            httpStatusReason(204) shouldBe "No Content"
        }

        it("well-known 4xx codes resolve to their reason phrase") {
            httpStatusReason(400) shouldBe "Bad Request"
            httpStatusReason(401) shouldBe "Unauthorized"
            httpStatusReason(404) shouldBe "Not Found"
            httpStatusReason(418) shouldBe "I'm a Teapot"
        }

        it("well-known 5xx codes resolve to their reason phrase") {
            httpStatusReason(500) shouldBe "Internal Server Error"
            httpStatusReason(502) shouldBe "Bad Gateway"
        }

        it("unknown status code returns null") {
            httpStatusReason(999) shouldBe null
        }

        it("negative status code returns null") {
            httpStatusReason(-1) shouldBe null
        }

        it("zero status code returns null") {
            httpStatusReason(0) shouldBe null
        }
    }
})
