package dev.skymansandy.wiretapsample.ui.design.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.design.component.badge.ConnectionState
import dev.skymansandy.wiretap.design.component.badge.MethodBadge
import dev.skymansandy.wiretap.design.component.badge.SourceDot
import dev.skymansandy.wiretap.design.component.badge.StateChip
import dev.skymansandy.wiretap.design.component.badge.StatusBadge
import dev.skymansandy.wiretap.design.component.badge.TagBadge
import dev.skymansandy.wiretap.design.component.badge.TagKind
import dev.skymansandy.wiretap.design.foundation.HttpMethod
import dev.skymansandy.wiretap.design.foundation.LogSource
import dev.skymansandy.wiretap.design.foundation.statusReasonFor

@Composable
fun BadgesSection() {
    DSSectionHeader("Badges & status")
    Stack(gap = 18.dp) {
        DSCard("HTTP methods", "GET · POST · PUT · PATCH · DELETE · ANY · WS · SSE") {
            Cluster(horizontalGap = 6.dp) {
                HttpMethod.values().forEach { MethodBadge(it) }
            }
        }
        DSCard("Status codes", "Full range — colored by class") {
            Cluster(horizontalGap = 6.dp) {
                listOf(100, 200, 201, 204, 301, 304, 401, 403, 404, 422, 429, 500, 502, 503).forEach { code ->
                    StatusBadge(status = code, reason = statusReasonFor(code))
                }
            }
        }
        DSCard("Connection state", "Live pulse on open/connecting") {
            Cluster(horizontalGap = 8.dp) {
                ConnectionState.values().forEach { StateChip(it) }
            }
        }
        DSCard("Tags & source dots", "Rule criteria tags + log source") {
            Stack(gap = 12.dp) {
                Cluster(horizontalGap = 6.dp) {
                    TagBadge("URL")
                    TagBadge("HDR×2")
                    TagBadge("BODY~")
                    TagBadge("Mock", TagKind.Mock)
                    TagBadge("Throttle", TagKind.Throttle)
                }
                Cluster(horizontalGap = 18.dp) {
                    LogSource.values().forEach { source ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(7.dp),
                        ) {
                            SourceDot(source)
                            DSCap(source.name.lowercase())
                        }
                    }
                }
            }
        }
    }
}
