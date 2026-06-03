package dev.skymansandy.wiretapsample.ui.design.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.design.component.button.WiretapButton
import dev.skymansandy.wiretap.design.component.button.WiretapButtonSize
import dev.skymansandy.wiretap.design.component.button.WiretapButtonStyle
import dev.skymansandy.wiretap.design.component.button.WiretapFab
import dev.skymansandy.wiretap.design.component.button.WiretapIconButton
import dev.skymansandy.wiretap.design.component.icon.WiretapIcons
import dev.skymansandy.wiretap.design.component.input.SearchField
import dev.skymansandy.wiretap.design.component.input.WiretapCheckbox
import dev.skymansandy.wiretap.design.component.input.WiretapChip
import dev.skymansandy.wiretap.design.component.input.WiretapSwitch
import dev.skymansandy.wiretap.design.component.input.WiretapTextField

@Composable
fun ActionsSection() {
    DSSectionHeader("Actions & inputs")
    Stack(gap = 18.dp) {
        DSCard("Buttons", "Primary · secondary · ghost · danger · small · disabled · FAB") {
            Stack(gap = 14.dp) {
                Cluster(horizontalGap = 10.dp) {
                    WiretapButton(text = "Apply", onClick = {})
                    WiretapButton(text = "Cancel", onClick = {}, style = WiretapButtonStyle.Secondary)
                    WiretapButton(text = "Clear", onClick = {}, style = WiretapButtonStyle.Ghost)
                    WiretapButton(text = "Clear all", onClick = {}, style = WiretapButtonStyle.Danger)
                    WiretapButton(text = "+ Rule", onClick = {}, size = WiretapButtonSize.Sm)
                }
                Cluster(horizontalGap = 10.dp) {
                    WiretapButton(text = "Disabled", onClick = {}, enabled = false)
                    WiretapFab(onClick = {}, contentDescription = "Add")
                }
            }
        }
        DSCard("Filter chips", "Toggle on/off · removable — click them") {
            val on = remember { mutableStateMapOf("GET" to true, "2xx" to true) }
            var hostOn by remember { mutableStateOf(true) }
            Stack(gap = 12.dp) {
                Cluster(horizontalGap = 6.dp) {
                    listOf("GET", "POST", "PUT", "DELETE").forEach { key ->
                        val selected = on[key] == true
                        WiretapChip(
                            label = key,
                            selected = selected,
                            onClick = { on[key] = !selected },
                        )
                    }
                }
                Cluster(horizontalGap = 6.dp) {
                    listOf("2xx", "3xx", "4xx", "5xx").forEach { key ->
                        val selected = on[key] == true
                        WiretapChip(
                            label = key,
                            selected = selected,
                            onClick = { on[key] = !selected },
                        )
                    }
                    if (hostOn) {
                        WiretapChip(
                            label = "api.beacon.dev",
                            selected = true,
                            onClick = { hostOn = !hostOn },
                            onRemove = { hostOn = false },
                        )
                    }
                }
            }
        }
        DSCard("Switch & checkbox", "Live toggles incl. indeterminate") {
            var sw by remember { mutableStateOf(true) }
            var sw2 by remember { mutableStateOf(false) }
            var cb by remember { mutableStateOf(true) }
            var cb2 by remember { mutableStateOf(false) }
            Cluster(horizontalGap = 28.dp) {
                LabeledItem("switch · on/off") {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        WiretapSwitch(checked = sw, onCheckedChange = { sw = it })
                        WiretapSwitch(checked = sw2, onCheckedChange = { sw2 = it })
                    }
                }
                LabeledItem("checkbox") {
                    WiretapCheckbox(checked = cb, onCheckedChange = { cb = it }, label = "Mock response")
                }
                LabeledItem("unchecked") {
                    WiretapCheckbox(checked = cb2, onCheckedChange = { cb2 = it }, label = "Throttle")
                }
                LabeledItem("indeterminate") {
                    WiretapCheckbox(
                        checked = false,
                        onCheckedChange = {},
                        label = "Some headers",
                        indeterminate = true,
                    )
                }
            }
        }
        DSCard("Search field", "Type to see the clear affordance appear") {
            var q by remember { mutableStateOf("") }
            SearchField(
                value = q,
                onValueChange = { q = it },
                placeholder = "Search URL, host, headers, body…",
            )
        }
        DSCard("Form fields", "Text · mono · textarea — focus to see the ring") {
            var name by remember { mutableStateOf("Mock tasks list") }
            var pattern by remember { mutableStateOf("/v2/tasks") }
            var body by remember { mutableStateOf("{\n  \"ok\": true\n}") }
            Stack(gap = 14.dp) {
                WiretapTextField(value = name, onValueChange = { name = it }, label = "Rule name")
                WiretapTextField(value = pattern, onValueChange = { pattern = it }, label = "URL pattern", mono = true)
                WiretapTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = "Response body",
                    mono = true,
                    minLines = 3,
                )
            }
        }
        DSCard("Icon buttons", "32px tap targets · with notification badge") {
            Cluster(horizontalGap = 8.dp) {
                WiretapIconButton(onClick = {}) { Icon(WiretapIcons.Search, null, modifier = Modifier.size(16.dp)) }
                WiretapIconButton(onClick = {}, badgeCount = 3) { Icon(WiretapIcons.Filter, null, modifier = Modifier.size(16.dp)) }
                WiretapIconButton(onClick = {}) { Icon(WiretapIcons.Share, null, modifier = Modifier.size(16.dp)) }
                WiretapIconButton(onClick = {}) { Icon(WiretapIcons.Trash, null, modifier = Modifier.size(16.dp)) }
                WiretapIconButton(onClick = {}) { Icon(WiretapIcons.More, null, modifier = Modifier.size(16.dp)) }
            }
        }
    }
}
