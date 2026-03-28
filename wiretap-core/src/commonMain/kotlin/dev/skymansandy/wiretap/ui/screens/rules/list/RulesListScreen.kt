package dev.skymansandy.wiretap.ui.screens.rules.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.skymansandy.wiretap.domain.model.RuleAction
import dev.skymansandy.wiretap.domain.model.WiretapRule
import dev.skymansandy.wiretap.domain.model.matchers.BodyMatcher
import dev.skymansandy.wiretap.domain.model.matchers.UrlMatcher
import dev.skymansandy.wiretap.helper.util.highlightText
import dev.skymansandy.wiretap.navigation.api.WiretapScreen
import dev.skymansandy.wiretap.navigation.api.WiretapScreen.CreateRuleScreen
import dev.skymansandy.wiretap.navigation.compose.LocalWiretapNavigator
import dev.skymansandy.wiretap.ui.common.StatusText
import dev.skymansandy.wiretap.ui.screens.rules.components.ActionBadge

@Composable
internal fun RulesListScreen(
    modifier: Modifier = Modifier,
    viewModel: RulesListViewModel,
) {
    val navigator = LocalWiretapNavigator.current
    val rules by viewModel.rules.collectAsStateWithLifecycle()
    val searchQuery by viewModel.debouncedQuery.collectAsStateWithLifecycle()

    Box(modifier = modifier) {
        if (rules.isEmpty()) {
            StatusText(
                modifier = Modifier.fillMaxSize(),
                text = when {
                    searchQuery.isBlank() -> "No rules yet"
                    else -> "No rules match \"$searchQuery\""
                },
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
            ) {
                items(
                    items = rules,
                    key = { it.id },
                ) { rule ->
                    RuleItem(
                        rule = rule,
                        searchQuery = searchQuery,
                        onToggle = { viewModel.setEnabled(rule.id, it) },
                        onClick = {
                            navigator.pushDetailPane(
                                WiretapScreen.RuleDetailScreen(rule.id),
                            )
                        },
                    )
                }
            }
        }

        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            onClick = {
                navigator.pushDetailPane(CreateRuleScreen())
            },
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Create rule",
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RuleItem(
    rule: WiretapRule,
    searchQuery: String,
    onClick: () -> Unit,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (rule.method != "*") MethodBadge(rule.method)

                if (rule.urlMatcher != null) MatcherBadge(
                    label = urlBadgeLabel(rule.urlMatcher),
                    color = MaterialTheme.colorScheme.primary,
                )

                if (rule.headerMatchers.isNotEmpty()) MatcherBadge(
                    label = if (rule.headerMatchers.size == 1) "HDR" else "HDR\u00D7${rule.headerMatchers.size}",
                    color = MaterialTheme.colorScheme.secondary,
                )

                if (rule.bodyMatcher != null) MatcherBadge(
                    label = bodyBadgeLabel(rule.bodyMatcher),
                    color = MaterialTheme.colorScheme.tertiary,
                )

                ActionBadge(action = rule.action)
            }

            Spacer(Modifier.height(4.dp))

            val subtitle = rule.urlMatcher?.pattern
                ?: rule.headerMatchers.firstOrNull()?.key
                ?: rule.bodyMatcher?.pattern
                ?: ""

            if (subtitle.isNotBlank()) {
                Text(
                    text = highlightText(subtitle, searchQuery),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            when (val action = rule.action) {
                is RuleAction.Mock -> {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Response: ${action.responseCode}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                is RuleAction.Throttle -> {
                    Spacer(Modifier.height(2.dp))
                    val delayText =
                        if (action.delayMaxMs != null && action.delayMaxMs != action.delayMs)
                            "Delay: ${action.delayMs}\u2013${action.delayMaxMs} ms"
                        else "Delay: ${action.delayMs} ms"
                    Text(
                        text = delayText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Switch(
            checked = rule.enabled,
            onCheckedChange = onToggle,
            modifier = Modifier.size(40.dp),
        )
    }

    HorizontalDivider()
}

@Composable
private fun MatcherBadge(
    label: String,
    color: Color,
) {
    Surface(color = color.copy(alpha = 0.15f), shape = MaterialTheme.shapes.extraSmall) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

@Composable
private fun MethodBadge(method: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.extraSmall,
    ) {
        Text(
            text = method,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

private fun urlBadgeLabel(matcher: UrlMatcher) = when (matcher) {
    is UrlMatcher.Exact -> "URL"
    is UrlMatcher.Contains -> "URL~"
    is UrlMatcher.Regex -> "URL*"
}

private fun bodyBadgeLabel(matcher: BodyMatcher) = when (matcher) {
    is BodyMatcher.Exact -> "BODY"
    is BodyMatcher.Contains -> "BODY~"
    is BodyMatcher.Regex -> "BODY*"
}

@Preview
@Composable
private fun Preview_RuleItemMock() {
    MaterialTheme {
        RuleItem(
            rule = WiretapRule(
                id = 1,
                method = "GET",
                urlMatcher = UrlMatcher.Contains("/api/users"),
                action = RuleAction.Mock(responseCode = 200),
                enabled = true,
            ),
            searchQuery = "",
            onClick = {},
            onToggle = {},
        )
    }
}

@Preview
@Composable
private fun Preview_RuleItemThrottle() {
    MaterialTheme {
        RuleItem(
            rule = WiretapRule(
                id = 2,
                method = "*",
                urlMatcher = UrlMatcher.Regex("/api/v\\d+/.*"),
                action = RuleAction.Throttle(delayMs = 1000, delayMaxMs = 3000),
                enabled = false,
            ),
            searchQuery = "",
            onClick = {},
            onToggle = {},
        )
    }
}
