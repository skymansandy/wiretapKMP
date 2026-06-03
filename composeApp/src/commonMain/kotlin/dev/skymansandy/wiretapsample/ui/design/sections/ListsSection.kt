package dev.skymansandy.wiretapsample.ui.design.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.design.component.list.LogRow
import dev.skymansandy.wiretap.design.component.list.MessageBubble
import dev.skymansandy.wiretap.design.component.list.MessageDirection
import dev.skymansandy.wiretap.design.component.list.RuleRow
import dev.skymansandy.wiretap.design.theme.WiretapDesign

@Composable
fun ListsSection() {
    DSSectionHeader("Lists & rows")
    Stack(gap = 18.dp) {
        DSCard("HTTP log row", "The core list row — click to select") {
            var selected by remember { mutableStateOf(0) }
            val c = WiretapDesign.colors
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, c.border1, RoundedCornerShape(10.dp)),
            ) {
                SampleLogRows.forEachIndexed { idx, row ->
                    LogRow(data = row, selected = idx == selected, onClick = { selected = idx })
                }
            }
        }
        DSCard("Rule row", "Working switch · enabled/disabled states") {
            val enabled = remember { mutableStateMapOf(0 to true, 1 to false) }
            val c = WiretapDesign.colors
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, c.border1, RoundedCornerShape(10.dp)),
            ) {
                SampleRuleRows.forEachIndexed { idx, rule ->
                    RuleRow(
                        data = rule,
                        enabled = enabled[idx] == true,
                        onToggle = { enabled[idx] = it },
                    )
                }
            }
        }
        DSCard("Message bubbles", "WebSocket transcript — sent (accent) / received") {
            val c = WiretapDesign.colors
            Column(modifier = Modifier.fillMaxWidth().background(c.background, RoundedCornerShape(10.dp))) {
                MessageBubble(
                    direction = MessageDirection.Sent,
                    content = """{"op":"subscribe","ch":"tasks"}""",
                    timestamp = "14:03:02.118",
                    size = "64 B",
                )
                MessageBubble(
                    direction = MessageDirection.Received,
                    content = """{"event":"task.created","id":"tsk_8h2"}""",
                    timestamp = "14:03:02.640",
                    size = "212 B",
                )
            }
        }
    }
}
