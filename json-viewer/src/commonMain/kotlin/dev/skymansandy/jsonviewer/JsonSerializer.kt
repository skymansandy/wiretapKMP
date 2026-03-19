package dev.skymansandy.jsonviewer

internal fun JsonNode.toJsonString(indent: Int = 2, compact: Boolean = false): String {
    val sb = StringBuilder()
    writeNode(sb, this, currentIndent = 0, step = indent, compact = compact)
    return sb.toString()
}

internal fun JsonNode.sortKeys(ascending: Boolean = true, recursive: Boolean = true): JsonNode = when (this) {
    is JsonNode.JObject -> {
        val sorted = if (ascending) fields.sortedBy { it.first } else fields.sortedByDescending { it.first }
        val mapped = if (recursive) sorted.map { (k, v) -> k to v.sortKeys(ascending, true) } else sorted
        JsonNode.JObject(mapped)
    }
    is JsonNode.JArray -> {
        if (recursive) JsonNode.JArray(elements.map { it.sortKeys(ascending, true) }) else this
    }
    else -> this
}

private fun writeNode(sb: StringBuilder, node: JsonNode, currentIndent: Int, step: Int, compact: Boolean) {
    val nl = if (compact) "" else "\n"
    val childIndent = currentIndent + step
    val indentStr = if (compact) "" else " ".repeat(childIndent)
    val closingIndentStr = if (compact) "" else " ".repeat(currentIndent)
    val colonSep = if (compact) ":" else ": "

    when (node) {
        is JsonNode.JObject -> {
            if (node.fields.isEmpty()) {
                sb.append("{}")
            } else {
                sb.append("{").append(nl)
                node.fields.forEachIndexed { i, (key, value) ->
                    sb.append(indentStr)
                    sb.append('"').append(escapeJsonString(key)).append('"')
                    sb.append(colonSep)
                    writeNode(sb, value, childIndent, step, compact)
                    if (i < node.fields.lastIndex) sb.append(",")
                    sb.append(nl)
                }
                sb.append(closingIndentStr).append("}")
            }
        }
        is JsonNode.JArray -> {
            if (node.elements.isEmpty()) {
                sb.append("[]")
            } else {
                sb.append("[").append(nl)
                node.elements.forEachIndexed { i, element ->
                    sb.append(indentStr)
                    writeNode(sb, element, childIndent, step, compact)
                    if (i < node.elements.lastIndex) sb.append(",")
                    sb.append(nl)
                }
                sb.append(closingIndentStr).append("]")
            }
        }
        is JsonNode.JString -> {
            sb.append('"').append(escapeJsonString(node.value)).append('"')
        }
        is JsonNode.JNumber -> sb.append(node.value)
        is JsonNode.JBoolean -> sb.append(node.value)
        is JsonNode.JNull -> sb.append("null")
    }
}

private fun escapeJsonString(s: String): String = buildString(s.length) {
    for (c in s) {
        when (c) {
            '"' -> append("\\\"")
            '\\' -> append("\\\\")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            '\b' -> append("\\b")
            '\u000C' -> append("\\f")
            else -> {
                if (c.code < 0x20) {
                    append("\\u${c.code.toString(16).padStart(4, '0')}")
                } else {
                    append(c)
                }
            }
        }
    }
}
