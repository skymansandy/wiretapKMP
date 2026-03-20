package dev.skymansandy.jsoncmp.component.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.skymansandy.jsoncmp.component.common.highlightJson
import dev.skymansandy.jsoncmp.config.JsonEditorState
import dev.skymansandy.jsoncmp.helper.constants.colors.JsonCmpColors
import dev.skymansandy.jsoncmp.helper.constants.typography.monoStyle

@Composable
internal fun CodeEditor(
    state: JsonEditorState,
    searchQuery: String,
    colors: JsonCmpColors,
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
            .defaultMinSize(minHeight = 200.dp)
            .height(IntrinsicSize.Min)
            .background(colors.background),
    ) {
        // Line number gutter
        val borderColor = colors.gutterBorder
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .background(colors.gutterBackground)
                .drawBehind {
                    val x = size.width
                    drawLine(
                        borderColor,
                        Offset(x, 0f),
                        Offset(x, size.height),
                        strokeWidth = 1.dp.toPx(),
                    )
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

        // Text editor with syntax highlighting
        val highlighted: AnnotatedString = remember(textFieldValue.text, searchQuery, colors) {
            highlightJson(
                text = textFieldValue.text,
                searchQuery = searchQuery,
                colors = colors,
            )
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

// ── Previews ──

private val previewJson = """
{
    "name": "John Doe",
    "age": 30,
    "isActive": true
}
""".trimIndent()

private val previewColors = JsonCmpColors.Dark

@Preview
@Composable
private fun Preview_CodeEditor() {
    MaterialTheme {
        CodeEditor(
            state = JsonEditorState(
                initialJson = previewJson,
                isEditing = true,
            ),
            searchQuery = "",
            colors = previewColors,
        )
    }
}

@Preview
@Composable
private fun Preview_CodeEditorWithSearch() {
    MaterialTheme {
        CodeEditor(
            state = JsonEditorState(
                initialJson = previewJson,
                isEditing = true,
            ),
            searchQuery = "age",
            colors = previewColors,
        )
    }
}
