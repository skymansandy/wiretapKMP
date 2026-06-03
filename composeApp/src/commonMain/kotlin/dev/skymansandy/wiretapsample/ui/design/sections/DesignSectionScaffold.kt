package dev.skymansandy.wiretapsample.ui.design.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.design.theme.WiretapDesign

@Composable
fun DSSectionHeader(title: String, modifier: Modifier = Modifier) {
    val c = WiretapDesign.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 40.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(text = title.uppercase(), style = WiretapDesign.typography.micro, color = c.fg2)
        HorizontalDivider(color = c.border1, modifier = Modifier.weight(1f))
    }
}

@Composable
fun DSCard(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    body: @Composable () -> Unit,
) {
    val c = WiretapDesign.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(c.surface0, WiretapDesign.shapes.lg)
            .border(1.dp, c.border1, WiretapDesign.shapes.lg)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(text = title, style = WiretapDesign.typography.title, color = c.fg1)
            if (subtitle != null) {
                Text(text = subtitle, style = WiretapDesign.typography.body, color = c.fg3)
            }
        }
        body()
    }
}

@Composable
fun DSCap(text: String, modifier: Modifier = Modifier, primary: Boolean = false) {
    Text(
        text = text,
        style = WiretapDesign.typography.monoMeta,
        color = if (primary) WiretapDesign.colors.fg2 else WiretapDesign.colors.fg4,
        modifier = modifier,
    )
}

@Composable
fun Cluster(
    modifier: Modifier = Modifier,
    horizontalGap: Dp = 12.dp,
    verticalGap: Dp = 12.dp,
    content: @Composable () -> Unit,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(horizontalGap),
        verticalArrangement = Arrangement.spacedBy(verticalGap),
    ) { content() }
}

@Composable
fun Stack(
    modifier: Modifier = Modifier,
    gap: Dp = 12.dp,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(gap)) { content() }
}

@Composable
fun LabeledItem(
    label: String,
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        content()
        DSCap(label)
    }
}

@Composable
fun MockScreen(
    height: Dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val c = WiretapDesign.colors
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(c.background, WiretapDesign.shapes.md)
            .border(1.dp, c.border1, WiretapDesign.shapes.md)
            .clipToBounds(),
    ) { content() }
}
