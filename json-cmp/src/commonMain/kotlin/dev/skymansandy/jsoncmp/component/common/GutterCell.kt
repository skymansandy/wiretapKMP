package dev.skymansandy.jsoncmp.component.common

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.skymansandy.jsoncmp.helper.constants.cellVerticalPadding
import dev.skymansandy.jsoncmp.helper.constants.colors.JsonCmpColors
import dev.skymansandy.jsoncmp.helper.constants.foldGlyphSize
import dev.skymansandy.jsoncmp.helper.constants.typography.monoStyle
import dev.skymansandy.jsoncmp.model.FoldType
import dev.skymansandy.jsoncmp.model.JsonLine
import dev.skymansandy.jsoncmp.model.JsonPart

@Composable
internal fun GutterCell(
    line: JsonLine,
    isFolded: Boolean,
    numDigits: Int,
    colors: JsonCmpColors,
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
            modifier = Modifier.padding(
                start = 12.dp,
                end = 6.dp,
                top = cellVerticalPadding,
                bottom = cellVerticalPadding,
            ),
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
)

@Preview
@Composable
private fun Preview_GutterCell() {
    MaterialTheme {
        GutterCell(
            line = previewLine,
            isFolded = false,
            numDigits = 2,
            colors = previewColors,
            onFoldToggle = {},
        )
    }
}

@Preview
@Composable
private fun Preview_GutterCellFoldable() {
    MaterialTheme {
        GutterCell(
            line = previewFoldableLine,
            isFolded = false,
            numDigits = 2,
            colors = previewColors,
            onFoldToggle = {},
        )
    }
}

@Preview
@Composable
private fun Preview_GutterCellFolded() {
    MaterialTheme {
        GutterCell(
            line = previewFoldableLine,
            isFolded = true,
            numDigits = 2,
            colors = previewColors,
            onFoldToggle = {},
        )
    }
}
