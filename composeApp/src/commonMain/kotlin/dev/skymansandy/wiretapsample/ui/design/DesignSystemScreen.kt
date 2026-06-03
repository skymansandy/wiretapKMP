package dev.skymansandy.wiretapsample.ui.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.design.theme.WiretapAccent
import dev.skymansandy.wiretap.design.theme.WiretapDensity
import dev.skymansandy.wiretap.design.theme.WiretapDesign
import dev.skymansandy.wiretap.design.theme.WiretapDesignTheme
import dev.skymansandy.wiretapsample.ui.design.sections.ActionsSection
import dev.skymansandy.wiretapsample.ui.design.sections.BadgesSection
import dev.skymansandy.wiretapsample.ui.design.sections.ColorsSection
import dev.skymansandy.wiretapsample.ui.design.sections.DetailSection
import dev.skymansandy.wiretapsample.ui.design.sections.IconographySection
import dev.skymansandy.wiretapsample.ui.design.sections.ListsSection
import dev.skymansandy.wiretapsample.ui.design.sections.NavigationSection
import dev.skymansandy.wiretapsample.ui.design.sections.OverlaysSection
import dev.skymansandy.wiretapsample.ui.design.sections.SpacingSection
import dev.skymansandy.wiretapsample.ui.design.sections.TypeSection

/**
 * Live, in-app showcase of the Wiretap design system. Mirrors the section
 * order of the HTML reference (Design System.html → ds-foundations.jsx +
 * ds-components.jsx). Accent + density controls at the top re-theme the
 * whole screen instantly via [WiretapDesignTheme].
 *
 * The screen wraps itself in its own [WiretapDesignTheme] so the rest of the
 * sample app (Material3) stays unaffected.
 */
@Composable
fun DesignSystemScreen(modifier: Modifier = Modifier) {
    var accent by rememberSaveable { mutableStateOf(WiretapAccent.Cyan) }
    var density by rememberSaveable { mutableStateOf(WiretapDensity.Regular) }

    WiretapDesignTheme(accent = accent, density = density) {
        val c = WiretapDesign.colors
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(c.pageBackdrop)
                .verticalScroll(rememberScrollState()),
        ) {
            DSTopBar(
                accent = accent,
                onAccent = { accent = it },
                density = density,
                onDensity = { density = it },
            )

            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                DesignSystemHero()
                ColorsSection()
                TypeSection()
                SpacingSection()
                IconographySection()
                ActionsSection()
                BadgesSection()
                NavigationSection()
                ListsSection()
                DetailSection()
                OverlaysSection()
                Box(Modifier.fillMaxWidth().padding(vertical = 40.dp)) {
                    Text(
                        text = "WIRETAP · dark theme · IBM Plex Sans + Mono · accent oklch(74% 0.135 230)",
                        style = WiretapDesign.typography.monoMeta,
                        color = c.fg4,
                    )
                }
            }
        }
    }
}

@Composable
private fun DSTopBar(
    accent: WiretapAccent,
    onAccent: (WiretapAccent) -> Unit,
    density: WiretapDensity,
    onDensity: (WiretapDensity) -> Unit,
) {
    val c = WiretapDesign.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.pageBackdrop.copy(alpha = 0.95f))
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            // Brand mark — small accent square with a centered hole, matching .ds-logo
            Box(
                Modifier
                    .size(26.dp)
                    .background(c.accent, WiretapDesign.shapes.sm),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier
                        .size(10.dp)
                        .background(c.pageBackdrop, WiretapDesign.shapes.xs),
                )
            }
            Text("WIRETAP", style = WiretapDesign.typography.wordmark, color = c.fg1)
            Text("design system", style = WiretapDesign.typography.monoMeta, color = c.fg3)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            AccentSwitcher(accent = accent, onAccent = onAccent)
            DensitySwitcher(density = density, onDensity = onDensity)
        }
    }
}

@Composable
private fun AccentSwitcher(accent: WiretapAccent, onAccent: (WiretapAccent) -> Unit) {
    val c = WiretapDesign.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("ACCENT", style = WiretapDesign.typography.micro, color = c.fg4)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            WiretapAccent.values().forEach { variant ->
                val colors = remember(variant) { variant.toColors() }
                val isOn = variant == accent
                Box(
                    Modifier
                        .size(22.dp)
                        .background(colors.base, CircleShape)
                        .border(2.dp, if (isOn) c.fg1 else Color.Transparent, CircleShape)
                        .clickable { onAccent(variant) },
                )
            }
        }
    }
}

@Composable
private fun DensitySwitcher(density: WiretapDensity, onDensity: (WiretapDensity) -> Unit) {
    val c = WiretapDesign.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("DENSITY", style = WiretapDesign.typography.micro, color = c.fg4)
        Row(
            modifier = Modifier
                .background(c.surface1, WiretapDesign.shapes.pill)
                .border(1.dp, c.border1, WiretapDesign.shapes.pill)
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            WiretapDensity.values().forEach { d ->
                val isOn = d == density
                Box(
                    Modifier
                        .clickable { onDensity(d) }
                        .background(if (isOn) c.surface3 else Color.Transparent, WiretapDesign.shapes.pill)
                        .padding(horizontal = 11.dp, vertical = 5.dp),
                ) {
                    Text(
                        text = d.name.lowercase(),
                        style = WiretapDesign.typography.monoMeta,
                        color = if (isOn) c.fg1 else c.fg3,
                    )
                }
            }
        }
    }
}

@Composable
private fun DesignSystemHero() {
    val c = WiretapDesign.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 36.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Wiretap Design System",
            style = WiretapDesign.typography.title.copy(
                fontSize = androidx.compose.ui.unit.TextUnit(34f, androidx.compose.ui.unit.TextUnitType.Sp),
            ),
            color = c.fg1,
        )
        Text(
            text = "A dark, terminal-adjacent system for a cross-platform network inspector. " +
                "Every component below is live — toggle switches, open sheets, step through " +
                "flows, search the JSON — and re-themes instantly with the accent and density " +
                "controls above.",
            style = WiretapDesign.typography.body,
            color = c.fg2,
        )
    }
}
