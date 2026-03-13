package dev.skymansandy.wiretap.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.model.MatcherType
import dev.skymansandy.wiretap.model.RuleAction
import dev.skymansandy.wiretap.model.WiretapRule
import dev.skymansandy.wiretap.repository.RuleRepository

@Composable
internal fun RulesListScreen(
    ruleRepository: RuleRepository,
    searchQuery: String = "",
    onRuleClick: (WiretapRule) -> Unit,
    onCreateClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rulesFlow = remember(searchQuery) {
        if (searchQuery.isBlank()) ruleRepository.getAll() else ruleRepository.search(searchQuery)
    }
    val rules by rulesFlow.collectAsState(initial = emptyList())

    Box(modifier = modifier) {
        if (rules.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    if (searchQuery.isBlank()) "No rules yet" else "No rules match \"$searchQuery\"",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(rules, key = { it.id }) { rule ->
                    RuleItem(
                        rule = rule,
                        searchQuery = searchQuery,
                        onClick = { onRuleClick(rule) },
                        onToggle = { enabled ->
                            ruleRepository.setEnabled(rule.id, enabled)
                        },
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = onCreateClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        ) {
            Icon(Icons.Default.Add, contentDescription = "Create rule")
        }
    }
}

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
        Column(modifier = Modifier.weight(1f)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MatcherTypeBadge(rule.matcherType)
                ActionBadge(rule.action)
                if (rule.method != "*") {
                    MethodBadge(rule.method)
                }
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = highlightText(rule.urlPattern, searchQuery),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            if (rule.action == RuleAction.MOCK && rule.mockResponseCode != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Response: ${rule.mockResponseCode}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (rule.action == RuleAction.THROTTLE && rule.throttleDelayMs != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Delay: ${rule.throttleDelayMs} ms",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
private fun MatcherTypeBadge(type: MatcherType) {
    val label = when (type) {
        MatcherType.URL_EXACT -> "EXACT"
        MatcherType.URL_REGEX -> "REGEX"
        MatcherType.HEADER_CONTAINS -> "HEADER"
        MatcherType.BODY_CONTAINS -> "BODY"
    }
    val color = when (type) {
        MatcherType.URL_EXACT -> MaterialTheme.colorScheme.primary
        MatcherType.URL_REGEX -> MaterialTheme.colorScheme.tertiary
        MatcherType.HEADER_CONTAINS -> MaterialTheme.colorScheme.secondary
        MatcherType.BODY_CONTAINS -> MaterialTheme.colorScheme.secondary
    }
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = MaterialTheme.shapes.extraSmall,
    ) {
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
internal fun ActionBadge(action: RuleAction) {
    val color = when (action) {
        RuleAction.MOCK -> MaterialTheme.colorScheme.error
        RuleAction.THROTTLE -> MaterialTheme.colorScheme.tertiary
    }
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = MaterialTheme.shapes.extraSmall,
    ) {
        Text(
            text = action.name,
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
