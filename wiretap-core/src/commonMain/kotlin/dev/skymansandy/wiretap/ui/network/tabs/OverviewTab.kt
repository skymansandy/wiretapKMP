package dev.skymansandy.wiretap.ui.network.tabs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.skymansandy.wiretap.data.db.entity.NetworkLogEntry
import dev.skymansandy.wiretap.ui.network.KeyValueTable

private fun formatSize(bytes: Long?): String {
    if (bytes == null || bytes == 0L) return "0 B"
    return when {
        bytes >= 1_048_576 -> "${"%.1f".format(bytes / 1_048_576f)} MB"
        bytes >= 1_024 -> "${"%.1f".format(bytes / 1_024f)} kB"
        else -> "$bytes B"
    }
}

@Composable
internal fun OverviewTab(entry: NetworkLogEntry) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        KeyValueTable(
            rows = buildList {
                add("URL" to entry.url)
                add("Method" to entry.method)
                add("Status" to if (entry.isInProgress) "In Progress" else entry.responseCode.toString())
                add("Duration" to if (entry.isInProgress) "..." else "${entry.durationMs}ms")
                add("Source" to entry.source.name)
                add("Request Size" to formatSize(entry.requestBody?.encodeToByteArray()?.size?.toLong()))
                add("Response Size" to formatSize(entry.responseBody?.encodeToByteArray()?.size?.toLong()))
                entry.protocol?.let { add("HTTP Version" to it) }
                entry.remoteAddress?.let { add("Remote Address" to it) }
                entry.tlsProtocol?.let { add("TLS Protocol" to it) }
                entry.cipherSuite?.let { add("Cipher Suite" to it) }
                entry.certificateCn?.let { add("Certificate CN" to it) }
                entry.issuerCn?.let { add("Issuer CN" to it) }
                entry.certificateExpiry?.let { add("Valid Until" to it) }
            },
        )
    }
}
