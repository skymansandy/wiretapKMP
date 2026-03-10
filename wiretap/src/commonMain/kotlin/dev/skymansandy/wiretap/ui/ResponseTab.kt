package dev.skymansandy.wiretap.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.model.NetworkLogEntry

@Composable
internal fun ResponseTab(entry: NetworkLogEntry) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        SectionTitle("Headers")
        HeadersList(
            headers = entry.responseHeaders,
            emptyText = "No headers",
        )
        SectionTitle("Body")
        CodeBlock(
            text = entry.responseBody ?: "No body",
            modifier = Modifier.padding(16.dp),
        )
    }
}
