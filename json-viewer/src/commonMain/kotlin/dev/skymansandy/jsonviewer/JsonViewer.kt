package dev.skymansandy.jsonviewer

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─── Public API ────────────────────────────────────────────────────────────────

data class JsonViewerColors(
    val key: Color,
    val string: Color,
    val number: Color,
    val booleanColor: Color,
    val nullColor: Color,
    val punctuation: Color,
    val lineNumber: Color,
    val foldHint: Color,
    val background: Color,
    val gutterBackground: Color,
    val highlight: Color,
    val highlightFg: Color,
)

val defaultJsonViewerColors = JsonViewerColors(
    key = Color(0xFF9CDCFE),
    string = Color(0xFFCE9178),
    number = Color(0xFFB5CEA8),
    booleanColor = Color(0xFF569CD6),
    nullColor = Color(0xFF808080),
    punctuation = Color(0xFFD4D4D4),
    lineNumber = Color(0xFF858585),
    foldHint = Color(0xFF858585),
    background = Color(0xFF1E1E1E),
    gutterBackground = Color(0xFF252526),
    highlight = Color(0xFFFFEB3B),
    highlightFg = Color(0xFF1E1E1E),
)

@Composable
fun JsonViewer(
    json: String,
    modifier: Modifier = Modifier,
    searchQuery: String = "",
    colors: JsonViewerColors = defaultJsonViewerColors,
) {
    val root = remember(json) { parseJson(json.trim()) }

    if (root == null) {
        PlainTextFallback(text = json, searchQuery = searchQuery, colors = colors, modifier = modifier)
        return
    }

    val allLines = remember(root) { buildDisplayLines(root) }
    val foldState = remember { mutableStateMapOf<Int, Boolean>() }
    val visibleLines by remember(allLines) {
        derivedStateOf {
            allLines.filter { line -> line.parentFoldIds.none { foldState[it] == true } }
        }
    }
    val numDigits = remember(allLines) { allLines.size.toString().length }

    SelectionContainer(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth()) {
            for (line in visibleLines) {
                val isFolded = line.foldId != null && foldState[line.foldId] == true
                Row(Modifier.fillMaxWidth()) {
                    // Gutter: background scoped to this Row's child, not the outer Column,
                    // so no indentation from content ever affects its width or background.
                    GutterCell(
                        line = line,
                        isFolded = isFolded,
                        numDigits = numDigits,
                        colors = colors,
                        onFoldToggle = {
                            line.foldId?.let { id -> foldState[id] = !(foldState[id] ?: false) }
                        },
                    )
                    ContentCell(
                        line = line,
                        isFolded = isFolded,
                        searchQuery = searchQuery,
                        colors = colors,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

// ─── Internal line model ───────────────────────────────────────────────────────

internal data class JsonLine(
    val lineNumber: Int,
    val depth: Int,
    val parts: List<JsonPart>,
    val foldId: Int?,
    val foldType: FoldType?,
    val parentFoldIds: List<Int>,
)

internal enum class FoldType { OBJECT, ARRAY }

internal sealed class JsonPart {
    abstract val text: String
    data class Key(override val text: String) : JsonPart()
    data class StrVal(override val text: String) : JsonPart()
    data class NumVal(override val text: String) : JsonPart()
    data class BoolVal(override val text: String) : JsonPart()
    data class NullVal(override val text: String) : JsonPart()
    data class Punct(override val text: String) : JsonPart()
}

// ─── Display line builder ──────────────────────────────────────────────────────

internal fun buildDisplayLines(root: JsonNode): List<JsonLine> = JsonLineBuilder().build(root)

private class JsonLineBuilder {
    private val out = mutableListOf<JsonLine>()
    private var lineNum = 0
    private var nextFoldId = 0

    fun build(root: JsonNode): List<JsonLine> {
        addNode(root, key = null, isLast = true, depth = 0, parentFoldIds = emptyList())
        return out
    }

    private fun addNode(
        node: JsonNode,
        key: String?,
        isLast: Boolean,
        depth: Int,
        parentFoldIds: List<Int>,
    ) {
        val keyParts: List<JsonPart> = if (key != null) {
            listOf(JsonPart.Key("\"$key\""), JsonPart.Punct(": "))
        } else emptyList()
        val comma: List<JsonPart> = if (!isLast) listOf(JsonPart.Punct(",")) else emptyList()

        when (node) {
            is JsonNode.JObject -> {
                if (node.fields.isEmpty()) {
                    out += JsonLine(++lineNum, depth, keyParts + JsonPart.Punct("{}") + comma, null, null, parentFoldIds)
                } else {
                    val myId = nextFoldId++
                    out += JsonLine(++lineNum, depth, keyParts + JsonPart.Punct("{"), myId, FoldType.OBJECT, parentFoldIds)
                    val childParents = parentFoldIds + myId
                    node.fields.forEachIndexed { i, (k, v) ->
                        addNode(v, k, i == node.fields.lastIndex, depth + 1, childParents)
                    }
                    out += JsonLine(++lineNum, depth, listOf(JsonPart.Punct("}")) + comma, null, null, childParents)
                }
            }

            is JsonNode.JArray -> {
                if (node.elements.isEmpty()) {
                    out += JsonLine(++lineNum, depth, keyParts + JsonPart.Punct("[]") + comma, null, null, parentFoldIds)
                } else {
                    val myId = nextFoldId++
                    out += JsonLine(++lineNum, depth, keyParts + JsonPart.Punct("["), myId, FoldType.ARRAY, parentFoldIds)
                    val childParents = parentFoldIds + myId
                    node.elements.forEachIndexed { i, v ->
                        addNode(v, null, i == node.elements.lastIndex, depth + 1, childParents)
                    }
                    out += JsonLine(++lineNum, depth, listOf(JsonPart.Punct("]")) + comma, null, null, childParents)
                }
            }

            is JsonNode.JString -> {
                val escaped = node.value
                    .replace("\\", "\\\\").replace("\"", "\\\"")
                    .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t")
                out += JsonLine(++lineNum, depth, keyParts + JsonPart.StrVal("\"$escaped\"") + comma, null, null, parentFoldIds)
            }

            is JsonNode.JNumber ->
                out += JsonLine(++lineNum, depth, keyParts + JsonPart.NumVal(node.value) + comma, null, null, parentFoldIds)

            is JsonNode.JBoolean ->
                out += JsonLine(++lineNum, depth, keyParts + JsonPart.BoolVal(node.value.toString()) + comma, null, null, parentFoldIds)

            is JsonNode.JNull ->
                out += JsonLine(++lineNum, depth, keyParts + JsonPart.NullVal("null") + comma, null, null, parentFoldIds)
        }
    }
}

// ─── Cell rendering ────────────────────────────────────────────────────────────

// Uniform vertical padding applied to both gutter and content cells so heights always match.
private val cellVerticalPadding = 3.dp

@Composable
private fun GutterCell(
    line: JsonLine,
    isFolded: Boolean,
    numDigits: Int,
    colors: JsonViewerColors,
    onFoldToggle: () -> Unit,
) {
    val foldGlyph = when {
        line.foldId == null -> " "
        isFolded -> "▶"
        else -> "▼"
    }
    Row(
        modifier = Modifier.background(colors.gutterBackground),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = line.lineNumber.toString().padStart(numDigits),
            style = monoStyle,
            color = colors.lineNumber,
            modifier = Modifier.padding(start = 12.dp, end = 6.dp, top = cellVerticalPadding, bottom = cellVerticalPadding),
        )
        Text(
            text = foldGlyph,
            style = monoStyle,
            color = colors.foldHint,
            modifier = (if (line.foldId != null) {
                Modifier.pointerInput(line.foldId) { detectTapGestures { onFoldToggle() } }
            } else Modifier).padding(end = 10.dp, top = cellVerticalPadding, bottom = cellVerticalPadding),
        )
    }
}

@Composable
private fun ContentCell(
    line: JsonLine,
    isFolded: Boolean,
    searchQuery: String,
    colors: JsonViewerColors,
    modifier: Modifier = Modifier,
) {
    // Pre-build plain text — necessary to get correct indices for addStyle calls.
    // Using toString() on AnnotatedString.Builder is unreliable; buildString is safe.
    val lineText = buildString {
        line.parts.forEach { append(it.text) }
        if (isFolded) append(if (line.foldType == FoldType.OBJECT) " … }" else " … ]")
    }

    val styledText = buildAnnotatedString {
        append(lineText)

        // Syntax coloring — tracked cursor keeps offsets correct
        var cursor = 0
        line.parts.forEach { part ->
            val color = when (part) {
                is JsonPart.Key -> colors.key
                is JsonPart.StrVal -> colors.string
                is JsonPart.NumVal -> colors.number
                is JsonPart.BoolVal -> colors.booleanColor
                is JsonPart.NullVal -> colors.nullColor
                is JsonPart.Punct -> colors.punctuation
            }
            addStyle(SpanStyle(color = color), cursor, cursor + part.text.length)
            cursor += part.text.length
        }
        if (isFolded) {
            addStyle(SpanStyle(color = colors.foldHint), cursor, lineText.length)
        }

        // Search highlights applied last so they overlay syntax colors
        if (searchQuery.isNotBlank()) {
            val lower = lineText.lowercase()
            val queryLower = searchQuery.lowercase()
            var idx = lower.indexOf(queryLower)
            while (idx >= 0) {
                addStyle(
                    SpanStyle(background = colors.highlight, color = colors.highlightFg),
                    start = idx,
                    end = idx + queryLower.length,
                )
                idx = lower.indexOf(queryLower, idx + queryLower.length)
            }
        }
    }

    Text(
        text = styledText,
        style = monoStyle,
        softWrap = false,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .background(colors.background)
            .padding(
                start = (line.depth * 16).dp + 8.dp,
                end = 16.dp,
                top = cellVerticalPadding,
                bottom = cellVerticalPadding,
            ),
    )
}

@Composable
private fun PlainTextFallback(
    text: String,
    searchQuery: String,
    colors: JsonViewerColors,
    modifier: Modifier,
) {
    val annotated = buildAnnotatedString {
        append(text)
        addStyle(SpanStyle(color = colors.punctuation), 0, text.length)
        if (searchQuery.isNotBlank()) {
            val lowerText = text.lowercase()
            val lowerQuery = searchQuery.lowercase()
            var idx = lowerText.indexOf(lowerQuery)
            while (idx >= 0) {
                addStyle(SpanStyle(background = colors.highlight, color = colors.highlightFg), idx, idx + lowerQuery.length)
                idx = lowerText.indexOf(lowerQuery, idx + lowerQuery.length)
            }
        }
    }
    SelectionContainer(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.background),
    ) {
        Text(
            text = annotated,
            style = monoStyle,
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(12.dp),
        )
    }
}

// Explicit lineHeight locks all cells to the same height regardless of glyph metrics
// (e.g. unicode ▶/▼ vs ASCII characters have different font ascent/descent values).
private val monoStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp, lineHeight = 18.sp)
