package dev.skymansandy.wiretap.helper.util

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import kotlin.test.Test

class JsonUtilTest {

    @Test
    fun `looksLikeJson - valid json object`() {
        looksLikeJson("""{"key":"value"}""").shouldBeTrue()
    }

    @Test
    fun `looksLikeJson - valid json array`() {
        looksLikeJson("""[1, 2, 3]""").shouldBeTrue()
    }

    @Test
    fun `looksLikeJson - json with whitespace`() {
        looksLikeJson("  { \"key\": \"value\" }  ").shouldBeTrue()
    }

    @Test
    fun `looksLikeJson - empty object`() {
        looksLikeJson("{}").shouldBeTrue()
    }

    @Test
    fun `looksLikeJson - empty array`() {
        looksLikeJson("[]").shouldBeTrue()
    }

    @Test
    fun `looksLikeJson - plain text is not json`() {
        looksLikeJson("hello world").shouldBeFalse()
    }

    @Test
    fun `looksLikeJson - xml is not json`() {
        looksLikeJson("<root><item/></root>").shouldBeFalse()
    }

    @Test
    fun `looksLikeJson - mismatched braces is not json`() {
        looksLikeJson("{hello]").shouldBeFalse()
    }

    @Test
    fun `looksLikeJson - number is not json`() {
        looksLikeJson("42").shouldBeFalse()
    }
}
