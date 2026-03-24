package dev.skymansandy.wiretap.ui.screens.console.http.components.tabs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.skymansandy.jsoncmp.JsonCMP
import dev.skymansandy.jsoncmp.config.rememberJsonEditorState
import dev.skymansandy.wiretap.data.db.entity.HttpLogEntry
import dev.skymansandy.wiretap.helper.util.looksLikeJson
import dev.skymansandy.wiretap.ui.common.CodeBlock
import dev.skymansandy.wiretap.ui.common.CopyBodyButton
import dev.skymansandy.wiretap.ui.common.CopyHeadersButton
import dev.skymansandy.wiretap.ui.common.HeadersList
import dev.skymansandy.wiretap.ui.common.SectionTitle

@Composable
internal fun RequestTab(
    modifier: Modifier = Modifier,
    entry: HttpLogEntry,
    searchQuery: String = "",
) {
    val body = entry.requestBody
    val isJson = body != null && looksLikeJson(body)

    var headersExpanded by remember { mutableStateOf(true) }

    Column(
        modifier = modifier.then(if (!isJson) Modifier.verticalScroll(rememberScrollState()) else Modifier),
    ) {
        SectionTitle(
            text = "Headers",
            action = if (entry.requestHeaders.isNotEmpty()) ({ CopyHeadersButton(headers = entry.requestHeaders) }) else null,
            expanded = headersExpanded,
            onToggleExpand = { headersExpanded = !headersExpanded },
        )

        AnimatedVisibility(visible = headersExpanded) {
            HeadersList(
                headers = entry.requestHeaders,
                emptyText = "No headers",
                searchQuery = searchQuery,
            )
        }

        SectionTitle(
            text = "Body",
            action = if (body != null) ({ CopyBodyButton(body = body) }) else null,
        )

        if (isJson) {
            val editorState = rememberJsonEditorState(initialJson = body!!)
            JsonCMP(
                state = editorState,
                searchQuery = searchQuery,
                modifier = Modifier.padding(8.dp),
            )
        } else {
            CodeBlock(
                text = body ?: "No body",
                modifier = Modifier.padding(16.dp),
                searchQuery = searchQuery,
            )
        }
    }
}

@Preview
@Composable
private fun Preview_RequestTab() {
    MaterialTheme {
        RequestTab(
            entry = HttpLogEntry(
                id = 1,
                url = "https://api.example.com/users",
                method = "POST",
                requestHeaders = mapOf(
                    "Content-Type" to "application/json",
                    "Authorization" to "Bearer eyJhbGciOi...",
                    "Accept" to "application/json",
                ),
                requestBody = """{"name":"John","email":"john@example.com"}""",
                responseCode = 201,
                timestamp = 1710850000000,
            ),
        )
    }
}

@Preview
@Composable
private fun Preview_RequestTabEmpty() {
    MaterialTheme {
        RequestTab(
            entry = HttpLogEntry(
                id = 2,
                url = "https://api.example.com/users/123",
                method = "GET",
                responseCode = 200,
                timestamp = 1710850000000,
            ),
        )
    }
}
