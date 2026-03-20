package dev.skymansandy.jsoncmp.component.common

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.skymansandy.jsoncmp.helper.constants.cellVerticalPadding
import dev.skymansandy.jsoncmp.helper.constants.colors.JsonCmpColors
import dev.skymansandy.jsoncmp.helper.constants.colors.partColor
import dev.skymansandy.jsoncmp.helper.constants.typography.monoStyle
import dev.skymansandy.jsoncmp.model.FoldType
import dev.skymansandy.jsoncmp.model.JsonLine
import dev.skymansandy.jsoncmp.model.JsonPart

@Composable
internal fun ContentCell(
    line: JsonLine,
    isFolded: Boolean,
    searchQuery: String,
    colors: JsonCmpColors,
    onFoldToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lineText = buildString {
        line.parts.forEach { append(it.text) }
    }

    if (isFolded && line.foldedContent.isNotEmpty()) {
        // Folded: show "{ ... }" as visible text.
        // Tap → expand fold. Long-press → copy full JSON to clipboard.
        @Suppress("DEPRECATION")
        val clipboardManager = LocalClipboardManager.current
        val fullJson = lineText + " " + line.foldedContent
        val closingBracket = if (line.foldType == FoldType.Object) "}" else "]"

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
                        onLongPress = {
                            clipboardManager.setText(AnnotatedString(fullJson))
                        },
                    )
                }
                .padding(
                    start = 8.dp,
                    end = 16.dp,
                    top = cellVerticalPadding,
                    bottom = cellVerticalPadding,
                ),
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
                .padding(
                    start = 8.dp,
                    end = 16.dp,
                    top = cellVerticalPadding,
                    bottom = cellVerticalPadding,
                ),
        )
    }
}

// ── Previews ──

private val previewColors = JsonCmpColors.Dark

private val previewLine = JsonLine(
    lineNumber = 2,
    depth = 1,
    parts = listOf(
        JsonPart.Indent("    "),
        JsonPart.Key("\"name\""),
        JsonPart.Punct(": "),
        JsonPart.StrVal("\"John Doe\""),
        JsonPart.Punct(","),
    ),
    foldId = null,
    foldType = null,
    parentFoldIds = emptyList(),
)

private val previewFoldableLine = JsonLine(
    lineNumber = 5,
    depth = 1,
    parts = listOf(
        JsonPart.Indent("    "),
        JsonPart.Key("\"address\""),
        JsonPart.Punct(": "),
        JsonPart.Punct("{"),
    ),
    foldId = 1,
    foldType = FoldType.Object,
    parentFoldIds = emptyList(),
    foldChildCount = 3,
    foldedContent = "\"street\": \"123 Main St\", \"city\": \"New York\" }",
)

@Preview
@Composable
private fun Preview_ContentCell() {
    MaterialTheme {
        ContentCell(
            line = previewLine,
            isFolded = false,
            searchQuery = "",
            colors = previewColors,
            onFoldToggle = {},
        )
    }
}

@Preview
@Composable
private fun Preview_ContentCellFolded() {
    MaterialTheme {
        ContentCell(
            line = previewFoldableLine,
            isFolded = true,
            searchQuery = "",
            colors = previewColors,
            onFoldToggle = {},
        )
    }
}

@Preview
@Composable
private fun Preview_ContentCellWithSearch() {
    MaterialTheme {
        ContentCell(
            line = previewLine,
            isFolded = false,
            searchQuery = "John",
            colors = previewColors,
            onFoldToggle = {},
        )
    }
}
