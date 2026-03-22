package dev.skymansandy.wiretap.ui.screens.rule

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.skymansandy.jsoncmp.JsonCMP
import dev.skymansandy.jsoncmp.config.rememberJsonEditorState
import dev.skymansandy.wiretap.data.db.entity.WiretapRule
import dev.skymansandy.wiretap.domain.model.BodyMatcher
import dev.skymansandy.wiretap.domain.model.HeaderMatcher
import dev.skymansandy.wiretap.domain.model.RuleAction
import dev.skymansandy.wiretap.domain.model.UrlMatcher
import dev.skymansandy.wiretap.helper.util.looksLikeJson
import dev.skymansandy.wiretap.ui.common.CodeBlock
import dev.skymansandy.wiretap.ui.common.HeadersList
import dev.skymansandy.wiretap.ui.rules.ActionBadge

@Suppress("CyclomaticComplexMethod")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RuleDetailScreen(
    rule: WiretapRule,
    viewModel: RuleDetailViewModel,
    onBack: () -> Unit,
    onDeleted: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val showDeleteConfirm by viewModel.showDeleteConfirm.collectAsStateWithLifecycle()
    val enabled by viewModel.enabled.collectAsStateWithLifecycle()

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDelete() },
            title = { Text("Delete Rule") },
            text = { Text("Are you sure you want to delete this rule?") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDelete(onDeleted) }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDelete() }) { Text("Cancel") }
            },
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Rule Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit rule")
                    }
                    IconButton(onClick = { viewModel.requestDelete() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete rule")
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
                Text("Enabled", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Switch(
                    checked = enabled,
                    onCheckedChange = { viewModel.toggleEnabled(it) },
                )
            }

            Spacer(Modifier.height(16.dp))

            DetailRow(
                "Method",
                if (rule.method == "*") "Any" else rule.method,
            )

            // URL matcher
            rule.urlMatcher?.let { matcher ->
                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))
                Text("URL", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,)
                Spacer(Modifier.height(8.dp))
                DetailRow(urlMatcherLabel(matcher), matcher.pattern)
            }

            // Header matchers
            if (rule.headerMatchers.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))
                Text("Headers", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
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
                Text("Body", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,)
                Spacer(Modifier.height(8.dp))
                DetailRow(bodyMatcherLabel(matcher), matcher.pattern)
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            // Action
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Action", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,)
                Spacer(Modifier.width(8.dp))
                ActionBadge(action = rule.action)
            }

            Spacer(Modifier.height(12.dp))

            when (val action = rule.action) {
                is RuleAction.Mock -> {
                    DetailRow("Response Code", action.responseCode.toString())

                    if (!action.responseBody.isNullOrBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Text("Response Body", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,)
                        Spacer(Modifier.height(4.dp))
                        if (looksLikeJson(action.responseBody)) {
                            val editorState = rememberJsonEditorState(initialJson = action.responseBody)
                            JsonCMP(
                                state = editorState,
                                modifier = Modifier.padding(vertical = 4.dp),
                            )
                        } else {
                            CodeBlock(text = action.responseBody, modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }

                    if (!action.responseHeaders.isNullOrEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Text("Response Headers", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,)
                        Spacer(Modifier.height(4.dp))
                        HeadersList(headers = action.responseHeaders, emptyText = "No headers")
                    }

                    action.throttleDelayMs?.let { delay ->
                        Spacer(Modifier.height(12.dp))
                        val delayText = if (action.throttleDelayMaxMs != null && action.throttleDelayMaxMs != delay)
                            "$delay–${action.throttleDelayMaxMs} ms"
                        else "$delay ms"
                        DetailRow("Throttle Delay", delayText)
                    }
                }
                is RuleAction.Throttle -> {
                    val delayText = if (action.delayMaxMs != null && action.delayMaxMs != action.delayMs)
                        "${action.delayMs}–${action.delayMaxMs} ms"
                    else "${action.delayMs} ms"
                    DetailRow("Delay", delayText)
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

@Composable
private fun urlMatcherLabel(matcher: UrlMatcher) = when (matcher) {
    is UrlMatcher.Exact -> "Exact"
    is UrlMatcher.Contains -> "Contains"
    is UrlMatcher.Regex -> "Regex"
}

@Composable
private fun bodyMatcherLabel(matcher: BodyMatcher) = when (matcher) {
    is BodyMatcher.Exact -> "Exact"
    is BodyMatcher.Contains -> "Contains"
    is BodyMatcher.Regex -> "Regex"
}

@Preview
@Composable
private fun Preview_DetailRow() {
    MaterialTheme {
        DetailRow(label = "Method", value = "GET")
    }
}

@Preview
@Composable
private fun Preview_HeaderMatcherKeyExists() {
    MaterialTheme {
        HeaderMatcherDetail(HeaderMatcher.KeyExists("Authorization"))
    }
}

@Preview
@Composable
private fun Preview_HeaderMatcherValueExact() {
    MaterialTheme {
        HeaderMatcherDetail(HeaderMatcher.ValueExact("Content-Type", "application/json"))
    }
}

@Preview
@Composable
private fun Preview_HeaderMatcherValueRegex() {
    MaterialTheme {
        HeaderMatcherDetail(HeaderMatcher.ValueRegex("Accept", "text/(html|xml)"))
    }
}
