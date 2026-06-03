package dev.skymansandy.wiretapsample.ui.design.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.design.theme.WiretapDensity
import dev.skymansandy.wiretap.design.theme.WiretapDesign

@Composable
fun SpacingSection() {
    DSSectionHeader("Spacing & elevation")
    Stack(gap = 18.dp) {
        DSCard("Radii", "4 / 6 / 10 / 14 / 20 / pill") {
            Cluster {
                RadiusBox("r-xs", 4.dp, RoundedCornerShape(4.dp))
                RadiusBox("r-sm", 6.dp, RoundedCornerShape(6.dp))
                RadiusBox("r-md", 10.dp, RoundedCornerShape(10.dp))
                RadiusBox("r-lg", 14.dp, RoundedCornerShape(14.dp))
                RadiusBox("r-xl", 20.dp, RoundedCornerShape(20.dp))
                RadiusBox("pill", 0.dp, RoundedCornerShape(50), label = "999px")
            }
        }
        DSCard("Elevation", "Shadows for overlays only — flat elements use borders") {
            val c = WiretapDesign.colors
            Cluster(horizontalGap = 24.dp) {
                LabeledItem("menu") {
                    Box(
                        Modifier
                            .size(width = 96.dp, height = 60.dp)
                            .shadow(WiretapDesign.elevation.menu, RoundedCornerShape(10.dp))
                            .background(c.surface2, RoundedCornerShape(10.dp))
                            .border(1.dp, c.border2, RoundedCornerShape(10.dp)),
                    )
                }
                LabeledItem("dialog") {
                    Box(
                        Modifier
                            .size(width = 96.dp, height = 60.dp)
                            .shadow(WiretapDesign.elevation.dialog, RoundedCornerShape(14.dp))
                            .background(c.surface2, RoundedCornerShape(14.dp))
                            .border(1.dp, c.border2, RoundedCornerShape(14.dp)),
                    )
                }
                LabeledItem("fab") {
                    Box(
                        Modifier
                            .size(60.dp)
                            .shadow(WiretapDesign.elevation.fab, CircleShape)
                            .background(c.accent, CircleShape),
                    )
                }
            }
        }
        DSCard("Row density", "Active density: ${WiretapDesign.density.name.lowercase()}") {
            val c = WiretapDesign.colors
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(c.background, RoundedCornerShape(10.dp))
                    .border(1.dp, c.border1, RoundedCornerShape(10.dp)),
            ) {
                listOf(
                    "compact" to WiretapDensity.Compact,
                    "regular" to WiretapDensity.Regular,
                    "comfy" to WiretapDensity.Comfy,
                ).forEachIndexed { i, (n, d) ->
                    DensityRow(name = n, density = d)
                    if (i < 2) HorizontalDivider(color = c.border1)
                }
            }
        }
    }
}

@Composable
private fun RadiusBox(name: String, radius: Dp, shape: Shape, label: String = "${radius.value.toInt()}px") {
    val c = WiretapDesign.colors
    LabeledItem(name) {
        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Box(
                Modifier
                    .size(width = 84.dp, height = 56.dp)
                    .background(c.surface2, shape)
                    .border(1.dp, c.border3, shape),
            )
            DSCap(label)
        }
    }
}

@Composable
private fun DensityRow(name: String, density: WiretapDensity) {
    val c = WiretapDesign.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = density.rowPaddingVertical),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            "$name · ${density.rowPaddingVertical.value.toInt()}px",
            style = WiretapDesign.typography.code,
            color = c.fg1,
            modifier = Modifier.weight(1f),
        )
        Text("124 ms", style = WiretapDesign.typography.code, color = c.fg3)
    }
}
