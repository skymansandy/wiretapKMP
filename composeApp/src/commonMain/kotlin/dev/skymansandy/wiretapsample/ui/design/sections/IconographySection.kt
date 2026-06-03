package dev.skymansandy.wiretapsample.ui.design.sections

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.design.component.icon.WiretapIconCatalog
import dev.skymansandy.wiretap.design.theme.WiretapDesign

@Composable
fun IconographySection() {
    DSSectionHeader("Iconography")
    DSCard(
        title = "Icon set",
        subtitle = "Material Icons Extended, tinted via LocalContentColor — inherit the theme",
    ) {
        val c = WiretapDesign.colors
        Cluster(horizontalGap = 8.dp, verticalGap = 8.dp) {
            WiretapIconCatalog.forEach { entry ->
                Column(
                    modifier = Modifier
                        .size(width = 76.dp, height = 64.dp)
                        .border(1.dp, c.border1, RoundedCornerShape(8.dp))
                        .padding(vertical = 14.dp, horizontal = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = entry.vector,
                        contentDescription = entry.name,
                        tint = c.fg2,
                        modifier = Modifier.size(20.dp),
                    )
                    DSCap(entry.name)
                }
            }
        }
    }
}
