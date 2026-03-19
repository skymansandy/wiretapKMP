package dev.skymansandy.jsonviewer

sealed class JsonNode {
    data class JObject(val fields: List<Pair<String, JsonNode>>) : JsonNode()
    data class JArray(val elements: List<JsonNode>) : JsonNode()
    data class JString(val value: String) : JsonNode()
    data class JNumber(val value: String) : JsonNode()
    data class JBoolean(val value: Boolean) : JsonNode()
    data object JNull : JsonNode()
}
