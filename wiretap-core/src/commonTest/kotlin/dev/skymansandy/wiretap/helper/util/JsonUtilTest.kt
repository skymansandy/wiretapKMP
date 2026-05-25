/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.util

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class JsonUtilTest : DescribeSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    describe("looksLikeJson") {
        it("object literal with braces is JSON-like") {
            looksLikeJson("""{"a":1}""") shouldBe true
        }

        it("array literal with brackets is JSON-like") {
            looksLikeJson("""[1,2,3]""") shouldBe true
        }

        it("surrounding whitespace is tolerated") {
            looksLikeJson("   \n  { \"a\":1 }   \n") shouldBe true
        }

        it("mismatched open and close are not JSON-like") {
            looksLikeJson("""{ "a": 1 ]""") shouldBe false
            looksLikeJson("""[ 1, 2, 3 }""") shouldBe false
        }

        it("plain text is not JSON-like") {
            looksLikeJson("hello world") shouldBe false
        }

        it("empty string is not JSON-like") {
            looksLikeJson("") shouldBe false
        }

        it("blank string is not JSON-like") {
            looksLikeJson("   \t   ") shouldBe false
        }

        it("single brace or bracket is not JSON-like") {
            looksLikeJson("{") shouldBe false
            looksLikeJson("[") shouldBe false
        }
    }
})
