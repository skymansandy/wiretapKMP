package dev.skymansandy.wiretap.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
internal fun HeadersList(
    modifier: Modifier = Modifier,
    headers: Map<String, String>,
    emptyText: String,
    searchQuery: String = "",
) {
    if (headers.isEmpty()) {
        Text(
            text = emptyText,
            style = MaterialTheme.typography.bodySmall,
            modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        return
    }

    SelectionContainer(modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            headers.forEach { (key, value) ->
                Text(
                    style = MaterialTheme.typography.bodySmall,
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(highlightText(key, searchQuery))
                        }
                        append(highlightText(": $value", searchQuery))
                    },
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview_HeadersList() {
    MaterialTheme {
        HeadersList(
            headers = mapOf(
                "Content-Type" to "application/json",
                "Authorization" to "Bearer eyJhbGciOi...",
                "Accept" to "*/*",
                "Cache-Control" to "no-cache",
            ),
            emptyText = "No headers",
        )
    }
}

@Preview
@Composable
private fun Preview_HeadersListEmpty() {
    MaterialTheme {
        HeadersList(
            headers = emptyMap(),
            emptyText = "No headers",
        )
    }
}

@Preview
@Composable
private fun Preview_HeadersListWithSearch() {
    MaterialTheme {
        HeadersList(
            headers = mapOf(
                "Content-Type" to "application/json",
                "Authorization" to "Bearer token",
            ),
            emptyText = "No headers",
            searchQuery = "json",
        )
    }
}
