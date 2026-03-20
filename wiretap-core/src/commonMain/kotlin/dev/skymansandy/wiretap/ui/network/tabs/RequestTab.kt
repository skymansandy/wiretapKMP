package dev.skymansandy.wiretap.ui.network.tabs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.skymansandy.jsonviewer.JsonEditor
import dev.skymansandy.jsonviewer.rememberJsonEditorState
import dev.skymansandy.wiretap.data.db.entity.NetworkLogEntry
import dev.skymansandy.wiretap.ui.components.CodeBlock
import dev.skymansandy.wiretap.ui.components.CopyBodyButton
import dev.skymansandy.wiretap.ui.components.CopyHeadersButton
import dev.skymansandy.wiretap.ui.components.HeadersList
import dev.skymansandy.wiretap.ui.components.SectionTitle
import dev.skymansandy.wiretap.util.looksLikeJson
import dev.skymansandy.wiretap.resources.*
import androidx.compose.material3.MaterialTheme
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
internal fun RequestTab(entry: NetworkLogEntry, searchQuery: String = "") {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        SectionTitle(stringResource(Res.string.headers), action = if (entry.requestHeaders.isNotEmpty()) ({ CopyHeadersButton(entry.requestHeaders) }) else null)
        HeadersList(
            headers = entry.requestHeaders,
            emptyText = stringResource(Res.string.no_headers),
            searchQuery = searchQuery,
        )
        val body = entry.requestBody
        SectionTitle(stringResource(Res.string.body), action = if (body != null) ({ CopyBodyButton(body) }) else null)
        if (body != null && looksLikeJson(body)) {
            val editorState = rememberJsonEditorState(initialJson = body)
            JsonEditor(
                state = editorState,
                searchQuery = searchQuery,
                modifier = Modifier.padding(8.dp),
            )
        } else {
            CodeBlock(
                text = body ?: stringResource(Res.string.no_body),
                modifier = Modifier.padding(16.dp),
                searchQuery = searchQuery,
            )
        }
    }
}

@Preview
@Composable
private fun RequestTabPreview() {
    MaterialTheme {
        RequestTab(
            entry = NetworkLogEntry(
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
private fun RequestTabEmptyPreview() {
    MaterialTheme {
        RequestTab(
            entry = NetworkLogEntry(
                id = 2,
                url = "https://api.example.com/users/123",
                method = "GET",
                responseCode = 200,
                timestamp = 1710850000000,
            ),
        )
    }
}
