package dev.skymansandy.wiretap.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.data.db.entity.WiretapRule
import dev.skymansandy.wiretap.domain.model.BodyMatcher
import dev.skymansandy.wiretap.domain.model.HeaderMatcher
import dev.skymansandy.wiretap.domain.model.RuleAction
import dev.skymansandy.wiretap.domain.model.UrlMatcher
import dev.skymansandy.wiretap.domain.repository.RuleRepository
import dev.skymansandy.jsonviewer.JsonEditor
import dev.skymansandy.jsonviewer.rememberJsonEditorState
import dev.skymansandy.wiretap.resources.Res
import dev.skymansandy.wiretap.resources.any_method
import dev.skymansandy.wiretap.resources.back
import dev.skymansandy.wiretap.resources.body
import dev.skymansandy.wiretap.resources.cancel
import dev.skymansandy.wiretap.resources.delete
import dev.skymansandy.wiretap.resources.delete_rule
import dev.skymansandy.wiretap.resources.delete_rule_cd
import dev.skymansandy.wiretap.resources.delete_rule_confirm
import dev.skymansandy.wiretap.resources.edit_rule_cd
import dev.skymansandy.wiretap.resources.enabled
import dev.skymansandy.wiretap.resources.headers
import dev.skymansandy.wiretap.resources.label_action
import dev.skymansandy.wiretap.resources.label_delay
import dev.skymansandy.wiretap.resources.label_url
import dev.skymansandy.wiretap.resources.no_headers
import dev.skymansandy.wiretap.resources.response_body
import dev.skymansandy.wiretap.resources.response_code_label
import dev.skymansandy.wiretap.resources.response_headers
import dev.skymansandy.wiretap.resources.rule_details
import dev.skymansandy.wiretap.resources.throttle_delay
import dev.skymansandy.wiretap.ui.components.CodeBlock
import dev.skymansandy.wiretap.ui.components.HeadersList
import dev.skymansandy.wiretap.ui.rules.ActionBadge
import dev.skymansandy.wiretap.helper.util.looksLikeJson
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RuleDetailScreen(
    rule: WiretapRule,
    ruleRepository: RuleRepository,
    onBack: () -> Unit,
    onDeleted: () -> Unit,
    onEditClick: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var enabled by remember { mutableStateOf(rule.enabled) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(Res.string.delete_rule)) },
            text = { Text(stringResource(Res.string.delete_rule_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        ruleRepository.deleteById(rule.id)
                        onDeleted()
                    }
                }) {
                    Text(stringResource(Res.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text(stringResource(Res.string.cancel)) }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.rule_details)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(Res.string.edit_rule_cd))
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(Res.string.delete_rule_cd))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            // Enabled toggle
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(Res.string.enabled), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Switch(
                    checked = enabled,
                    onCheckedChange = {
                        enabled = it
                        scope.launch { ruleRepository.setEnabled(rule.id, it) }
                    },
                )
            }

            Spacer(Modifier.height(16.dp))

            DetailRow("Method", if (rule.method == "*") stringResource(Res.string.any_method) else rule.method)

            // URL matcher
            rule.urlMatcher?.let { matcher ->
                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))
                Text(stringResource(Res.string.label_url), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,)
                Spacer(Modifier.height(8.dp))
                DetailRow(urlMatcherLabel(matcher), matcher.pattern)
            }

            // Header matchers
            if (rule.headerMatchers.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))
                Text(stringResource(Res.string.headers), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,)
                rule.headerMatchers.forEach { matcher ->
                    Spacer(Modifier.height(8.dp))
                    HeaderMatcherDetail(matcher)
                }
            }

            // Body matcher
            rule.bodyMatcher?.let { matcher ->
                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))
                Text(stringResource(Res.string.body), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,)
                Spacer(Modifier.height(8.dp))
                DetailRow(bodyMatcherLabel(matcher), matcher.pattern)
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            // Action
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(Res.string.label_action), style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,)
                Spacer(Modifier.width(8.dp))
                ActionBadge(rule.action)
            }

            Spacer(Modifier.height(12.dp))

            when (rule.action) {
                RuleAction.Mock -> {
                    DetailRow(stringResource(Res.string.response_code_label), (rule.mockResponseCode ?: 200).toString())

                    if (!rule.mockResponseBody.isNullOrBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Text(stringResource(Res.string.response_body), style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,)
                        Spacer(Modifier.height(4.dp))
                        if (looksLikeJson(rule.mockResponseBody)) {
                            val editorState = rememberJsonEditorState(initialJson = rule.mockResponseBody)
                            JsonEditor(
                                state = editorState,
                                modifier = Modifier.padding(vertical = 4.dp),
                            )
                        } else {
                            CodeBlock(text = rule.mockResponseBody, modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }

                    if (!rule.mockResponseHeaders.isNullOrEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Text(stringResource(Res.string.response_headers), style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,)
                        Spacer(Modifier.height(4.dp))
                        HeadersList(headers = rule.mockResponseHeaders, emptyText = stringResource(Res.string.no_headers))
                    }
                }
                RuleAction.Throttle -> {
                    val delayText = if (rule.throttleDelayMaxMs != null && rule.throttleDelayMaxMs != rule.throttleDelayMs)
                        "${rule.throttleDelayMs ?: 0}–${rule.throttleDelayMaxMs} ms"
                    else "${rule.throttleDelayMs ?: 0} ms"
                    DetailRow(stringResource(Res.string.label_delay), delayText)
                }
            }

            rule.throttleDelayMs?.let { delay ->
                if (rule.action == RuleAction.Mock) {
                    Spacer(Modifier.height(12.dp))
                    val delayText = if (rule.throttleDelayMaxMs != null && rule.throttleDelayMaxMs != delay)
                        "$delay–${rule.throttleDelayMaxMs} ms"
                    else "$delay ms"
                    DetailRow(stringResource(Res.string.throttle_delay), delayText)
                }
            }
        }
    }
}

@Composable
private fun HeaderMatcherDetail(matcher: HeaderMatcher) {
    when (matcher) {
        is HeaderMatcher.KeyExists ->
            DetailRow("Key Exists", matcher.key)
        is HeaderMatcher.ValueExact ->
            DetailRow("${matcher.key}  =  Exact", matcher.value)
        is HeaderMatcher.ValueContains ->
            DetailRow("${matcher.key}  ~  Contains", matcher.value)
        is HeaderMatcher.ValueRegex ->
            DetailRow("${matcher.key}  *  Regex", matcher.pattern)
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,)
        Spacer(Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
}

private fun urlMatcherLabel(matcher: UrlMatcher) = when (matcher) {
    is UrlMatcher.Exact -> "Exact"
    is UrlMatcher.Contains -> "Contains"
    is UrlMatcher.Regex -> "Regex"
}

private fun bodyMatcherLabel(matcher: BodyMatcher) = when (matcher) {
    is BodyMatcher.Exact -> "Exact"
    is BodyMatcher.Contains -> "Contains"
    is BodyMatcher.Regex -> "Regex"
}

@Preview
@Composable
private fun DetailRowPreview() {
    MaterialTheme {
        DetailRow(label = "Method", value = "GET")
    }
}

@Preview
@Composable
private fun HeaderMatcherKeyExistsPreview() {
    MaterialTheme {
        HeaderMatcherDetail(HeaderMatcher.KeyExists("Authorization"))
    }
}

@Preview
@Composable
private fun HeaderMatcherValueExactPreview() {
    MaterialTheme {
        HeaderMatcherDetail(HeaderMatcher.ValueExact("Content-Type", "application/json"))
    }
}

@Preview
@Composable
private fun HeaderMatcherValueRegexPreview() {
    MaterialTheme {
        HeaderMatcherDetail(HeaderMatcher.ValueRegex("Accept", "text/(html|xml)"))
    }
}
