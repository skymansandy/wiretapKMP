package dev.skymansandy.wiretap.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.skymansandy.wiretap.model.NetworkLogEntry

@Composable
internal fun OverviewTab(entry: NetworkLogEntry) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        KeyValueTable(
            rows = listOf(
                "URL" to entry.url,
                "Method" to entry.method,
                "Status" to entry.responseCode.toString(),
                "Duration" to "${entry.durationMs}ms",
                "Source" to entry.source.name,
            ),
        )
    }
}
