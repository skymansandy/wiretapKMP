package dev.skymansandy.jsoncmp.helper.parser

import dev.skymansandy.jsoncmp.model.JsonNode

internal fun parseJsonResult(input: String): Pair<JsonNode?, JsonError?> = try {
    val parser = JsonParser(input.trim())
    val node = parser.parseValue()
    parser.skipWs()
    if (parser.exhausted()) node to null
    else null to JsonError(
        "Unexpected content after JSON at position ${parser.currentPos()}",
        parser.currentPos(),
    )
} catch (e: Exception) {
    null to JsonError(e.message ?: "Invalid JSON")
}
