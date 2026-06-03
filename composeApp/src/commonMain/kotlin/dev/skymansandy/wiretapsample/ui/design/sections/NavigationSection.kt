package dev.skymansandy.wiretapsample.ui.design.sections

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.design.component.button.WiretapIconButton
import dev.skymansandy.wiretap.design.component.icon.WiretapIcons
import dev.skymansandy.wiretap.design.component.nav.BottomNav
import dev.skymansandy.wiretap.design.component.nav.BottomNavItem
import dev.skymansandy.wiretap.design.component.nav.DetailTabs
import dev.skymansandy.wiretap.design.component.nav.NavRail
import dev.skymansandy.wiretap.design.component.nav.ScreenHeader
import dev.skymansandy.wiretap.design.component.nav.SubTabs

@Composable
fun NavigationSection() {
    DSSectionHeader("Navigation & headers")
    Stack(gap = 18.dp) {
        DSCard("Sub-tabs", "Logs/Rules + detail tabs — click to switch") {
            var top by remember { mutableStateOf(0) }
            var detail by remember { mutableStateOf(0) }
            Stack(gap = 16.dp) {
                SubTabs(items = listOf("Logs", "Rules"), selected = top, onSelect = { top = it })
                DetailTabs(items = listOf("Overview", "Request", "Response"), selected = detail, onSelect = { detail = it })
            }
        }
        DSCard("Screen header", "Back · title/subtitle · trailing actions") {
            Stack(gap = 0.dp) {
                ScreenHeader(
                    title = "Rule Details",
                    subtitle = "rule_id r2 · mock",
                    leading = {
                        WiretapIconButton(onClick = {}) { Icon(WiretapIcons.Back, null) }
                    },
                    trailing = {
                        WiretapIconButton(onClick = {}) { Icon(WiretapIcons.Edit, null) }
                        WiretapIconButton(onClick = {}) { Icon(WiretapIcons.Trash, null) }
                    },
                )
                ScreenHeader(
                    title = "HTTP Console",
                    subtitle = "13 captured · live",
                    trailing = {
                        WiretapIconButton(onClick = {}) { Icon(WiretapIcons.Search, null) }
                        WiretapIconButton(onClick = {}, badgeCount = 2) { Icon(WiretapIcons.Filter, null) }
                    },
                )
            }
        }
        DSCard("Bottom nav (phone)", "Active tab in accent — click") {
            var sel by remember { mutableStateOf("http") }
            val items = listOf(
                BottomNavItem("http", "HTTP", WiretapIcons.Http),
                BottomNavItem("ws", "Sockets", WiretapIcons.Ws),
                BottomNavItem("sse", "SSE", WiretapIcons.Sse),
            )
            BottomNav(items = items, selectedKey = sel, onSelect = { sel = it })
        }
        DSCard("Nav rail (desktop)", "Brand + rail items + live proxy footer") {
            var sel by remember { mutableStateOf("http") }
            val items = listOf(
                BottomNavItem("http", "HTTP", WiretapIcons.Http),
                BottomNavItem("ws", "Sockets", WiretapIcons.Ws),
                BottomNavItem("sse", "SSE", WiretapIcons.Sse),
            )
            Box(modifier = Modifier.height(280.dp)) {
                NavRail(
                    items = items,
                    selectedKey = sel,
                    onSelect = { sel = it },
                    statusText = "proxy :8765",
                    modifier = Modifier.fillMaxHeight().padding(end = 8.dp),
                )
            }
        }
    }
}
