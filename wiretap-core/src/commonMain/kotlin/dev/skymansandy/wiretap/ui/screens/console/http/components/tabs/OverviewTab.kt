package dev.skymansandy.wiretap.ui.screens.console.http.components.tabs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.skymansandy.wiretap.data.db.entity.HttpLogEntry
import dev.skymansandy.wiretap.helper.util.formatSize
import dev.skymansandy.wiretap.resources.*
import dev.skymansandy.wiretap.ui.common.KeyValueTable
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun OverviewTab(
    modifier: Modifier = Modifier,
    entry: HttpLogEntry,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        val labelUrl = stringResource(Res.string.label_url)
        val labelMethod = stringResource(Res.string.label_method)
        val labelStatus = stringResource(Res.string.label_status)
        val statusInProgress = stringResource(Res.string.status_in_progress)
        val labelDuration = stringResource(Res.string.label_duration)
        val labelSource = stringResource(Res.string.label_source)
        val labelRequestSize = stringResource(Res.string.label_request_size)
        val labelResponseSize = stringResource(Res.string.label_response_size)
        val labelHttpVersion = stringResource(Res.string.label_http_version)
        val labelRemoteAddress = stringResource(Res.string.label_remote_address)
        val labelTlsProtocol = stringResource(Res.string.label_tls_protocol)
        val labelCipherSuite = stringResource(Res.string.label_cipher_suite)
        val labelCertificateCn = stringResource(Res.string.label_certificate_cn)
        val labelIssuerCn = stringResource(Res.string.label_issuer_cn)
        val labelValidUntil = stringResource(Res.string.label_valid_until)

        KeyValueTable(
            rows = buildList {
                add(labelUrl to entry.url)
                add(labelMethod to entry.method)
                add(labelStatus to if (entry.isInProgress) statusInProgress else entry.responseCode.toString())
                add(labelDuration to if (entry.isInProgress) "..." else "${entry.durationMs}ms")
                add(labelSource to entry.source.name)
                add(labelRequestSize to formatSize(entry.requestBody?.encodeToByteArray()?.size?.toLong()))
                add(labelResponseSize to formatSize(entry.responseBody?.encodeToByteArray()?.size?.toLong()))
                entry.protocol?.let { add(labelHttpVersion to it) }
                entry.remoteAddress?.let { add(labelRemoteAddress to it) }
                entry.tlsProtocol?.let { add(labelTlsProtocol to it) }
                entry.cipherSuite?.let { add(labelCipherSuite to it) }
                entry.certificateCn?.let { add(labelCertificateCn to it) }
                entry.issuerCn?.let { add(labelIssuerCn to it) }
                entry.certificateExpiry?.let { add(labelValidUntil to it) }
            },
        )
    }
}

@Preview
@Composable
private fun Preview_OverviewTab() {
    MaterialTheme {
        OverviewTab(
            entry = HttpLogEntry(
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
            ),
        )
    }
}
