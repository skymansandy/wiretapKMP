package dev.skymansandy.wiretap.ui.network

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.util.copyToClipboard

@Composable
internal fun SectionTitle(text: String, action: (@Composable () -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 4.dp, top = 16.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
        action?.invoke()
    }
}

@Composable
internal fun CopyBodyButton(body: String) {
    TextButton(onClick = { copyToClipboard(body) }) {
        Icon(
            imageVector = Icons.Filled.ContentCopy,
            contentDescription = "Copy body",
            modifier = Modifier.size(14.dp),
        )
        Spacer(Modifier.width(4.dp))
        Text("Copy", style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
internal fun KeyValueTable(rows: List<Pair<String, String>>) {
    SelectionContainer {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            rows.forEach { (key, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        text = key,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(0.35f),
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(0.65f),
                    )
                }
            }
        }
    }
}

@Composable
internal fun HeadersList(
    headers: Map<String, String>,
    emptyText: String,
    searchQuery: String = "",
) {
    if (headers.isEmpty()) {
        Text(
            text = emptyText,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        return
    }
    SelectionContainer {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            headers.forEach { (key, value) ->
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(highlightText(key, searchQuery))
                        }
                        append(highlightText(": $value", searchQuery))
                    },
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
internal fun CodeBlock(
    text: String,
    modifier: Modifier = Modifier,
    searchQuery: String = "",
) {
    SelectionContainer(modifier = modifier.fillMaxWidth()) {
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

internal fun looksLikeJson(text: String): Boolean {
    val t = text.trim()
    return (t.startsWith("{") && t.endsWith("}")) || (t.startsWith("[") && t.endsWith("]"))
}

internal fun highlightText(text: String, query: String): AnnotatedString {
    if (query.isBlank()) return AnnotatedString(text)
    return buildAnnotatedString {
        val lowerText = text.lowercase()
        val lowerQuery = query.lowercase()
        var cursor = 0
        var match = lowerText.indexOf(lowerQuery, cursor)
        while (match >= 0) {
            append(text.substring(cursor, match))
            withStyle(SpanStyle(background = Color(0xFFFFEB3B), color = Color.Black)) {
                append(text.substring(match, match + query.length))
            }
            cursor = match + query.length
            match = lowerText.indexOf(lowerQuery, cursor)
        }
        append(text.substring(cursor))
    }
}
