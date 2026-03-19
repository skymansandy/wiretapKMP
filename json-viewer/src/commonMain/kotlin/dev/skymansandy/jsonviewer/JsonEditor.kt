package dev.skymansandy.jsonviewer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// ─── Public API ──────────────────────────────────────────────────────────────────

@Composable
fun JsonEditor(
    state: JsonEditorState,
    modifier: Modifier = Modifier,
    searchQuery: String = "",
    colors: JsonViewerColors = defaultJsonViewerColors,
    onJsonChange: (json: String, parsed: JsonNode?, error: JsonError?) -> Unit = { _, _, _ -> },
) {
    LaunchedEffect(state.rawJson, state.parsedJson, state.error) {
        onJsonChange(state.rawJson, state.parsedJson, state.error)
    }

    Column(modifier = modifier) {
        if (state.isEditing) {
            EditorToolbar(state = state, colors = colors)
            ErrorBanner(error = state.error, colors = colors)
            CodeEditor(state = state, searchQuery = searchQuery, colors = colors)
        } else {
            ViewerContent(state = state, searchQuery = searchQuery, colors = colors)
        }
    }
}

// ─── Viewer (with folding) ────────────────────────────────────────────────────────

@Composable
private fun ViewerContent(
    state: JsonEditorState,
    searchQuery: String,
    colors: JsonViewerColors,
) {
    val allLines = state.allLines
    val foldState = state.foldState

    if (allLines.isEmpty()) {
        // Invalid or empty JSON — show raw text
        PlainText(text = state.rawJson, searchQuery = searchQuery, colors = colors)
        return
    }

    val visibleLines by remember(allLines) {
        derivedStateOf {
            allLines.filter { line -> line.parentFoldIds.none { foldState[it] == true } }
        }
    }
    val numDigits = remember(allLines) { allLines.size.toString().length }

    SelectionContainer(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth()) {
            for (line in visibleLines) {
                val isFolded = line.foldId != null && foldState[line.foldId] == true
                Row(Modifier.fillMaxWidth()) {
                    DisableSelection {
                        GutterCell(
                            line = line,
                            isFolded = isFolded,
                            numDigits = numDigits,
                            colors = colors,
                            onFoldToggle = {
                                line.foldId?.let { id -> foldState[id] = !(foldState[id] ?: false) }
                            },
                        )
                    }
                    ContentCell(
                        line = line,
                        isFolded = isFolded,
                        searchQuery = searchQuery,
                        colors = colors,
                        onFoldToggle = {
                            line.foldId?.let { id -> foldState[id] = !(foldState[id] ?: false) }
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun PlainText(text: String, searchQuery: String, colors: JsonViewerColors) {
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
        Modifier
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

// ─── Toolbar (edit mode only) ─────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditorToolbar(
    state: JsonEditorState,
    colors: JsonViewerColors,
) {
    var showSortSheet by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.gutterBackground)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Group 1: Collapse / Expand
        ToolbarIconButton(symbol = "\u229F", tooltip = "Collapse All", colors = colors) { state.collapseAll() }
        ToolbarIconButton(symbol = "\u229E", tooltip = "Expand All", colors = colors) { state.expandAll() }

        ToolbarDivider(colors)

        // Group 2: Format
        ToolbarIconButton(
            symbol = if (state.isCompact) "{ }" else "{·}",
            tooltip = if (state.isCompact) "Beautify" else "Compact",
            colors = colors,
        ) { state.format(compact = !state.isCompact) }

        ToolbarDivider(colors)

        // Group 3: Sort
        ToolbarIconButton(symbol = "\u2195", tooltip = "Sort Keys", colors = colors) { showSortSheet = true }
    }

    if (showSortSheet) {
        val sheetState = rememberModalBottomSheetState()
        val scope = rememberCoroutineScope()
        ModalBottomSheet(
            onDismissRequest = { showSortSheet = false },
            sheetState = sheetState,
            containerColor = colors.gutterBackground,
            contentColor = colors.punctuation,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(
                    text = "Sort Keys",
                    style = monoStyle.copy(fontSize = 14.sp),
                    color = colors.key,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                SortOption(label = "Sort Ascending (A \u2192 Z)", colors = colors) {
                    state.sortKeys(ascending = true)
                    scope.launch { sheetState.hide() }.invokeOnCompletion { showSortSheet = false }
                }
                HorizontalDivider(color = colors.gutterBorder, thickness = 0.5.dp)
                SortOption(label = "Sort Descending (Z \u2192 A)", colors = colors) {
                    state.sortKeys(ascending = false)
                    scope.launch { sheetState.hide() }.invokeOnCompletion { showSortSheet = false }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ToolbarIconButton(
    symbol: String,
    tooltip: String,
    colors: JsonViewerColors,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(colors.background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = symbol,
            style = monoStyle.copy(fontSize = 14.sp),
            color = colors.key,
        )
    }
}

@Composable
private fun ToolbarDivider(colors: JsonViewerColors) {
    Spacer(Modifier.width(4.dp))
    Box(
        Modifier
            .width(1.dp)
            .height(24.dp)
            .background(colors.gutterBorder)
    )
    Spacer(Modifier.width(4.dp))
}

@Composable
private fun SortOption(label: String, colors: JsonViewerColors, onClick: () -> Unit) {
    Text(
        text = label,
        style = monoStyle.copy(fontSize = 13.sp),
        color = colors.punctuation,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
    )
}

// ─── Error Banner ─────────────────────────────────────────────────────────────────

@Composable
private fun ErrorBanner(error: JsonError?, colors: JsonViewerColors) {
    if (error == null) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF5C2020))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "\u26A0",
            style = monoStyle,
            color = Color(0xFFFF6B6B),
            modifier = Modifier.padding(end = 8.dp),
        )
        Text(
            text = error.message,
            style = monoStyle.copy(fontSize = 11.sp),
            color = Color(0xFFFF6B6B),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ─── Code Editor (plain text) ─────────────────────────────────────────────────────

@Composable
private fun CodeEditor(
    state: JsonEditorState,
    searchQuery: String,
    colors: JsonViewerColors,
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue(state.rawJson)) }
    var lastSyncedRaw by remember { mutableStateOf(state.rawJson) }

    if (state.rawJson != lastSyncedRaw) {
        textFieldValue = TextFieldValue(state.rawJson)
        lastSyncedRaw = state.rawJson
    }

    val horizontalScrollState = rememberScrollState()
    val lineCount = remember(textFieldValue.text) { textFieldValue.text.count { it == '\n' } + 1 }
    val numDigits = remember(lineCount) { lineCount.toString().length }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .background(colors.background)
    ) {
        val borderColor = colors.gutterBorder
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .background(colors.gutterBackground)
                .drawBehind {
                    val x = size.width
                    drawLine(borderColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1.dp.toPx())
                }
                .padding(start = 12.dp, end = 8.dp),
        ) {
            for (i in 1..lineCount) {
                Text(
                    text = i.toString().padStart(numDigits),
                    style = monoStyle,
                    color = colors.lineNumber,
                    softWrap = false,
                )
            }
        }

        val highlighted: AnnotatedString = remember(textFieldValue.text, searchQuery, colors) {
            highlightJson(textFieldValue.text, searchQuery, colors)
        }

        BasicTextField(
            value = textFieldValue.copy(annotatedString = highlighted),
            onValueChange = { newValue ->
                textFieldValue = newValue
                lastSyncedRaw = newValue.text
                state.updateRawJson(newValue.text)
            },
            textStyle = monoStyle,
            cursorBrush = SolidColor(colors.key),
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(horizontalScrollState)
                .padding(start = 8.dp, end = 16.dp),
        )
    }
}

// ─── Syntax Highlighting ──────────────────────────────────────────────────────────

private fun highlightJson(
    text: String,
    searchQuery: String,
    colors: JsonViewerColors,
): AnnotatedString = buildAnnotatedString {
    append(text)
    addStyle(SpanStyle(color = colors.punctuation), 0, text.length)

    val tokens = tokenizeJson(text)
    for (token in tokens) {
        val color = when (token.type) {
            TokenType.KEY -> colors.key
            TokenType.STRING -> colors.string
            TokenType.NUMBER -> colors.number
            TokenType.BOOLEAN -> colors.booleanColor
            TokenType.NULL -> colors.nullColor
            TokenType.PUNCTUATION -> colors.punctuation
        }
        addStyle(SpanStyle(color = color), token.start, token.end)
    }

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

// ─── JSON Tokenizer ───────────────────────────────────────────────────────────────

private enum class TokenType { KEY, STRING, NUMBER, BOOLEAN, NULL, PUNCTUATION }

private data class Token(val type: TokenType, val start: Int, val end: Int)

private fun tokenizeJson(text: String): List<Token> {
    val tokens = mutableListOf<Token>()
    var pos = 0
    val len = text.length

    while (pos < len) {
        val c = text[pos]
        when {
            c == '"' -> {
                val start = pos
                pos++
                while (pos < len) {
                    when (text[pos]) {
                        '\\' -> pos += 2
                        '"' -> { pos++; break }
                        else -> pos++
                    }
                }
                val isKey = isFollowedByColon(text, pos, len)
                tokens += Token(if (isKey) TokenType.KEY else TokenType.STRING, start, pos)
            }
            c == '-' || c.isDigit() -> {
                val start = pos
                if (c == '-') pos++
                while (pos < len && text[pos].isDigit()) pos++
                if (pos < len && text[pos] == '.') {
                    pos++
                    while (pos < len && text[pos].isDigit()) pos++
                }
                if (pos < len && (text[pos] == 'e' || text[pos] == 'E')) {
                    pos++
                    if (pos < len && (text[pos] == '+' || text[pos] == '-')) pos++
                    while (pos < len && text[pos].isDigit()) pos++
                }
                tokens += Token(TokenType.NUMBER, start, pos)
            }
            text.startsWith("true", pos) && (pos + 4 >= len || !text[pos + 4].isLetterOrDigit()) -> {
                tokens += Token(TokenType.BOOLEAN, pos, pos + 4); pos += 4
            }
            text.startsWith("false", pos) && (pos + 5 >= len || !text[pos + 5].isLetterOrDigit()) -> {
                tokens += Token(TokenType.BOOLEAN, pos, pos + 5); pos += 5
            }
            text.startsWith("null", pos) && (pos + 4 >= len || !text[pos + 4].isLetterOrDigit()) -> {
                tokens += Token(TokenType.NULL, pos, pos + 4); pos += 4
            }
            c == '{' || c == '}' || c == '[' || c == ']' || c == ':' || c == ',' -> {
                tokens += Token(TokenType.PUNCTUATION, pos, pos + 1); pos++
            }
            else -> pos++
        }
    }
    return tokens
}

private fun isFollowedByColon(text: String, from: Int, len: Int): Boolean {
    var i = from
    while (i < len && text[i].isWhitespace()) i++
    return i < len && text[i] == ':'
}
