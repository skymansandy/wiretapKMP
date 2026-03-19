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
import dev.skymansandy.wiretap.ui.network.CodeBlock
import dev.skymansandy.wiretap.ui.network.CopyBodyButton
import dev.skymansandy.wiretap.ui.network.CopyHeadersButton
import dev.skymansandy.wiretap.ui.network.HeadersList
import dev.skymansandy.wiretap.ui.network.SectionTitle
import dev.skymansandy.wiretap.ui.network.looksLikeJson

@Composable
internal fun ResponseTab(entry: NetworkLogEntry, searchQuery: String = "") {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        SectionTitle("Headers", action = if (entry.responseHeaders.isNotEmpty()) ({ CopyHeadersButton(entry.responseHeaders) }) else null)
        HeadersList(
            headers = entry.responseHeaders,
            emptyText = "No headers",
            searchQuery = searchQuery,
        )
        val body = entry.responseBody
        SectionTitle("Body", action = if (body != null) ({ CopyBodyButton(body) }) else null)
        if (body != null && looksLikeJson(body)) {
            val editorState = rememberJsonEditorState(initialJson = body, isEditing = true)
            JsonEditor(
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
