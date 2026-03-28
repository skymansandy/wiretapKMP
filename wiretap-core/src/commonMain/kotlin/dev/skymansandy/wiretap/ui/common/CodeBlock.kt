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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.helper.constants.jsonMockText
import dev.skymansandy.wiretap.helper.util.highlightText

@Composable
internal fun CodeBlock(
    modifier: Modifier = Modifier,
    text: String,
    searchQuery: String = "",
) {
    val highlightedText = remember(text, searchQuery) {
        highlightText(text, searchQuery)
    }

    SelectionContainer(
        modifier = modifier,
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = highlightedText,
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
private fun Preview_CodeBlock() {
    MaterialTheme {
        CodeBlock(
            modifier = Modifier.fillMaxWidth(),
            text = jsonMockText,
        )
    }
}

@Preview
@Composable
private fun Preview_CodeBlockWithSearch() {
    MaterialTheme {
        CodeBlock(
            modifier = Modifier.fillMaxWidth(),
            text = jsonMockText,
            searchQuery = "John",
        )
    }
}
