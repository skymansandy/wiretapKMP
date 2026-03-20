package dev.skymansandy.jsoncmp

import dev.skymansandy.jsoncmp.model.JsonNode

internal object TestData {

    const val EMPTY_OBJECT = "{}"
    const val EMPTY_ARRAY = "[]"
    const val SIMPLE_OBJECT = """{"name": "John", "age": 30}"""
    const val NESTED_OBJECT = """{"user": {"name": "John", "address": {"city": "NYC"}}}"""
    const val SIMPLE_ARRAY = """[1, 2, 3]"""
    const val MIXED_TYPES = """{"str": "hello", "num": 42, "bool": true, "nil": null, "arr": [1], "obj": {}}"""
    const val ESCAPE_CHARS = """{"msg": "line1\nline2\ttab"}"""
    const val SCIENTIFIC_NUMBER = """{"val": 1.5e10}"""
    const val NEGATIVE_NUMBER = """{"val": -42}"""
    const val DECIMAL_NUMBER = """{"val": 3.14}"""
    const val INVALID_JSON = """{invalid}"""
    const val TRAILING_CONTENT = """{"a": 1} extra"""
    const val UNSORTED_KEYS = """{"c": 3, "a": 1, "b": 2}"""
    const val BOOLEAN_TRUE = """{"flag": true}"""
    const val BOOLEAN_FALSE = """{"flag": false}"""
    const val NULL_VALUE = """{"val": null}"""

    val BEAUTIFIED_SIMPLE_OBJECT = """
{
  "name": "John",
  "age": 30
}
    """.trimIndent()

    val simpleObjectNode = JsonNode.JObject(
        listOf(
            "name" to JsonNode.JString("John"),
            "age" to JsonNode.JNumber("30"),
        ),
    )

    val nestedObjectNode = JsonNode.JObject(
        listOf(
            "user" to JsonNode.JObject(
                listOf(
                    "name" to JsonNode.JString("John"),
                    "address" to JsonNode.JObject(
                        listOf("city" to JsonNode.JString("NYC")),
                    ),
                ),
            ),
        ),
    )

    val unsortedNode = JsonNode.JObject(
        listOf(
            "c" to JsonNode.JNumber("3"),
            "a" to JsonNode.JNumber("1"),
            "b" to JsonNode.JNumber("2"),
        ),
    )

    val simpleArrayNode = JsonNode.JArray(
        listOf(
            JsonNode.JNumber("1"),
            JsonNode.JNumber("2"),
            JsonNode.JNumber("3"),
        ),
    )

    val mixedTypesNode = JsonNode.JObject(
        listOf(
            "str" to JsonNode.JString("hello"),
            "num" to JsonNode.JNumber("42"),
            "bool" to JsonNode.JBoolean(true),
            "nil" to JsonNode.JNull,
            "arr" to JsonNode.JArray(listOf(JsonNode.JNumber("1"))),
            "obj" to JsonNode.JObject(emptyList()),
        ),
    )
}
