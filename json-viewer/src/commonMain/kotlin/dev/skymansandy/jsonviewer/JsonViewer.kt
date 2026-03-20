package dev.skymansandy.jsonviewer

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
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
    val gutterBorder: Color = Color(0xFF3C3C3C),
    val foldEllipsis: Color = Color(0xFFC586C0),
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
    gutterBorder = Color(0xFF3C3C3C),
    foldEllipsis = Color(0xFFC586C0),
)

// ─── Path model ───────────────────────────────────────────────────────────────

sealed class PathSegment {
    data class Key(val name: String) : PathSegment()
    data class Index(val idx: Int) : PathSegment()
}

typealias JsonPath = List<PathSegment>

// ─── Internal line model ───────────────────────────────────────────────────────

internal data class JsonLine(
    val lineNumber: Int,
    val depth: Int,
    val parts: List<JsonPart>,
    val foldId: Int?,
    val foldType: FoldType?,
    val parentFoldIds: List<Int>,
    val foldChildCount: Int = 0,
    /** Inline text of all lines inside this fold (children + closing bracket),
     *  joined without newlines. Appended as transparent text when folded so
     *  copy/paste captures the real JSON content. */
    val foldedContent: String = "",
    /** Path from root to the node this line represents. Empty for closing brackets. */
    val path: JsonPath = emptyList(),
    /** Whether this line is a closing bracket (not a value node). */
    val isClosingBracket: Boolean = false,
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
    data class Indent(override val text: String) : JsonPart()
}

// ─── Display line builder ──────────────────────────────────────────────────────

internal fun buildDisplayLines(root: JsonNode): List<JsonLine> = JsonLineBuilder().build(root)

private class JsonLineBuilder {
    private val out = mutableListOf<JsonLine>()
    private var lineNum = 0
    private var nextFoldId = 0

    fun build(root: JsonNode): List<JsonLine> {
        addNode(root, key = null, isLast = true, depth = 0, parentFoldIds = emptyList(), path = emptyList())
        return out
    }

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
                    out += JsonLine(++lineNum, depth, indent + keyParts + JsonPart.Punct("{}") + comma, null, null, parentFoldIds, path = path)
                } else {
                    val myId = nextFoldId++
                    val headerIdx = out.size
                    out += JsonLine(++lineNum, depth, indent + keyParts + JsonPart.Punct("{"), myId, FoldType.OBJECT, parentFoldIds, foldChildCount = node.fields.size, path = path)
                    val childParents = parentFoldIds + myId
                    node.fields.forEachIndexed { i, (k, v) ->
                        addNode(v, k, i == node.fields.lastIndex, depth + 1, childParents, path = path + PathSegment.Key(k))
                    }
                    out += JsonLine(++lineNum, depth, indent + listOf(JsonPart.Punct("}")) + comma, null, null, childParents, isClosingBracket = true, path = path)
                    val foldedContent = out.subList(headerIdx + 1, out.size)
                        .joinToString(" ") { line -> line.parts.joinToString("") { it.text }.trim() }
                    out[headerIdx] = out[headerIdx].copy(foldedContent = foldedContent)
                }
            }

            is JsonNode.JArray -> {
                if (node.elements.isEmpty()) {
                    out += JsonLine(++lineNum, depth, indent + keyParts + JsonPart.Punct("[]") + comma, null, null, parentFoldIds, path = path)
                } else {
                    val myId = nextFoldId++
                    val headerIdx = out.size
                    out += JsonLine(++lineNum, depth, indent + keyParts + JsonPart.Punct("["), myId, FoldType.ARRAY, parentFoldIds, foldChildCount = node.elements.size, path = path)
                    val childParents = parentFoldIds + myId
                    node.elements.forEachIndexed { i, v ->
                        addNode(v, null, i == node.elements.lastIndex, depth + 1, childParents, path = path + PathSegment.Index(i))
                    }
                    out += JsonLine(++lineNum, depth, indent + listOf(JsonPart.Punct("]")) + comma, null, null, childParents, isClosingBracket = true, path = path)
                    val foldedContent = out.subList(headerIdx + 1, out.size)
                        .joinToString(" ") { line -> line.parts.joinToString("") { it.text }.trim() }
                    out[headerIdx] = out[headerIdx].copy(foldedContent = foldedContent)
                }
            }

            is JsonNode.JString -> {
                val escaped = node.value
                    .replace("\\", "\\\\").replace("\"", "\\\"")
                    .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t")
                out += JsonLine(++lineNum, depth, indent + keyParts + JsonPart.StrVal("\"$escaped\"") + comma, null, null, parentFoldIds, path = path)
            }

            is JsonNode.JNumber ->
                out += JsonLine(++lineNum, depth, indent + keyParts + JsonPart.NumVal(node.value) + comma, null, null, parentFoldIds, path = path)

            is JsonNode.JBoolean ->
                out += JsonLine(++lineNum, depth, indent + keyParts + JsonPart.BoolVal(node.value.toString()) + comma, null, null, parentFoldIds, path = path)

            is JsonNode.JNull ->
                out += JsonLine(++lineNum, depth, indent + keyParts + JsonPart.NullVal("null") + comma, null, null, parentFoldIds, path = path)
        }
    }
}

// ─── Cell rendering ────────────────────────────────────────────────────────────

// Uniform vertical padding applied to both gutter and content cells so heights always match.
internal val cellVerticalPadding = 3.dp

// Fixed width for the fold glyph column so ▶/▼ all occupy the same space.
internal val foldGlyphSize = 14.dp

@Composable
internal fun GutterCell(
    line: JsonLine,
    isFolded: Boolean,
    numDigits: Int,
    colors: JsonViewerColors,
    onFoldToggle: () -> Unit,
) {
    val foldGlyph = when {
        line.foldId == null -> ""
        isFolded -> "▶"
        else -> "▼"
    }
    val borderColor = colors.gutterBorder
    Row(
        modifier = Modifier
            .background(colors.gutterBackground)
            .drawBehind {
                val x = size.width
                drawLine(borderColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1.dp.toPx())
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = line.lineNumber.toString().padStart(numDigits),
            style = monoStyle,
            color = colors.lineNumber,
            modifier = Modifier.padding(start = 12.dp, end = 6.dp, top = cellVerticalPadding, bottom = cellVerticalPadding),
        )
        Box(
            modifier = Modifier
                .size(foldGlyphSize)
                .then(
                    if (line.foldId != null) {
                        Modifier.pointerInput(line.foldId) { detectTapGestures { onFoldToggle() } }
                    } else Modifier,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (foldGlyph.isNotEmpty()) {
                Text(
                    text = foldGlyph,
                    style = monoStyle,
                    color = colors.foldHint,
                )
            }
        }
    }
}

@Composable
internal fun ContentCell(
    line: JsonLine,
    isFolded: Boolean,
    searchQuery: String,
    colors: JsonViewerColors,
    onFoldToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lineText = buildString {
        line.parts.forEach { append(it.text) }
    }

    if (isFolded && line.foldedContent.isNotEmpty()) {
        // Folded: show "{ ... }" as visible text.
        // Tap → expand fold. Long-press → copy full JSON to clipboard.
        val clipboardManager = LocalClipboardManager.current
        val fullJson = lineText + " " + line.foldedContent
        val closingBracket = if (line.foldType == FoldType.OBJECT) "}" else "]"

        val styledText = buildAnnotatedString {
            var cursor = 0
            line.parts.forEach { part ->
                append(part.text)
                addStyle(SpanStyle(color = partColor(part, colors)), cursor, cursor + part.text.length)
                cursor += part.text.length
            }
            val ellipsisStart = length
            append(" ... ")
            addStyle(SpanStyle(color = colors.foldEllipsis), ellipsisStart, length)
            val bracketStart = length
            append(closingBracket)
            addStyle(SpanStyle(color = colors.punctuation), bracketStart, length)

            if (searchQuery.isNotBlank()) {
                val queryLower = searchQuery.lowercase()
                // Highlight visible line text matches (e.g. key name)
                val displayText = toAnnotatedString().text
                val lower = displayText.lowercase()
                var idx = lower.indexOf(queryLower)
                while (idx >= 0) {
                    addStyle(
                        SpanStyle(background = colors.highlight, color = colors.highlightFg),
                        start = idx,
                        end = idx + queryLower.length,
                    )
                    idx = lower.indexOf(queryLower, idx + queryLower.length)
                }
                // If the search matches anything inside the collapsed content,
                // highlight the entire " ... <bracket>" region to signal hidden matches.
                if (line.foldedContent.lowercase().contains(queryLower)) {
                    addStyle(
                        SpanStyle(background = colors.highlight, color = colors.highlightFg),
                        start = ellipsisStart,
                        end = length,
                    )
                }
            }
        }

        Text(
            text = styledText,
            style = monoStyle,
            softWrap = false,
            overflow = TextOverflow.Clip,
            modifier = modifier
                .background(colors.background)
                .pointerInput(line.foldId) {
                    detectTapGestures(
                        onTap = { onFoldToggle() },
                        onLongPress = { clipboardManager.setText(AnnotatedString(fullJson)) },
                    )
                }
                .padding(start = 8.dp, end = 16.dp, top = cellVerticalPadding, bottom = cellVerticalPadding),
        )
    } else {
        val styledText = buildAnnotatedString {
            append(lineText)
            var cursor = 0
            line.parts.forEach { part ->
                addStyle(SpanStyle(color = partColor(part, colors)), cursor, cursor + part.text.length)
                cursor += part.text.length
            }
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
            overflow = TextOverflow.Clip,
            modifier = modifier
                .background(colors.background)
                .padding(start = 8.dp, end = 16.dp, top = cellVerticalPadding, bottom = cellVerticalPadding),
        )
    }
}

internal fun partColor(part: JsonPart, colors: JsonViewerColors): Color = when (part) {
    is JsonPart.Key -> colors.key
    is JsonPart.StrVal -> colors.string
    is JsonPart.NumVal -> colors.number
    is JsonPart.BoolVal -> colors.booleanColor
    is JsonPart.NullVal -> colors.nullColor
    is JsonPart.Punct -> colors.punctuation
    is JsonPart.Indent -> colors.punctuation
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
internal val monoStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp, lineHeight = 18.sp)
