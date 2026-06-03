package dev.skymansandy.wiretapsample.ui.design.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.design.component.button.WiretapButton
import dev.skymansandy.wiretap.design.component.button.WiretapButtonStyle
import dev.skymansandy.wiretap.design.component.input.WiretapChip
import dev.skymansandy.wiretap.design.component.overlay.ConfirmDialog
import dev.skymansandy.wiretap.design.component.overlay.EmptyState
import dev.skymansandy.wiretap.design.component.overlay.Stepper
import dev.skymansandy.wiretap.design.component.overlay.WiretapBottomSheet
import dev.skymansandy.wiretap.design.theme.WiretapDesign

@Composable
fun OverlaysSection() {
    DSSectionHeader("Overlays & flows")
    Stack(gap = 18.dp) {
        DSCard("Bottom sheet", "Filter sheet — opens over the screen") {
            BottomSheetDemo()
        }
        DSCard("Confirmation dialog", "Danger variant — quantifies the loss") {
            ConfirmDialogDemo()
        }
        DSCard("Step indicator", "Create-rule flow — advance with Next") {
            StepperDemo()
        }
        DSCard("Empty state", "Names the gap · gives the literal command") {
            MockScreen(height = 320.dp) {
                EmptyState(
                    title = "No HTTP traffic yet",
                    description = "Point your client at the Wiretap proxy and start a request.",
                    setupCommand = "$ export HTTPS_PROXY=http://localhost:8765",
                )
            }
        }
    }
}

@Composable
private fun BottomSheetDemo() {
    var open by remember { mutableStateOf(false) }
    val chips = remember { mutableMapOf("2xx" to true, "GET" to true) }
    val c = WiretapDesign.colors
    Box(modifier = Modifier) {
        MockScreen(height = 280.dp) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                WiretapButton(text = "Open filter sheet", onClick = { open = true })
                Box(modifier = Modifier.padding(top = 12.dp)) {
                    androidx.compose.material3.Text(
                        text = "slides up · tap backdrop to dismiss",
                        style = WiretapDesign.typography.monoMeta,
                        color = c.fg4,
                    )
                }
            }
            WiretapBottomSheet(
                open = open,
                onDismiss = { open = false },
                title = "Filter",
                footer = {
                    androidx.compose.foundation.layout.Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        WiretapButton(text = "Clear all", onClick = {}, style = WiretapButtonStyle.Ghost)
                        WiretapButton(text = "Apply", onClick = { open = false })
                    }
                },
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    SheetCategory("Method", listOf("GET", "POST", "PUT", "DELETE"), chips)
                    SheetCategory("Status", listOf("2xx", "3xx", "4xx", "5xx"), chips)
                }
            }
        }
    }
}

@Composable
private fun SheetCategory(
    label: String,
    keys: List<String>,
    state: MutableMap<String, Boolean>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        androidx.compose.material3.Text(
            text = label.uppercase(),
            style = WiretapDesign.typography.micro,
            color = WiretapDesign.colors.fg3,
        )
        Cluster(horizontalGap = 6.dp) {
            keys.forEach { key ->
                val on = state[key] == true
                WiretapChip(label = key, selected = on, onClick = { state[key] = !on })
            }
        }
    }
}

@Composable
private fun ConfirmDialogDemo() {
    var open by remember { mutableStateOf(false) }
    val c = WiretapDesign.colors
    MockScreen(height = 240.dp) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            WiretapButton(
                text = "Clear all logs…",
                onClick = { open = true },
                style = WiretapButtonStyle.Danger,
            )
            Box(modifier = Modifier.padding(top = 12.dp)) {
                androidx.compose.material3.Text(
                    text = "centered · backdrop dims the screen",
                    style = WiretapDesign.typography.monoMeta,
                    color = c.fg4,
                )
            }
        }
        ConfirmDialog(
            open = open,
            title = "Clear all HTTP logs?",
            description = "This removes all 13 captured HTTP entries. This action can't be undone.",
            confirmLabel = "Clear all",
            cancelLabel = "Cancel",
            danger = true,
            onConfirm = { open = false },
            onCancel = { open = false },
        )
    }
}

@Composable
private fun StepperDemo() {
    var step by remember { mutableStateOf(0) }
    val labels = listOf("Criteria", "Action")
    Stack(gap = 14.dp) {
        Stepper(labels = labels, current = step, modifier = Modifier.background(WiretapDesign.colors.background))
        Cluster(horizontalGap = 8.dp) {
            WiretapButton(
                text = "Back",
                onClick = { step = (step - 1).coerceAtLeast(0) },
                style = WiretapButtonStyle.Ghost,
                enabled = step > 0,
            )
            WiretapButton(
                text = if (step == labels.lastIndex) "Create Rule" else "Next",
                onClick = { step = (step + 1).coerceAtMost(labels.lastIndex) },
            )
        }
    }
}
