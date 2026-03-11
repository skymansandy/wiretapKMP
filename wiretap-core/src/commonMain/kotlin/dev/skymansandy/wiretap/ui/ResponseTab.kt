package dev.skymansandy.wiretap.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.skymansandy.jsonviewer.JsonViewer
import dev.skymansandy.wiretap.model.NetworkLogEntry

@Composable
internal fun ResponseTab(entry: NetworkLogEntry, searchQuery: String = "") {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        SectionTitle("Headers")
        HeadersList(
            headers = entry.responseHeaders,
            emptyText = "No headers",
            searchQuery = searchQuery,
        )
        val body = entry.responseBody
        SectionTitle("Body", action = if (body != null) ({ CopyBodyButton(body) }) else null)
        if (body != null && looksLikeJson(body)) {
            JsonViewer(
                json = body,
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
