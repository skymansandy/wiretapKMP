/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.ui.screens.http.detail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.domain.model.ResponseSource
import dev.skymansandy.wiretap.ui.theme.ruleColor

@Composable
internal fun RuleMatchBanner(
    modifier: Modifier = Modifier,
    source: ResponseSource,
    matchedRuleId: Long?,
    onViewRule: ((ruleId: Long) -> Unit)?,
) {
    if (source == ResponseSource.Network) return

    val contentColor = source.ruleColor
    val bgColor = contentColor.copy(alpha = 0.15f)
    val label = when (source) {
        ResponseSource.Mock -> "Mocked by rule"
        ResponseSource.Throttle -> "Throttled by rule"
        ResponseSource.MockAndThrottle -> "Mocked + throttled by rule"
        ResponseSource.Network -> return
    }

    val clickable = matchedRuleId != null && onViewRule != null

    Row(
        modifier = modifier
            .background(bgColor)
            .then(
                when {
                    clickable -> Modifier.clickable { onViewRule.invoke(matchedRuleId) }
                    else -> Modifier
                },
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
        )

        if (clickable) {
            Text(
                text = "⚡ View Rule",
                style = MaterialTheme.typography.labelMedium,
                color = contentColor,
            )
        }
    }
}

@Preview
@Composable
private fun Preview_RuleMatchBannerMock() {
    MaterialTheme {
        RuleMatchBanner(
            source = ResponseSource.Mock,
            matchedRuleId = 1,
            onViewRule = {},
        )
    }
}

@Preview
@Composable
private fun Preview_RuleMatchBannerThrottle() {
    MaterialTheme {
        RuleMatchBanner(
            source = ResponseSource.Throttle,
            matchedRuleId = 2,
            onViewRule = {},
        )
    }
}
