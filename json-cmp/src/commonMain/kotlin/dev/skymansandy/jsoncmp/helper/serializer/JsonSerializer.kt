package dev.skymansandy.jsoncmp.helper.serializer

import dev.skymansandy.jsoncmp.model.JsonNode
import dev.skymansandy.jsoncmp.model.JsonPath
import dev.skymansandy.jsoncmp.model.PathSegment

internal fun JsonNode.toJsonString(indent: Int = 2, compact: Boolean = false): String {
    val sb = StringBuilder()
    writeNode(sb, this, currentIndent = 0, step = indent, compact = compact)
    return sb.toString()
}

internal fun encodePath(path: JsonPath): String =
    path.joinToString("/") {
        when (it) {
            is PathSegment.Key -> "k:${it.name}"
            is PathSegment.Index -> "i:${it.idx}"
        }
    }

private fun writeFoldedNode(
    sb: StringBuilder,
    node: JsonNode,
    foldedPaths: Set<String>,
    currentPath: JsonPath,
    currentIndent: Int,
    step: Int,
) {
    val pathKey = encodePath(currentPath)
    // If this path is folded, serialize the whole subtree compact
    if (pathKey in foldedPaths) {
        writeNode(sb, node, currentIndent = 0, step = 0, compact = true)
        return
    }

    val childIndent = currentIndent + step
    val indentStr = " ".repeat(childIndent)
    val closingIndentStr = " ".repeat(currentIndent)

    when (node) {
        is JsonNode.JObject -> {
            if (node.fields.isEmpty()) {
                sb.append("{}")
            } else {
                sb.append("{\n")
                node.fields.forEachIndexed { i, (key, value) ->
                    val childPath = currentPath + PathSegment.Key(key)
                    sb.append(indentStr)
                    sb.append('"').append(escapeJsonString(key)).append('"')
                    sb.append(": ")
                    writeFoldedNode(sb, value, foldedPaths, childPath, childIndent, step)
                    if (i < node.fields.lastIndex) sb.append(",")
                    sb.append("\n")
                }
                sb.append(closingIndentStr).append("}")
            }
        }

        is JsonNode.JArray -> {
            if (node.elements.isEmpty()) {
                sb.append("[]")
            } else {
                sb.append("[\n")
                node.elements.forEachIndexed { i, element ->
                    val childPath = currentPath + PathSegment.Index(i)
                    sb.append(indentStr)
                    writeFoldedNode(sb, element, foldedPaths, childPath, childIndent, step)
                    if (i < node.elements.lastIndex) sb.append(",")
                    sb.append("\n")
                }
                sb.append(closingIndentStr).append("]")
            }
        }

        is JsonNode.JString -> sb.append('"').append(escapeJsonString(node.value)).append('"')
        is JsonNode.JNumber -> sb.append(node.value)
        is JsonNode.JBoolean -> sb.append(node.value)
        is JsonNode.JNull -> sb.append("null")
    }
}

internal fun JsonNode.sortKeys(
    ascending: Boolean = true,
    recursive: Boolean = true,
): JsonNode = when (this) {
    is JsonNode.JObject -> {
        val sorted = when {
            ascending -> fields.sortedBy { it.first }
            else -> fields.sortedByDescending { it.first }
        }

        val mapped = when {
            recursive -> sorted.map { (k, v) -> k to v.sortKeys(ascending, true) }
            else -> sorted
        }

        JsonNode.JObject(mapped)
    }

    is JsonNode.JArray -> {
        when {
            recursive -> JsonNode.JArray(elements.map { it.sortKeys(ascending, true) })
            else -> this
        }
    }

    else -> this
}

private fun writeNode(
    sb: StringBuilder,
    node: JsonNode,
    currentIndent: Int,
    step: Int,
    compact: Boolean,
) {
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
