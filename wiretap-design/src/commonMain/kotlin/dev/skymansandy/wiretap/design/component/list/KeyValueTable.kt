/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.design.component.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.design.theme.WiretapDesign

/**
 * Two-column key/value table. Keys render in the label color; pass [mono] true
 * to render the value in mono (matches the `dd.mono` rule in styles.css used
 * by the request/response detail panes).
 */
@Composable
fun KeyValueTable(
    rows: List<KeyValueRow>,
    modifier: Modifier = Modifier,
    labelWidth: androidx.compose.ui.unit.Dp = 130.dp,
) {
    val c = WiretapDesign.colors
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        rows.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = row.key,
                    style = WiretapDesign.typography.body,
                    color = c.fg3,
                    modifier = Modifier.width(labelWidth),
                )
                Box(modifier = Modifier.weight(1f)) {
                    if (row.valueComposable != null) {
                        row.valueComposable.invoke()
                    } else {
                        Text(
                            text = row.value.orEmpty(),
                            style = if (row.mono) {
                                WiretapDesign.typography.code
                            } else {
                                WiretapDesign.typography.body
                            },
                            color = c.fg1,
                        )
                    }
                }
            }
        }
    }
}

data class KeyValueRow(
    val key: String,
    val value: String? = null,
    val mono: Boolean = false,
    val valueComposable: (@Composable () -> Unit)? = null,
)
