package dev.skymansandy.wiretap.ui.rules

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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.skymansandy.wiretap.data.db.entity.WiretapRule
import dev.skymansandy.wiretap.domain.model.BodyMatcher
import dev.skymansandy.wiretap.domain.model.RuleAction
import dev.skymansandy.wiretap.domain.model.UrlMatcher
import dev.skymansandy.wiretap.domain.repository.RuleRepository
import dev.skymansandy.wiretap.resources.Res
import dev.skymansandy.wiretap.resources.create_rule_cd
import dev.skymansandy.wiretap.resources.delay_fixed
import dev.skymansandy.wiretap.resources.delay_range
import dev.skymansandy.wiretap.resources.hdr
import dev.skymansandy.wiretap.resources.hdr_count
import dev.skymansandy.wiretap.resources.no_rules_match
import dev.skymansandy.wiretap.resources.no_rules_yet
import dev.skymansandy.wiretap.resources.response_code_display
import dev.skymansandy.wiretap.ui.common.highlightText
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun RulesListScreen(
    modifier: Modifier = Modifier,
    ruleRepository: RuleRepository,
    searchQuery: String = "",
    onRuleClick: (WiretapRule) -> Unit,
    onCreateClick: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val rulesFlow = remember(searchQuery) {
        if (searchQuery.isBlank()) ruleRepository.getAll() else ruleRepository.search(searchQuery)
    }
    val rules by rulesFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    Box(modifier = modifier) {
        if (rules.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    style = MaterialTheme.typography.bodyLarge,
                    text = when {
                        searchQuery.isBlank() -> stringResource(Res.string.no_rules_yet)
                        else -> stringResource(
                            Res.string.no_rules_match,
                            searchQuery,
                        )
                    },
                )
            }
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
                        onClick = { onRuleClick(rule) },
                        onToggle = { scope.launch { ruleRepository.setEnabled(rule.id, it) } },
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = onCreateClick,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(Res.string.create_rule_cd),
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
                    label = if (rule.headerMatchers.size == 1) stringResource(Res.string.hdr) else stringResource(
                        Res.string.hdr_count,
                        rule.headerMatchers.size,
                    ),
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
                        text = stringResource(
                            Res.string.response_code_display,
                            action.responseCode,
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                is RuleAction.Throttle -> {
                    Spacer(Modifier.height(2.dp))
                    val delayText =
                        if (action.delayMaxMs != null && action.delayMaxMs != action.delayMs)
                            stringResource(
                                Res.string.delay_range,
                                action.delayMs.toString(),
                                action.delayMaxMs.toString(),
                            )
                        else stringResource(Res.string.delay_fixed, action.delayMs.toString())
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
