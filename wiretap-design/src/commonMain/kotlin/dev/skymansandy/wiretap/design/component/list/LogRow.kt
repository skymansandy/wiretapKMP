/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.design.component.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.design.component.badge.MethodBadge
import dev.skymansandy.wiretap.design.component.badge.SourceDot
import dev.skymansandy.wiretap.design.component.badge.StatusBadge
import dev.skymansandy.wiretap.design.foundation.HttpMethod
import dev.skymansandy.wiretap.design.foundation.LogSource
import dev.skymansandy.wiretap.design.theme.WiretapDesign

data class LogRowData(
    val method: HttpMethod,
    val status: Int,
    val url: String,
    val timestamp: String,
    val duration: String,
    val sizes: List<String>,
    val source: LogSource,
)

@Composable
fun LogRow(
    data: LogRowData,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = WiretapDesign.colors
    val density = WiretapDesign.density
    val bg = if (selected) c.accentSoft else c.background

    Box(modifier = modifier.fillMaxWidth().background(bg).clickable(onClick = onClick)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = density.rowPaddingVertical),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MethodBadge(method = data.method)
                StatusBadge(status = data.status)
                Text(
                    text = data.url,
                    style = WiretapDesign.typography.monoRow,
                    color = c.fg1,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                SourceDot(data.source)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(text = data.timestamp, style = WiretapDesign.typography.monoMeta, color = c.fg3)
                Box(Modifier.size(3.dp).background(c.fg4, CircleShape))
                Text(text = data.duration, style = WiretapDesign.typography.monoMeta, color = c.fg3)
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                ) {
                    data.sizes.forEach { s ->
                        Text(text = s, style = WiretapDesign.typography.monoMeta, color = c.fg4)
                    }
                }
            }
        }
        if (selected) {
            Box(
                Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxHeight()
                    .width(3.dp)
                    .background(c.accent),
            )
        }
        HorizontalDivider(
            color = c.border1,
            thickness = 1.dp,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}
