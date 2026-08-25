/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.util

import dev.skymansandy.wiretap.ui.theme.WiretapColors
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe

class TextHighlightUtilTest : DescribeSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    describe("highlightText") {
        it("returns the text unstyled when the query is blank") {
            val result = highlightText("hello world", "")

            result.text shouldBe "hello world"
            result.spanStyles.shouldBeEmpty()
        }

        it("highlights every occurrence of the query") {
            val result = highlightText("ping pong ping", "ping")

            result.text shouldBe "ping pong ping"
            result.spanStyles.map { it.start to it.end } shouldBe listOf(0 to 4, 10 to 14)
        }

        it("matches regardless of case while preserving the original text") {
            val result = highlightText("Hello World", "world")

            result.text shouldBe "Hello World"
            result.spanStyles.map { it.start to it.end } shouldBe listOf(6 to 11)
        }

        it("paints the active range with the active highlight colour") {
            val result = highlightText("ping pong ping", "ping", activeRange = 10..13)

            result.spanStyles.map { it.item.background } shouldBe listOf(
                WiretapColors.SearchHighlightBackground,
                WiretapColors.SearchHighlightActiveBackground,
            )
        }

        it("survives text whose lowercase form is longer than the original") {
            // U+0130 lowercases to two characters, so offsets taken from a
            // lowercased copy no longer line up with the original string.
            shouldNotThrowAny {
                highlightText("İİ", "i")
            }
        }

        it("keeps offsets valid against the original text for case-expanding characters") {
            val text = "İXİ"
            val result = highlightText(text, "x")

            result.text shouldBe text
            result.spanStyles.map { it.start to it.end } shouldBe listOf(1 to 2)
        }
    }
})
