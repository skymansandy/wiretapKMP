package dev.skymansandy.wiretap.ui.rules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.data.db.entity.WiretapRule
import dev.skymansandy.wiretap.domain.model.MatcherType
import dev.skymansandy.wiretap.domain.model.RuleAction
import dev.skymansandy.wiretap.domain.repository.RuleRepository
import dev.skymansandy.wiretap.util.HeadersSerializerUtil
import dev.skymansandy.wiretap.util.currentTimeMillis

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CreateRuleScreen(
    ruleRepository: RuleRepository,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    existingRule: WiretapRule? = null,
) {
    val isEditing = existingRule != null
    var matcherType by remember { mutableStateOf(existingRule?.matcherType ?: MatcherType.URL_EXACT) }
    var pattern by remember { mutableStateOf(existingRule?.urlPattern ?: "") }
    var method by remember { mutableStateOf(existingRule?.method ?: "*") }
    var action by remember { mutableStateOf(existingRule?.action ?: RuleAction.MOCK) }

    // Mock fields
    var mockResponseCode by remember { mutableStateOf(existingRule?.mockResponseCode?.toString() ?: "200") }
    var mockResponseBody by remember { mutableStateOf(existingRule?.mockResponseBody ?: "") }
    var mockResponseHeaders by remember {
        mutableStateOf(existingRule?.mockResponseHeaders?.let { HeadersSerializerUtil.serialize(it) } ?: "")
    }

    // Throttle fields
    var throttleDelayMs by remember { mutableStateOf(existingRule?.throttleDelayMs?.toString() ?: "1000") }

    // Regex tester
    var testUrl by remember { mutableStateOf("") }

    val canSave = pattern.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Rule" else "Create Rule") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Matcher Type
            Text(
                text = "Matcher Type",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            MatcherTypeSelector(
                selected = matcherType,
                onSelect = { matcherType = it },
            )

            // Pattern
            OutlinedTextField(
                value = pattern,
                onValueChange = { pattern = it },
                label = { Text(patternLabel(matcherType)) },
                placeholder = { Text(patternPlaceholder(matcherType)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = matcherType != MatcherType.BODY_CONTAINS,
                minLines = if (matcherType == MatcherType.BODY_CONTAINS) 3 else 1,
            )

            // Regex Tester
            if (matcherType == MatcherType.URL_REGEX && pattern.isNotBlank()) {
                RegexTester(pattern = pattern, testUrl = testUrl, onTestUrlChange = { testUrl = it })
            }

            // Method
            MethodSelector(method = method, onMethodChange = { method = it })

            // Action
            Text(
                text = "Action",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = action == RuleAction.MOCK,
                    onClick = { action = RuleAction.MOCK },
                    label = { Text("Mock") },
                )
                FilterChip(
                    selected = action == RuleAction.THROTTLE,
                    onClick = { action = RuleAction.THROTTLE },
                    label = { Text("Throttle") },
                )
            }

            // Action-specific fields
            when (action) {
                RuleAction.MOCK -> {
                    OutlinedTextField(
                        value = mockResponseCode,
                        onValueChange = { mockResponseCode = it.filter { c -> c.isDigit() } },
                        label = { Text("Response Code") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )

                    OutlinedTextField(
                        value = mockResponseBody,
                        onValueChange = { mockResponseBody = it },
                        label = { Text("Response Body") },
                        placeholder = { Text("{\"key\": \"value\"}") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        maxLines = 10,
                    )

                    OutlinedTextField(
                        value = mockResponseHeaders,
                        onValueChange = { mockResponseHeaders = it },
                        label = { Text("Response Headers") },
                        placeholder = { Text("Content-Type: application/json\nCache-Control: no-cache") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 5,
                    )
                }

                RuleAction.THROTTLE -> {
                    OutlinedTextField(
                        value = throttleDelayMs,
                        onValueChange = { throttleDelayMs = it.filter { c -> c.isDigit() } },
                        label = { Text("Delay (ms)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                }
            }

            // Save Button
            Button(
                onClick = {
                    val rule = WiretapRule(
                        id = existingRule?.id ?: 0,
                        matcherType = matcherType,
                        urlPattern = pattern.trim(),
                        method = method.trim().ifBlank { "*" },
                        action = action,
                        mockResponseCode = if (action == RuleAction.MOCK) {
                            mockResponseCode.toIntOrNull() ?: 200
                        } else null,
                        mockResponseBody = if (action == RuleAction.MOCK) {
                            mockResponseBody.ifBlank { null }
                        } else null,
                        mockResponseHeaders = if (action == RuleAction.MOCK && mockResponseHeaders.isNotBlank()) {
                            HeadersSerializerUtil.deserialize(mockResponseHeaders)
                        } else null,
                        throttleDelayMs = if (action == RuleAction.THROTTLE) {
                            throttleDelayMs.toLongOrNull() ?: 1000L
                        } else null,
                        enabled = existingRule?.enabled ?: true,
                        createdAt = existingRule?.createdAt ?: currentTimeMillis(),
                    )
                    if (isEditing) ruleRepository.updateRule(rule) else ruleRepository.addRule(rule)
                    onSaved()
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Save Rule")
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MatcherTypeSelector(
    selected: MatcherType,
    onSelect: (MatcherType) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        MatcherType.entries.forEach { type ->
            FilterChip(
                selected = selected == type,
                onClick = { onSelect(type) },
                label = { Text(matcherTypeChipLabel(type)) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MethodSelector(
    method: String,
    onMethodChange: (String) -> Unit,
) {
    val methods = listOf("*", "GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS")
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            value = if (method == "*") "Any" else method,
            onValueChange = {},
            readOnly = true,
            label = { Text("HTTP Method") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            methods.forEach { m ->
                DropdownMenuItem(
                    text = { Text(if (m == "*") "Any" else m) },
                    onClick = {
                        onMethodChange(m)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun RegexTester(
    pattern: String,
    testUrl: String,
    onTestUrlChange: (String) -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Regex Tester",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = testUrl,
                onValueChange = onTestUrlChange,
                label = { Text("Test URL") },
                placeholder = { Text("https://api.example.com/users/123") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            if (testUrl.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                val result = testRegex(pattern, testUrl)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (result.matches) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = null,
                        tint = if (result.matches) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    Column {
                        Text(
                            text = if (result.matches) "Match found" else "No match",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (result.matches) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                        )
                        if (result.error != null) {
                            Text(
                                text = result.error,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class RegexTestResult(val matches: Boolean, val error: String?)

private fun testRegex(pattern: String, input: String): RegexTestResult {
    return try {
        val regex = pattern.toRegex(RegexOption.IGNORE_CASE)
        RegexTestResult(matches = regex.containsMatchIn(input), error = null)
    } catch (e: Exception) {
        RegexTestResult(matches = false, error = "Invalid regex: ${e.message}")
    }
}

private fun patternLabel(type: MatcherType): String = when (type) {
    MatcherType.URL_EXACT -> "URL"
    MatcherType.URL_REGEX -> "URL Regex Pattern"
    MatcherType.HEADER_CONTAINS -> "Header Pattern"
    MatcherType.BODY_CONTAINS -> "Body Pattern"
}

private fun patternPlaceholder(type: MatcherType): String = when (type) {
    MatcherType.URL_EXACT -> "https://api.example.com/users/123"
    MatcherType.URL_REGEX -> "api\\.example\\.com/users/\\d+"
    MatcherType.HEADER_CONTAINS -> "Authorization: Bearer"
    MatcherType.BODY_CONTAINS -> "\"error\""
}

private fun matcherTypeChipLabel(type: MatcherType): String = when (type) {
    MatcherType.URL_EXACT -> "Exact URL"
    MatcherType.URL_REGEX -> "URL Regex"
    MatcherType.HEADER_CONTAINS -> "Header"
    MatcherType.BODY_CONTAINS -> "Body"
}
