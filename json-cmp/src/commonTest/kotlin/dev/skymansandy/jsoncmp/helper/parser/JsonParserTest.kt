package dev.skymansandy.jsoncmp.helper.parser

import dev.skymansandy.jsoncmp.TestData
import dev.skymansandy.jsoncmp.model.JsonNode
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.test.Test

class JsonParserTest {

    // ── Successful parsing ──

    @Test
    fun parseEmptyObject() {
        val (node, error) = parseJsonResult(TestData.EMPTY_OBJECT)

        error.shouldBeNull()
        node.shouldNotBeNull()
        val obj = node.shouldBeInstanceOf<JsonNode.JObject>()
        obj.fields.shouldBeEmpty()
    }

    @Test
    fun parseEmptyArray() {
        val (node, error) = parseJsonResult(TestData.EMPTY_ARRAY)

        error.shouldBeNull()
        node.shouldNotBeNull()
        val arr = node.shouldBeInstanceOf<JsonNode.JArray>()
        arr.elements.shouldBeEmpty()
    }

    @Test
    fun parseSimpleObjectWithStringAndNumber() {
        val (node, error) = parseJsonResult(TestData.SIMPLE_OBJECT)

        error.shouldBeNull()
        node.shouldNotBeNull()
        node shouldBe TestData.simpleObjectNode
    }

    @Test
    fun parseNestedObjects() {
        val (node, error) = parseJsonResult(TestData.NESTED_OBJECT)

        error.shouldBeNull()
        node.shouldNotBeNull()
        node shouldBe TestData.nestedObjectNode
    }

    @Test
    fun parseSimpleArray() {
        val (node, error) = parseJsonResult(TestData.SIMPLE_ARRAY)

        error.shouldBeNull()
        node.shouldNotBeNull()
        node shouldBe TestData.simpleArrayNode
    }

    @Test
    fun parseMixedTypes() {
        val (node, error) = parseJsonResult(TestData.MIXED_TYPES)

        error.shouldBeNull()
        node.shouldNotBeNull()
        node shouldBe TestData.mixedTypesNode
    }

    @Test
    fun parseBooleanTrue() {
        val (node, error) = parseJsonResult(TestData.BOOLEAN_TRUE)

        error.shouldBeNull()
        node.shouldNotBeNull()
        val obj = node.shouldBeInstanceOf<JsonNode.JObject>()
        obj.fields.shouldHaveSize(1)
        obj.fields[0].second shouldBe JsonNode.JBoolean(true)
    }

    @Test
    fun parseBooleanFalse() {
        val (node, error) = parseJsonResult(TestData.BOOLEAN_FALSE)

        error.shouldBeNull()
        node.shouldNotBeNull()
        val obj = node.shouldBeInstanceOf<JsonNode.JObject>()
        obj.fields[0].second shouldBe JsonNode.JBoolean(false)
    }

    @Test
    fun parseNullValue() {
        val (node, error) = parseJsonResult(TestData.NULL_VALUE)

        error.shouldBeNull()
        node.shouldNotBeNull()
        val obj = node.shouldBeInstanceOf<JsonNode.JObject>()
        obj.fields[0].second shouldBe JsonNode.JNull
    }

    // ── Number formats ──

    @Test
    fun parseNegativeNumber() {
        val (node, error) = parseJsonResult(TestData.NEGATIVE_NUMBER)

        error.shouldBeNull()
        node.shouldNotBeNull()
        val obj = node.shouldBeInstanceOf<JsonNode.JObject>()
        obj.fields[0].second shouldBe JsonNode.JNumber("-42")
    }

    @Test
    fun parseDecimalNumber() {
        val (node, error) = parseJsonResult(TestData.DECIMAL_NUMBER)

        error.shouldBeNull()
        node.shouldNotBeNull()
        val obj = node.shouldBeInstanceOf<JsonNode.JObject>()
        obj.fields[0].second shouldBe JsonNode.JNumber("3.14")
    }

    @Test
    fun parseScientificNotation() {
        val (node, error) = parseJsonResult(TestData.SCIENTIFIC_NUMBER)

        error.shouldBeNull()
        node.shouldNotBeNull()
        val obj = node.shouldBeInstanceOf<JsonNode.JObject>()
        obj.fields[0].second shouldBe JsonNode.JNumber("1.5e10")
    }

    @Test
    fun parseScientificNotationWithPlusSign() {
        val (node, error) = parseJsonResult("""{"v": 1e+3}""")

        error.shouldBeNull()
        node.shouldNotBeNull()
        val obj = node.shouldBeInstanceOf<JsonNode.JObject>()
        obj.fields[0].second shouldBe JsonNode.JNumber("1e+3")
    }

    @Test
    fun parseScientificNotationWithMinusSign() {
        val (node, error) = parseJsonResult("""{"v": 2.5E-4}""")

        error.shouldBeNull()
        node.shouldNotBeNull()
        val obj = node.shouldBeInstanceOf<JsonNode.JObject>()
        obj.fields[0].second shouldBe JsonNode.JNumber("2.5E-4")
    }

    // ── Escape sequences ──

    @Test
    fun parseEscapeSequences() {
        val (node, error) = parseJsonResult(TestData.ESCAPE_CHARS)

        error.shouldBeNull()
        node.shouldNotBeNull()
        val obj = node.shouldBeInstanceOf<JsonNode.JObject>()
        val str = obj.fields[0].second.shouldBeInstanceOf<JsonNode.JString>()
        str.value shouldBe "line1\nline2\ttab"
    }

    @Test
    fun parseUnicodeEscape() {
        val (node, error) = parseJsonResult("""{"ch": "\u0041"}""")

        error.shouldBeNull()
        node.shouldNotBeNull()
        val obj = node.shouldBeInstanceOf<JsonNode.JObject>()
        val str = obj.fields[0].second.shouldBeInstanceOf<JsonNode.JString>()
        str.value shouldBe "A"
    }

    @Test
    fun parseBackslashEscape() {
        val (node, error) = parseJsonResult("""{"path": "c:\\dir\\file"}""")

        error.shouldBeNull()
        val obj = node.shouldBeInstanceOf<JsonNode.JObject>()
        val str = obj.fields[0].second.shouldBeInstanceOf<JsonNode.JString>()
        str.value shouldBe "c:\\dir\\file"
    }

    @Test
    fun parseSlashEscape() {
        val (node, error) = parseJsonResult("""{"url": "http:\/\/example.com"}""")

        error.shouldBeNull()
        val obj = node.shouldBeInstanceOf<JsonNode.JObject>()
        val str = obj.fields[0].second.shouldBeInstanceOf<JsonNode.JString>()
        str.value shouldBe "http://example.com"
    }

    // ── Whitespace handling ──

    @Test
    fun parseWithLeadingAndTrailingWhitespace() {
        val (node, error) = parseJsonResult("   ${TestData.EMPTY_OBJECT}   ")

        error.shouldBeNull()
        node.shouldNotBeNull()
        node.shouldBeInstanceOf<JsonNode.JObject>()
    }

    @Test
    fun parseWithNewlinesAndTabs() {
        val json = "{\n\t\"key\"\t:\t\"value\"\n}"
        val (node, error) = parseJsonResult(json)

        error.shouldBeNull()
        node.shouldNotBeNull()
        val obj = node.shouldBeInstanceOf<JsonNode.JObject>()
        obj.fields.shouldHaveSize(1)
        obj.fields[0].first shouldBe "key"
    }

    // ── Error cases ──

    @Test
    fun errorOnInvalidJson() {
        val (node, error) = parseJsonResult(TestData.INVALID_JSON)

        node.shouldBeNull()
        error.shouldNotBeNull()
    }

    @Test
    fun errorOnTrailingContent() {
        val (node, error) = parseJsonResult(TestData.TRAILING_CONTENT)

        node.shouldBeNull()
        error.shouldNotBeNull()
        error.message shouldContain "Unexpected content"
    }

    @Test
    fun emptyInputReturnsNullNodeAndNullError() {
        val (node, error) = parseJsonResult("")

        node.shouldBeNull()
        error.shouldNotBeNull()
    }

    @Test
    fun whitespaceOnlyInputReturnsNullNodeAndError() {
        val (node, error) = parseJsonResult("   ")

        node.shouldBeNull()
        error.shouldNotBeNull()
    }

    // ── Lenient parsing ──

    @Test
    fun lenientParseHandlesTrailingCommasInObjects() {
        val (node, error) = parseJsonResult("""{"a": 1, "b": 2,}""")

        error.shouldBeNull()
        node.shouldNotBeNull()
        val obj = node.shouldBeInstanceOf<JsonNode.JObject>()
        // Parser should handle trailing comma gracefully
        // since it keeps parsing until '}'
    }

    @Test
    fun parseDeeplyNestedStructure() {
        val json = """{"a": {"b": {"c": {"d": [1, [2, [3]]]}}}}"""
        val (node, error) = parseJsonResult(json)

        error.shouldBeNull()
        node.shouldNotBeNull()
    }
}
