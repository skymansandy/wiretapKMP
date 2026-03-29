/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

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
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.helper.constants.jsonMockText
import dev.skymansandy.wiretap.helper.util.highlightText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private const val SEARCH_DEBOUNCE_MS = 300L

@Composable
internal fun CodeBlock(
    modifier: Modifier = Modifier,
    text: String,
    searchQuery: String = "",
) {
    val highlightedText = produceState(AnnotatedString(text), text, searchQuery) {
        if (searchQuery.isNotBlank()) delay(SEARCH_DEBOUNCE_MS)

        value = withContext(Dispatchers.Default) {
            highlightText(text, searchQuery)
        }
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
                text = highlightedText.value,
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
