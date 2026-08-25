/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.constants

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe

class HttpConstantsTest : DescribeSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    describe("HTTP_METHODS") {
        it("offers QUERY (RFC 10008)") {
            HTTP_METHODS shouldContain "QUERY"
        }

        it("offers the methods an app actually sends") {
            HTTP_METHODS shouldBe listOf(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "HEAD",
                "OPTIONS",
                "QUERY",
            )
        }

        it("excludes the rules wildcard") {
            HTTP_METHODS shouldNotContain "*"
        }

        it("has no duplicates") {
            HTTP_METHODS.distinct() shouldBe HTTP_METHODS
        }

        it("is uppercase throughout") {
            HTTP_METHODS.all { it == it.uppercase() } shouldBe true
        }
    }

    describe("RULE_HTTP_METHODS") {
        it("leads with the any-method wildcard") {
            RULE_HTTP_METHODS.first() shouldBe "*"
        }

        it("otherwise mirrors HTTP_METHODS so the two lists cannot drift") {
            RULE_HTTP_METHODS.drop(1) shouldBe HTTP_METHODS
        }
    }
})
