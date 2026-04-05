/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.ui.screens.rules.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.domain.model.RuleAction
import dev.skymansandy.wiretap.ui.theme.ruleColor

@Composable
internal fun ActionBadge(
    modifier: Modifier = Modifier,
    action: RuleAction,
) {
    val color = action.type.ruleColor

    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.15f),
        shape = MaterialTheme.shapes.extraSmall,
    ) {
        Text(
            text = action.type.label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

@Preview
@Composable
private fun Preview_ActionBadgeMock() {
    MaterialTheme {
        ActionBadge(action = RuleAction.Mock())
    }
}

@Preview
@Composable
private fun Preview_ActionBadgeThrottle() {
    MaterialTheme {
        ActionBadge(action = RuleAction.Throttle())
    }
}
