package dev.skymansandy.jsonviewer

internal fun parseJson(input: String): JsonNode? = try {
    val parser = JsonParser(input.trim())
    val node = parser.parseValue()
    parser.skipWs()
    if (parser.exhausted()) node else null
} catch (_: Exception) {
    null
}

internal class JsonParser(private val src: String) {
    private var pos = 0

    fun exhausted() = pos >= src.length

    fun skipWs() {
        while (pos < src.length && src[pos].isWhitespace()) pos++
    }

    fun parseValue(): JsonNode {
        skipWs()
        return when {
            cur() == '{' -> parseObject()
            cur() == '[' -> parseArray()
            cur() == '"' -> JsonNode.JString(parseStringValue())
            src.startsWith("true", pos) -> { pos += 4; JsonNode.JBoolean(true) }
            src.startsWith("false", pos) -> { pos += 5; JsonNode.JBoolean(false) }
            src.startsWith("null", pos) -> { pos += 4; JsonNode.JNull }
            cur() == '-' || cur().isDigit() -> parseNumber()
            else -> error("Unexpected '${cur()}' at pos $pos")
        }
    }

    private fun parseObject(): JsonNode.JObject {
        eat('{')
        skipWs()
        val fields = mutableListOf<Pair<String, JsonNode>>()
        while (cur() != '}') {
            skipWs()
            val key = parseStringValue()
            skipWs(); eat(':'); skipWs()
            val value = parseValue()
            fields += key to value
            skipWs()
            if (cur() == ',') { eat(','); skipWs() }
        }
        eat('}')
        return JsonNode.JObject(fields)
    }

    private fun parseArray(): JsonNode.JArray {
        eat('[')
        skipWs()
        val items = mutableListOf<JsonNode>()
        while (cur() != ']') {
            items += parseValue()
            skipWs()
            if (cur() == ',') { eat(','); skipWs() }
        }
        eat(']')
        return JsonNode.JArray(items)
    }

    private fun parseStringValue(): String {
        eat('"')
        val sb = StringBuilder()
        while (pos < src.length && cur() != '"') {
            if (cur() == '\\') {
                pos++
                when (cur()) {
                    '"', '\\', '/' -> sb.append(cur())
                    'n' -> sb.append('\n')
                    'r' -> sb.append('\r')
                    't' -> sb.append('\t')
                    'b' -> sb.append('\b')
                    'f' -> sb.append('\u000C')
                    'u' -> {
                        sb.append(src.substring(pos + 1, pos + 5).toInt(16).toChar())
                        pos += 4
                    }
                    else -> { sb.append('\\'); sb.append(cur()) }
                }
            } else {
                sb.append(cur())
            }
            pos++
        }
        eat('"')
        return sb.toString()
    }

    private fun parseNumber(): JsonNode.JNumber {
        val start = pos
        if (cur() == '-') pos++
        while (pos < src.length && cur().isDigit()) pos++
        if (pos < src.length && cur() == '.') {
            pos++
            while (pos < src.length && cur().isDigit()) pos++
        }
        if (pos < src.length && (cur() == 'e' || cur() == 'E')) {
            pos++
            if (pos < src.length && (cur() == '+' || cur() == '-')) pos++
            while (pos < src.length && cur().isDigit()) pos++
        }
        return JsonNode.JNumber(src.substring(start, pos))
    }

    private fun cur(): Char = if (pos < src.length) src[pos] else '\u0000'
    private fun eat(c: Char) { check(cur() == c) { "Expected '$c' at $pos, got '${cur()}'" }; pos++ }
}
