package dev.skymansandy.wiretapsample.ui.design.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.design.component.badge.MethodBadge
import dev.skymansandy.wiretap.design.component.badge.StatusBadge
import dev.skymansandy.wiretap.design.component.data.CodeBlock
import dev.skymansandy.wiretap.design.component.data.Collapsible
import dev.skymansandy.wiretap.design.component.data.JsonViewer
import dev.skymansandy.wiretap.design.component.input.SearchField
import dev.skymansandy.wiretap.design.component.list.KeyValueRow
import dev.skymansandy.wiretap.design.component.list.KeyValueTable
import dev.skymansandy.wiretap.design.foundation.HttpMethod
import dev.skymansandy.wiretap.design.theme.WiretapDesign

@Composable
fun DetailSection() {
    DSSectionHeader("Detail & data")
    Stack(gap = 18.dp) {
        DSCard("Key/value overview", "kv-table — label column + mono values") {
            KeyValueTable(
                rows = listOf(
                    KeyValueRow("Method", valueComposable = { MethodBadge(HttpMethod.GET) }),
                    KeyValueRow("Status", valueComposable = { StatusBadge(status = 200, reason = "OK") }),
                    KeyValueRow("Duration", "124 ms", mono = true),
                    KeyValueRow("Response size", "18.0 KB", mono = true),
                    KeyValueRow("Protocol", "HTTP/2", mono = true),
                ),
            )
        }
        DSCard("Collapsible section", "Click the header to expand/collapse") {
            val c = WiretapDesign.colors
            Collapsible(
                title = "Headers",
                count = SampleHeaders.size,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, c.border1, RoundedCornerShape(10.dp)),
            ) {
                KeyValueTable(
                    rows = SampleHeaders.map { (k, v) -> KeyValueRow(k, v, mono = true) },
                    labelWidth = 140.dp,
                )
            }
        }
        DSCard("JSON viewer", "Syntax + line numbers + live search highlight") {
            var q by remember { mutableStateOf("") }
            val c = WiretapDesign.colors
            Stack(gap = 10.dp) {
                SearchField(value = q, onValueChange = { q = it }, placeholder = "Highlight in body…")
                JsonViewer(
                    json = SampleJson,
                    searchQuery = q,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(c.surface0, RoundedCornerShape(10.dp))
                        .border(1.dp, c.border1, RoundedCornerShape(10.dp))
                        .padding(horizontal = 4.dp),
                )
            }
        }
        DSCard("Code block", "Mono, line-numbered — request as cURL") {
            val c = WiretapDesign.colors
            CodeBlock(
                code = SampleCurl,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(c.surface0, RoundedCornerShape(10.dp))
                    .border(1.dp, c.border1, RoundedCornerShape(10.dp))
                    .padding(horizontal = 4.dp),
            )
        }
    }
}
