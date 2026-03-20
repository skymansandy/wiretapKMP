package dev.skymansandy.wiretap.ui.screens.console.http.components.tabs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.skymansandy.jsoncmp.JsonCMP
import dev.skymansandy.jsoncmp.config.rememberJsonEditorState
import dev.skymansandy.wiretap.data.db.entity.HttpLogEntry
import dev.skymansandy.wiretap.ui.common.CodeBlock
import dev.skymansandy.wiretap.ui.common.CopyBodyButton
import dev.skymansandy.wiretap.ui.common.CopyHeadersButton
import dev.skymansandy.wiretap.ui.common.HeadersList
import dev.skymansandy.wiretap.ui.common.SectionTitle
import dev.skymansandy.wiretap.helper.util.looksLikeJson
import dev.skymansandy.wiretap.resources.*
import androidx.compose.material3.MaterialTheme
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
internal fun ResponseTab(
    modifier: Modifier = Modifier,
    entry: HttpLogEntry,
    searchQuery: String = "",
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        SectionTitle(
            text = stringResource(Res.string.headers),
            action = if (entry.responseHeaders.isNotEmpty()) ({ CopyHeadersButton(headers = entry.responseHeaders) }) else null
        )

        HeadersList(
            headers = entry.responseHeaders,
            emptyText = stringResource(Res.string.no_headers),
            searchQuery = searchQuery,
        )

        val body = entry.responseBody
        SectionTitle(
            text = stringResource(Res.string.body),
            action = if (body != null) ({ CopyBodyButton(body = body) }) else null
        )

        if (body != null && looksLikeJson(body)) {
            val editorState = rememberJsonEditorState(initialJson = body)
            JsonCMP(
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
private fun Preview_ResponseTab() {
    MaterialTheme {
        ResponseTab(
            entry = HttpLogEntry(
                id = 1,
                url = "https://api.example.com/users/123",
                method = "GET",
                responseCode = 200,
                responseHeaders = mapOf(
                    "Content-Type" to "application/json",
                    "Cache-Control" to "max-age=3600",
                    "X-Request-Id" to "abc-123-def",
                ),
                responseBody = """{"id":123,"name":"John","email":"john@example.com","age":30}""",
                timestamp = 1710850000000,
            ),
        )
    }
}

@Preview
@Composable
private fun Preview_ResponseTabEmpty() {
    MaterialTheme {
        ResponseTab(
            entry = HttpLogEntry(
                id = 2,
                url = "https://api.example.com/users/123",
                method = "DELETE",
                responseCode = 204,
                timestamp = 1710850000000,
            ),
        )
    }
}
