package dev.skymansandy.wiretapsample.ui.design.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.skymansandy.wiretap.design.component.badge.MethodBadge
import dev.skymansandy.wiretap.design.component.badge.SourceDot
import dev.skymansandy.wiretap.design.component.badge.StatusBadge
import dev.skymansandy.wiretap.design.component.badge.TagBadge
import dev.skymansandy.wiretap.design.component.badge.TagKind
import dev.skymansandy.wiretap.design.foundation.HttpMethod
import dev.skymansandy.wiretap.design.foundation.LogSource
import dev.skymansandy.wiretap.design.foundation.statusReasonFor
import dev.skymansandy.wiretap.design.theme.WiretapAccent
import dev.skymansandy.wiretap.design.theme.WiretapDesign

@Composable
fun ColorsSection() {
    DSSectionHeader("Colors")
    Stack(gap = 18.dp) {
        DSCard("Surfaces", "Near-black ramp — elevation by lightness step, not shadow") {
            val c = WiretapDesign.colors
            Cluster {
                Swatch("page", "#050608", c.pageBackdrop)
                Swatch("bg", "#0A0C10", c.background)
                Swatch("surface-0", "#0E1117", c.surface0)
                Swatch("surface-1", "#12161D", c.surface1)
                Swatch("surface-2", "#181D26", c.surface2)
                Swatch("surface-3", "#1F2531", c.surface3)
                Swatch("surface-4", "#262D3A", c.surface4)
            }
        }
        DSCard("Borders", "Hairline 1px ramp + accent focus ring") {
            val c = WiretapDesign.colors
            Cluster {
                Swatch("border-1", "#1D232D", c.border1)
                Swatch("border-2", "#262D39", c.border2)
                Swatch("border-3", "#353D4B", c.border3)
                Swatch("focus", "accent / .5", c.borderFocus)
            }
        }
        DSCard("Text", "Four-step foreground ramp on --bg") {
            val c = WiretapDesign.colors
            Stack(gap = 10.dp) {
                TextSwatch("Primary", "fg-1 · #ECF0F5", c.fg1)
                TextSwatch("Secondary", "fg-2 · #A4ADBC", c.fg2)
                TextSwatch("Tertiary", "fg-3 · #6C7686", c.fg3)
                TextSwatch("Faint", "fg-4 · #4A525F", c.fg4)
            }
        }
        DSCard("Accent", "Cyan default + 3 swaps — matched L/C") {
            Cluster {
                WiretapAccent.values().forEach { variant ->
                    val colors = variant.toColors()
                    Swatch(variant.name.lowercase(), oklchLabel(variant), colors.base)
                }
                val c = WiretapDesign.colors
                Swatch("accent-soft", "live / .14", c.accentSoft)
                Swatch("accent-line", "live / .35", c.accentLine)
            }
        }
        DSCard("HTTP method colors", "Each method owns a hue; badge = 18% fill + 30% border") {
            Cluster(horizontalGap = 6.dp) {
                HttpMethod.values().forEach { method -> MethodBadge(method) }
            }
        }
        DSCard("Status class colors", "Keyed by first digit (1xx–5xx)") {
            Cluster(horizontalGap = 6.dp) {
                listOf(100, 200, 304, 401, 429, 500).forEach { code ->
                    StatusBadge(status = code, reason = statusReasonFor(code))
                }
            }
        }
        DSCard("Action & source colors", "Mock (green) / throttle (amber) + source dots") {
            Stack(gap = 14.dp) {
                Cluster(horizontalGap = 8.dp) {
                    TagBadge("Mock", TagKind.Mock)
                    TagBadge("Throttle", TagKind.Throttle)
                    TagBadge("Matched", TagKind.Accent)
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

@Composable
private fun Swatch(name: String, value: String, color: Color) {
    val c = WiretapDesign.colors
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Box(
            Modifier
                .size(width = 96.dp, height = 54.dp)
                .background(color, RoundedCornerShape(8.dp))
                .border(1.dp, c.border2, RoundedCornerShape(8.dp)),
        )
        DSCap(name, primary = true)
        DSCap(value)
    }
}

@Composable
private fun TextSwatch(label: String, value: String, color: Color) {
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = label,
            style = WiretapDesign.typography.body.copy(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
            color = color,
            modifier = Modifier.width(110.dp),
        )
        DSCap(value)
    }
}

private fun oklchLabel(v: WiretapAccent): String = when (v) {
    WiretapAccent.Cyan -> "74% .135 230"
    WiretapAccent.Violet -> "72% .13 295"
    WiretapAccent.Green -> "74% .13 150"
    WiretapAccent.Amber -> "78% .13 80"
}
