/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.design.component.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.design.component.badge.MethodBadge
import dev.skymansandy.wiretap.design.component.badge.TagBadge
import dev.skymansandy.wiretap.design.component.badge.TagKind
import dev.skymansandy.wiretap.design.component.input.WiretapSwitch
import dev.skymansandy.wiretap.design.foundation.HttpMethod
import dev.skymansandy.wiretap.design.theme.WiretapDesign

data class RuleTag(val label: String, val kind: TagKind = TagKind.Default)

data class RuleRowData(
    val method: HttpMethod? = null,
    val criteria: List<RuleTag>,
    val action: RuleTag,
    val pattern: String,
    val actionDetail: String,
)

@Composable
fun RuleRow(
    data: RuleRowData,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = WiretapDesign.colors
    Box(modifier = modifier.fillMaxWidth().background(c.background)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .alpha(if (enabled) 1f else 0.5f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (data.method != null) MethodBadge(data.method)
                    data.criteria.forEach { tag -> TagBadge(tag.label, tag.kind) }
                    TagBadge(data.action.label, data.action.kind)
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = data.pattern,
                        style = WiretapDesign.typography.monoMeta,
                        color = c.fg2,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = data.actionDetail,
                        style = WiretapDesign.typography.monoMeta,
                        color = c.fg3,
                    )
                }
            }
            WiretapSwitch(checked = enabled, onCheckedChange = onToggle)
        }
        HorizontalDivider(
            color = c.border1,
            thickness = 1.dp,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}
