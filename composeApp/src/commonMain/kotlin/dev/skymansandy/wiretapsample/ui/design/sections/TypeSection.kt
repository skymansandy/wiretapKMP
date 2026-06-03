package dev.skymansandy.wiretapsample.ui.design.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import dev.skymansandy.wiretap.design.theme.WiretapDesign

@Composable
fun TypeSection() {
    DSSectionHeader("Type")
    Stack(gap = 18.dp) {
        DSCard("Families", "IBM Plex Sans (UI) + IBM Plex Mono (machine values)") {
            val c = WiretapDesign.colors
            val t = WiretapDesign.typography
            Stack(gap = 18.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(
                        text = "IBM Plex Sans",
                        style = t.title.copy(fontSize = 30.sp, letterSpacing = (-0.01).em),
                        color = c.fg1,
                    )
                    Text(
                        text = "The quick brown fox · 0123456789",
                        style = t.body.copy(fontSize = 14.sp),
                        color = c.fg2,
                    )
                    DSCap("--font-ui · 400 500 600 700", primary = true)
                }
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(
                        text = "IBM Plex Mono",
                        style = t.monoRow.copy(fontSize = 30.sp, fontWeight = FontWeight.Medium),
                        color = c.fg1,
                    )
                    Text(
                        text = "GET /v2/tasks · 200 · 18.0 KB",
                        style = t.monoMeta.copy(fontSize = 14.sp),
                        color = c.fg2,
                    )
                    DSCap("--font-mono · 400 500 600 700", primary = true)
                }
            }
        }
        DSCard("Scale", "Title · body · labels · mono metadata") {
            val c = WiretapDesign.colors
            val t = WiretapDesign.typography
            Stack(gap = 14.dp) {
                ScaleItem("Screen title — HTTP Console", "600 15px / -0.01em · Sans", t.title, c.fg1)
                ScaleItem("Body & row text — point your client at the proxy.", "400 13px · Sans", t.body, c.fg1)
                ScaleItem("GENERAL · SECTION LABEL", "600 10px / 0.12em upper · Mono", t.micro, c.fg3)
                ScaleItem("api.beacon.dev/v2/tasks", "500 13px · Mono row", t.monoRow, c.fg1)
                ScaleItem("14:02:41.881 · 124 ms · ↓ 18.0 KB", "400 11px · Mono meta", t.monoMeta, c.fg3)
            }
        }
    }
}

@Composable
private fun ScaleItem(
    sample: String,
    caption: String,
    style: androidx.compose.ui.text.TextStyle,
    color: androidx.compose.ui.graphics.Color,
) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(text = sample, style = style, color = color)
        DSCap(caption)
    }
}
