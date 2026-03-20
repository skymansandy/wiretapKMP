package dev.skymansandy.jsoncmp.helper.lines

import dev.skymansandy.jsoncmp.model.FoldType
import dev.skymansandy.jsoncmp.model.JsonLine
import dev.skymansandy.jsoncmp.model.JsonNode
import dev.skymansandy.jsoncmp.model.JsonPart
import dev.skymansandy.jsoncmp.model.JsonPath
import dev.skymansandy.jsoncmp.model.PathSegment

internal class JsonLineBuilder {

    private val out = mutableListOf<JsonLine>()
    private var lineNum = 0
    private var nextFoldId = 0

    fun build(root: JsonNode): List<JsonLine> {

        addNode(
            root,
            key = null,
            isLast = true,
            depth = 0,
            parentFoldIds = emptyList(),
            path = emptyList(),
        )
        return out
    }

    @Suppress("LongMethod")
    private fun addNode(
        node: JsonNode,
        key: String?,
        isLast: Boolean,
        depth: Int,
        parentFoldIds: List<Int>,
        path: JsonPath,
    ) {

        val indent: List<JsonPart> = if (depth > 0) listOf(JsonPart.Indent("  ".repeat(depth))) else emptyList()
        val keyParts: List<JsonPart> = if (key != null) {
            listOf(JsonPart.Key("\"$key\""), JsonPart.Punct(": "))
        } else emptyList()
        val comma: List<JsonPart> = if (!isLast) listOf(JsonPart.Punct(",")) else emptyList()

        when (node) {
            is JsonNode.JObject -> {
                if (node.fields.isEmpty()) {
                    out += JsonLine(
                        ++lineNum, depth, indent + keyParts + JsonPart.Punct("{}") + comma,
                        null, null, parentFoldIds, path = path,
                    )
                } else {
                    val myId = nextFoldId++
                    val headerIdx = out.size
                    out += JsonLine(
                        ++lineNum, depth, indent + keyParts + JsonPart.Punct("{"),
                        myId, FoldType.Object, parentFoldIds,
                        foldChildCount = node.fields.size, path = path,
                    )
                    val childParents = parentFoldIds + myId
                    node.fields.forEachIndexed { i, (k, v) ->
                        addNode(
                            v, k, i == node.fields.lastIndex, depth + 1,
                            childParents, path = path + PathSegment.Key(k),
                        )
                    }
                    out += JsonLine(
                        ++lineNum, depth, indent + listOf(JsonPart.Punct("}")) + comma,
                        null, null, childParents, isClosingBracket = true, path = path,
                    )
                    val foldedContent = out.subList(headerIdx + 1, out.size)
                        .joinToString(" ") { line -> line.parts.joinToString("") { it.text }.trim() }
                    out[headerIdx] = out[headerIdx].copy(foldedContent = foldedContent)
                }
            }

            is JsonNode.JArray -> {
                if (node.elements.isEmpty()) {
                    out += JsonLine(
                        ++lineNum, depth, indent + keyParts + JsonPart.Punct("[]") + comma,
                        null, null, parentFoldIds, path = path,
                    )
                } else {
                    val myId = nextFoldId++
                    val headerIdx = out.size
                    out += JsonLine(
                        ++lineNum, depth, indent + keyParts + JsonPart.Punct("["),
                        myId, FoldType.Array, parentFoldIds,
                        foldChildCount = node.elements.size, path = path,
                    )
                    val childParents = parentFoldIds + myId
                    node.elements.forEachIndexed { i, v ->
                        addNode(
                            v, null, i == node.elements.lastIndex, depth + 1,
                            childParents, path = path + PathSegment.Index(i),
                        )
                    }
                    out += JsonLine(
                        ++lineNum, depth, indent + listOf(JsonPart.Punct("]")) + comma,
                        null, null, childParents, isClosingBracket = true, path = path,
                    )
                    val foldedContent = out.subList(headerIdx + 1, out.size)
                        .joinToString(" ") { line -> line.parts.joinToString("") { it.text }.trim() }
                    out[headerIdx] = out[headerIdx].copy(foldedContent = foldedContent)
                }
            }

            is JsonNode.JString -> {
                val escaped = node.value
                    .replace("\\", "\\\\").replace("\"", "\\\"")
                    .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t")
                out += JsonLine(
                    ++lineNum, depth, indent + keyParts + JsonPart.StrVal("\"$escaped\"") + comma,
                    null, null, parentFoldIds, path = path,
                )
            }

            is JsonNode.JNumber ->
                out += JsonLine(
                    ++lineNum, depth, indent + keyParts + JsonPart.NumVal(node.value) + comma,
                    null, null, parentFoldIds, path = path,
                )

            is JsonNode.JBoolean ->
                out += JsonLine(
                    ++lineNum, depth, indent + keyParts + JsonPart.BoolVal(node.value.toString()) + comma,
                    null, null, parentFoldIds, path = path,
                )

            is JsonNode.JNull ->
                out += JsonLine(
                    ++lineNum, depth, indent + keyParts + JsonPart.NullVal("null") + comma,
                    null, null, parentFoldIds, path = path,
                )
        }
    }
}

internal fun buildDisplayLines(root: JsonNode): List<JsonLine> = JsonLineBuilder().build(root)
