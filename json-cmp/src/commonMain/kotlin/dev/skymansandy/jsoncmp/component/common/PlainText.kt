package dev.skymansandy.jsoncmp.component.common

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.skymansandy.jsoncmp.helper.constants.colors.JsonCmpColors
import dev.skymansandy.jsoncmp.helper.constants.typography.monoStyle

@Composable
internal fun PlainText(
    modifier: Modifier = Modifier,
    text: String,
    searchQuery: String,
    colors: JsonCmpColors,
) {
    val annotated = buildAnnotatedString {
        append(text)
        addStyle(SpanStyle(color = colors.punctuation), 0, text.length)
        if (searchQuery.isNotBlank()) {
            val lowerText = text.lowercase()
            val lowerQuery = searchQuery.lowercase()
            var idx = lowerText.indexOf(lowerQuery)
            while (idx >= 0) {
                addStyle(
                    SpanStyle(background = colors.highlight, color = colors.highlightFg),
                    idx,
                    idx + lowerQuery.length,
                )
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

// ── Previews ──

private val previewColors = JsonCmpColors.Dark

@Preview
@Composable
private fun Preview_PlainText() {
    MaterialTheme {
        PlainText(
            text = "This is plain, unparseable text content",
            searchQuery = "",
            colors = previewColors,
        )
    }
}

@Preview
@Composable
private fun Preview_PlainTextWithSearch() {
    MaterialTheme {
        PlainText(
            text = "This is plain, unparseable text content",
            searchQuery = "plain",
            colors = previewColors,
        )
    }
}
