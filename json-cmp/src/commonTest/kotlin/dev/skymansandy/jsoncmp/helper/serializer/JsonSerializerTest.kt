package dev.skymansandy.jsoncmp.helper.serializer

import dev.skymansandy.jsoncmp.TestData
import dev.skymansandy.jsoncmp.model.JsonNode
import dev.skymansandy.jsoncmp.model.PathSegment
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotContain
import kotlin.test.Test

class JsonSerializerTest {

    // ── toJsonString ──

    @Test
    fun serializeSimpleObjectPrettyPrinted() {
        TestData.simpleObjectNode.toJsonString() shouldBe TestData.BEAUTIFIED_SIMPLE_OBJECT
    }

    @Test
    fun serializeSimpleObjectCompact() {
        TestData.simpleObjectNode.toJsonString(compact = true) shouldBe """{"name":"John","age":30}"""
    }

    @Test
    fun serializeEmptyObject() {
        JsonNode.JObject(emptyList()).toJsonString() shouldBe "{}"
    }

    @Test
    fun serializeEmptyArray() {
        JsonNode.JArray(emptyList()).toJsonString() shouldBe "[]"
    }

    @Test
    fun serializeSimpleArrayPretty() {
        val expected = """
[
  1,
  2,
  3
]
        """.trimIndent()

        TestData.simpleArrayNode.toJsonString() shouldBe expected
    }

    @Test
    fun serializeSimpleArrayCompact() {
        TestData.simpleArrayNode.toJsonString(compact = true) shouldBe "[1,2,3]"
    }

    @Test
    fun serializeNestedObjectPretty() {
        val result = TestData.nestedObjectNode.toJsonString()

        result shouldBe """
{
  "user": {
    "name": "John",
    "address": {
      "city": "NYC"
    }
  }
}
        """.trimIndent()
    }

    @Test
    fun serializeWithCustomIndent() {
        val result = TestData.simpleObjectNode.toJsonString(indent = 4)

        result shouldBe """
{
    "name": "John",
    "age": 30
}
        """.trimIndent()
    }

    @Test
    fun serializeStringWithSpecialCharactersEscapesProperly() {
        val node = JsonNode.JObject(
            listOf("msg" to JsonNode.JString("line1\nline2\ttab\"quoted\\")),
        )
        val result = node.toJsonString(compact = true)

        result shouldBe """{"msg":"line1\nline2\ttab\"quoted\\"}"""
    }

    @Test
    fun serializeAllPrimitiveTypes() {
        val result = TestData.mixedTypesNode.toJsonString(compact = true)

        result shouldBe """{"str":"hello","num":42,"bool":true,"nil":null,"arr":[1],"obj":{}}"""
    }

    @Test
    fun serializeBooleanValues() {
        JsonNode.JBoolean(true).toJsonString() shouldBe "true"
        JsonNode.JBoolean(false).toJsonString() shouldBe "false"
    }

    @Test
    fun serializeNull() {
        JsonNode.JNull.toJsonString() shouldBe "null"
    }

    @Test
    fun serializeString() {
        JsonNode.JString("hello").toJsonString() shouldBe "\"hello\""
    }

    @Test
    fun serializeNumber() {
        JsonNode.JNumber("42").toJsonString() shouldBe "42"
    }

    @Test
    fun compactModeHasNoWhitespace() {
        val result = TestData.nestedObjectNode.toJsonString(compact = true)

        result shouldNotContain "\n"
        result shouldNotContain "  "
    }

    // ── sortKeys ──

    @Test
    fun sortKeysAscending() {
        val sorted = TestData.unsortedNode.sortKeys(ascending = true)
        val obj = sorted as JsonNode.JObject

        obj.fields.map { it.first } shouldBe listOf("a", "b", "c")
    }

    @Test
    fun sortKeysDescending() {
        val sorted = TestData.unsortedNode.sortKeys(ascending = false)
        val obj = sorted as JsonNode.JObject

        obj.fields.map { it.first } shouldBe listOf("c", "b", "a")
    }

    @Test
    fun sortKeysRecursively() {
        val node = JsonNode.JObject(
            listOf(
                "z" to JsonNode.JObject(
                    listOf(
                        "b" to JsonNode.JNumber("2"),
                        "a" to JsonNode.JNumber("1"),
                    ),
                ),
                "a" to JsonNode.JNumber("0"),
            ),
        )

        val sorted = node.sortKeys(ascending = true, recursive = true)
        val obj = sorted as JsonNode.JObject

        obj.fields.map { it.first } shouldBe listOf("a", "z")
        val nestedObj = obj.fields[1].second as JsonNode.JObject
        nestedObj.fields.map { it.first } shouldBe listOf("a", "b")
    }

    @Test
    fun sortKeysNonRecursively() {
        val node = JsonNode.JObject(
            listOf(
                "z" to JsonNode.JObject(
                    listOf(
                        "b" to JsonNode.JNumber("2"),
                        "a" to JsonNode.JNumber("1"),
                    ),
                ),
                "a" to JsonNode.JNumber("0"),
            ),
        )

        val sorted = node.sortKeys(ascending = true, recursive = false)
        val obj = sorted as JsonNode.JObject

        obj.fields.map { it.first } shouldBe listOf("a", "z")
        val nestedObj = obj.fields[1].second as JsonNode.JObject
        nestedObj.fields.map { it.first } shouldBe listOf("b", "a")
    }

    @Test
    fun sortKeysInArrayElementsRecursively() {
        val node = JsonNode.JArray(
            listOf(
                JsonNode.JObject(
                    listOf(
                        "b" to JsonNode.JNumber("2"),
                        "a" to JsonNode.JNumber("1"),
                    ),
                ),
            ),
        )

        val sorted = node.sortKeys(ascending = true, recursive = true) as JsonNode.JArray
        val innerObj = sorted.elements[0] as JsonNode.JObject

        innerObj.fields.map { it.first } shouldBe listOf("a", "b")
    }

    @Test
    fun sortKeysPreservesPrimitives() {
        JsonNode.JString("hello").sortKeys() shouldBe JsonNode.JString("hello")
        JsonNode.JNumber("42").sortKeys() shouldBe JsonNode.JNumber("42")
        JsonNode.JBoolean(true).sortKeys() shouldBe JsonNode.JBoolean(true)
        JsonNode.JNull.sortKeys() shouldBe JsonNode.JNull
    }

    // ── encodePath ──

    @Test
    fun encodeEmptyPath() {
        encodePath(emptyList()) shouldBe ""
    }

    @Test
    fun encodePathWithSingleKey() {
        encodePath(listOf(PathSegment.Key("name"))) shouldBe "k:name"
    }

    @Test
    fun encodePathWithSingleIndex() {
        encodePath(listOf(PathSegment.Index(0))) shouldBe "i:0"
    }

    @Test
    fun encodeMixedPath() {
        val path = listOf(
            PathSegment.Key("users"),
            PathSegment.Index(0),
            PathSegment.Key("name"),
        )

        encodePath(path) shouldBe "k:users/i:0/k:name"
    }
}
