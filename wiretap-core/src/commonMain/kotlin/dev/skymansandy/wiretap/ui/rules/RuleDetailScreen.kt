package dev.skymansandy.wiretap.ui.rules

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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.data.db.entity.WiretapRule
import dev.skymansandy.wiretap.domain.model.MatcherType
import dev.skymansandy.wiretap.domain.model.RuleAction
import dev.skymansandy.wiretap.domain.repository.RuleRepository
import dev.skymansandy.wiretap.ui.network.CodeBlock
import dev.skymansandy.wiretap.ui.network.HeadersList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RuleDetailScreen(
    rule: WiretapRule,
    ruleRepository: RuleRepository,
    onBack: () -> Unit,
    onDeleted: () -> Unit,
    onEditClick: () -> Unit,
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var enabled by remember { mutableStateOf(rule.enabled) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Rule") },
            text = { Text("Are you sure you want to delete this rule?") },
            confirmButton = {
                TextButton(onClick = {
                    ruleRepository.deleteById(rule.id)
                    onDeleted()
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    Scaffold(
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
                    IconButton(onClick = { showDeleteConfirm = true }) {
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Enabled",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = enabled,
                    onCheckedChange = {
                        enabled = it
                        ruleRepository.setEnabled(rule.id, it)
                    },
                )
            }

            Spacer(Modifier.height(16.dp))

            DetailRow("Matcher Type", matcherTypeLabel(rule.matcherType))

            Spacer(Modifier.height(12.dp))

            DetailRow("Pattern", rule.urlPattern)

            Spacer(Modifier.height(12.dp))

            DetailRow("Method", if (rule.method == "*") "Any" else rule.method)

            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Action",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(8.dp))
                ActionBadge(rule.action)
            }

            Spacer(Modifier.height(16.dp))

            when (rule.action) {
                RuleAction.MOCK -> {
                    DetailRow("Response Code", (rule.mockResponseCode ?: 200).toString())

                    if (!rule.mockResponseBody.isNullOrBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Response Body",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(4.dp))
                        CodeBlock(
                            text = rule.mockResponseBody,
                            modifier = Modifier.padding(vertical = 4.dp),
                        )
                    }

                    if (!rule.mockResponseHeaders.isNullOrEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Response Headers",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(4.dp))
                        HeadersList(
                            headers = rule.mockResponseHeaders,
                            emptyText = "No headers",
                        )
                    }
                }

                RuleAction.THROTTLE -> {
                    DetailRow("Delay", "${rule.throttleDelayMs ?: 0} ms")
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
        )
    }
}

private fun matcherTypeLabel(type: MatcherType): String = when (type) {
    MatcherType.URL_EXACT -> "Exact URL Match"
    MatcherType.URL_REGEX -> "URL Regex Match"
    MatcherType.HEADER_CONTAINS -> "Header Contains"
    MatcherType.BODY_CONTAINS -> "Body Contains"
}
