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

@Composable
internal fun RequestTab(entry: NetworkLogEntry, searchQuery: String = "") {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        SectionTitle("Headers", action = if (entry.requestHeaders.isNotEmpty()) ({ CopyHeadersButton(entry.requestHeaders) }) else null)
        HeadersList(
            headers = entry.requestHeaders,
            emptyText = "No headers",
            searchQuery = searchQuery,
        )
        val body = entry.requestBody
        SectionTitle("Body", action = if (body != null) ({ CopyBodyButton(body) }) else null)
        if (body != null && looksLikeJson(body)) {
            val editorState = rememberJsonEditorState(initialJson = body)
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
