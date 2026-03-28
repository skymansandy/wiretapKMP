package dev.skymansandy.wiretap.ui.screens.http.detail.tabs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.skymansandy.wiretap.domain.model.HttpLog
import dev.skymansandy.wiretap.domain.model.ResponseSource
import dev.skymansandy.wiretap.domain.model.TimingPhase
import dev.skymansandy.wiretap.helper.util.formatSize
import dev.skymansandy.wiretap.ui.common.KeyValueTable
import dev.skymansandy.wiretap.ui.screens.http.detail.component.StatusHeader
import dev.skymansandy.wiretap.ui.screens.http.detail.component.TimingGanttChart

@Composable
internal fun OverviewTab(
    modifier: Modifier = Modifier,
    entry: HttpLog,
) {

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState()),
    ) {

        StatusHeader(
            modifier = Modifier.fillMaxWidth(),
            entry = entry,
        )

        if (entry.timingPhases.isNotEmpty() && entry.source != ResponseSource.Mock) {
            HorizontalDivider()
            TimingGanttChart(
                modifier = Modifier.fillMaxWidth(),
                phases = entry.timingPhases,
                totalDurationMs = entry.durationMs,
            )
        }

        HorizontalDivider()

        KeyValueTable(
            modifier = Modifier.fillMaxWidth(),
            rows = remember(entry) {
                buildList {
                    add("URL" to entry.url)
                    add("Method" to entry.method)
                    add("Source" to entry.source.name)
                    add("Request Size" to formatSize(entry.requestBody?.encodeToByteArray()?.size?.toLong()))
                    add("Response Size" to formatSize(entry.responseBodySize))
                    entry.protocol?.let { add("HTTP Version" to it) }
                    entry.remoteAddress?.let { add("Remote Address" to it) }
                    entry.tlsProtocol?.let { add("TLS Protocol" to it) }
                    entry.cipherSuite?.let { add("Cipher Suite" to it) }
                    entry.certificateCn?.let { add("Certificate CN" to it) }
                    entry.issuerCn?.let { add("Issuer CN" to it) }
                    entry.certificateExpiry?.let { add("Valid Until" to it) }
                }
            },
        )
    }
}

@Preview
@Composable
private fun Preview_OverviewTab() {
    MaterialTheme {
        OverviewTab(
            entry = HttpLog(
                id = 1,
                url = "https://api.example.com/users/123",
                method = "GET",
                responseCode = 200,
                durationMs = 142,
                timestamp = 1710850000000,
                requestBody = """{"query":"test"}""",
                responseBody = """{"name":"John","age":30}""",
                protocol = "HTTP/2",
                remoteAddress = "93.184.216.34:443",
                tlsProtocol = "TLSv1.3",
                cipherSuite = "TLS_AES_256_GCM_SHA384",
                timingPhases = listOf(
                    TimingPhase(name = "DNS", startMs = 0.0, durationMs = 5.2),
                    TimingPhase(name = "TCP", startMs = 5.2, durationMs = 23.4),
                    TimingPhase(name = "TLS", startMs = 12.0, durationMs = 18.1),
                    TimingPhase(name = "Request", startMs = 28.6, durationMs = 2.1),
                    TimingPhase(name = "Waiting", startMs = 30.7, durationMs = 95.3),
                    TimingPhase(name = "Download", startMs = 126.0, durationMs = 16.0),
                ),
            ),
        )
    }
}

@Preview
@Composable
private fun Preview_OverviewTab_InProgress() {
    MaterialTheme {
        OverviewTab(
            entry = HttpLog(
                id = 2,
                url = "https://api.example.com/slow",
                method = "POST",
                timestamp = 1710850000000,
            ),
        )
    }
}
