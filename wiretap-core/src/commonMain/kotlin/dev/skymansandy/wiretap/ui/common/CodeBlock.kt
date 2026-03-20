package dev.skymansandy.wiretap.ui.common

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.helper.constants.jsonMockText

@Composable
internal fun CodeBlock(
    modifier: Modifier = Modifier,
    text: String,
    searchQuery: String = "",
) {
    SelectionContainer(
        modifier = modifier.fillMaxWidth(),
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = highlightText(text, searchQuery),
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(12.dp),
            )
        }
    }
}

@Preview
@Composable
private fun CodeBlockPreview() {
    MaterialTheme {
        CodeBlock(text = jsonMockText)
    }
}

@Preview
@Composable
private fun CodeBlockWithSearchPreview() {
    MaterialTheme {
        CodeBlock(
            text = jsonMockText,
            searchQuery = "John",
        )
    }
}
