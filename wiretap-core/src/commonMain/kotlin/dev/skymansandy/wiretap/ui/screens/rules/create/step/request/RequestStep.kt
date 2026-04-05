/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.ui.screens.rules.create.step.request

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.skymansandy.wiretap.ui.model.BodyMatchMode
import dev.skymansandy.wiretap.ui.model.HeaderEntry
import dev.skymansandy.wiretap.ui.model.HeaderEntryMode
import dev.skymansandy.wiretap.ui.model.UrlMatchMode
import dev.skymansandy.wiretap.ui.model.bodyPlaceholder
import dev.skymansandy.wiretap.ui.model.hasValue
import dev.skymansandy.wiretap.ui.model.headerValuePlaceholder
import dev.skymansandy.wiretap.ui.model.isRegex
import dev.skymansandy.wiretap.ui.model.urlPlaceholder
import dev.skymansandy.wiretap.ui.screens.rules.components.ExpandableSection
import dev.skymansandy.wiretap.ui.screens.rules.components.MethodSelector
import dev.skymansandy.wiretap.ui.screens.rules.components.RegexTesterIcon
import dev.skymansandy.wiretap.ui.screens.rules.create.CreateRuleViewModel

private val URL_INFO = """
    |Match requests whose URL meets the selected condition.
    |
    |• Exact — URL must match the full value exactly
    |• Contains — URL must include the given substring
    |• Regex — URL must match the regular expression pattern
""".trimMargin()

private val HEADERS_INFO = """
    |Match requests that have specific HTTP headers.
    |
    |• Key Exists — matches if the header key is present, regardless of value
    |• Exact — header value must match exactly
    |• Contains — header value must include the given substring
    |• Regex — header value must match the regular expression pattern
""".trimMargin()

private val BODY_INFO = """
    |Match requests whose body content meets the selected condition.
    |
    |• Exact — body must match the full value exactly
    |• Contains — body must include the given substring
    |• Regex — body must match the regular expression pattern
""".trimMargin()

private fun urlFieldLabel(mode: UrlMatchMode) = when (mode) {
    UrlMatchMode.Exact -> "URL is"
    UrlMatchMode.Contains -> "URL contains"
    UrlMatchMode.Regex -> "URL looks like"
}

private fun bodyFieldLabel(mode: BodyMatchMode) = when (mode) {
    BodyMatchMode.Exact -> "Body is"
    BodyMatchMode.Contains -> "Body contains"
    BodyMatchMode.Regex -> "Body looks like"
}

private fun urlWarning(mode: UrlMatchMode?, pattern: String): String? = when {
    mode == null -> null
    pattern.isBlank() -> when (mode) {
        UrlMatchMode.Exact -> "Enter the exact URL to match"
        UrlMatchMode.Contains -> "Enter a substring the URL should contain"
        UrlMatchMode.Regex -> "Enter a regex pattern to match the URL"
    }
    else -> null
}

private fun bodyWarning(mode: BodyMatchMode?, pattern: String): String? = when {
    mode == null -> null
    pattern.isBlank() -> when (mode) {
        BodyMatchMode.Exact -> "Enter the exact body to match"
        BodyMatchMode.Contains -> "Enter a substring the body should contain"
        BodyMatchMode.Regex -> "Enter a regex pattern to match the body"
    }
    else -> null
}

private fun headersWarning(entries: List<HeaderEntry>): String? {
    if (entries.isEmpty()) return null
    for (entry in entries) {
        if (entry.key.isBlank()) return "Header key is missing"
        if (entry.mode.hasValue() && entry.value.isBlank()) {
            return when (entry.mode) {
                HeaderEntryMode.ValueExact -> "Enter the exact header value for \"${entry.key}\""
                HeaderEntryMode.ValueContains -> "Enter a substring for header \"${entry.key}\""
                HeaderEntryMode.ValueRegex -> "Enter a regex pattern for header \"${entry.key}\""
                else -> null
            }
        }
    }
    return null
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun RequestStep(
    modifier: Modifier = Modifier,
    viewModel: CreateRuleViewModel,
) {
    val state by viewModel.requestState.collectAsStateWithLifecycle()
    val method = state.method
    val urlMode = state.urlMode
    val urlPattern = state.urlPattern
    val headerEntries = state.headerEntries
    val bodyMode = state.bodyMode
    val bodyPattern = state.bodyPattern

    val testUrlLabel = "Test URL"
    val testHeaderValueLabel = "Test Header Value"
    val testBodyLabel = "Test Body"

    Column(
        modifier = modifier,
    ) {
        // HTTP Method — always visible at the top
        MethodSelector(
            method = method,
            onMethodChange = { viewModel.updateMethod(it) },
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        // ── URL ──────────────────────────────────────────────────────────────────
        ExpandableSection(
            title = "Match URL",
            subtitle = when (urlMode) {
                null -> null
                else -> "${urlMode.label}: ${urlPattern.ifEmpty { "..." }}"
            },
            warning = urlWarning(urlMode, urlPattern),
            infoText = URL_INFO,
            initiallyExpanded = true,
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = urlMode == null,
                    onClick = { viewModel.updateUrlMode(null) },
                    label = { Text("None") },
                )

                UrlMatchMode.entries.forEach { mode ->
                    FilterChip(
                        selected = urlMode == mode,
                        onClick = { viewModel.updateUrlMode(mode) },
                        label = { Text(mode.label) },
                    )
                }
            }

            if (urlMode != null) {
                OutlinedTextField(
                    value = urlPattern,
                    onValueChange = { viewModel.updateUrlPattern(it) },
                    label = { Text(urlFieldLabel(urlMode)) },
                    placeholder = { Text(urlPlaceholder(urlMode)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = when {
                        urlMode.isRegex() -> {
                            { RegexTesterIcon { viewModel.openRegexTester(urlPattern, testUrlLabel) } }
                        }

                        else -> null
                    },
                )
            }
        }

        // ── Headers ───────────────────────────────────────────────────────────────
        ExpandableSection(
            title = "Match Headers",
            subtitle = when {
                headerEntries.isNotEmpty() -> "${headerEntries.size} condition${if (headerEntries.size != 1) "s" else ""}"
                else -> null
            },
            warning = headersWarning(headerEntries),
            infoText = HEADERS_INFO,
            initiallyExpanded = headerEntries.isNotEmpty(),
        ) {
            headerEntries.forEachIndexed { idx, entry ->
                HeaderMatcherItem(
                    entry = entry,
                    onUpdate = { viewModel.updateHeader(idx, it) },
                    onRemove = { viewModel.removeHeader(idx) },
                    onOpenRegexTester = {
                        viewModel.openRegexTester(it, testHeaderValueLabel)
                    },
                )
            }

            TextButton(
                onClick = { viewModel.addHeader() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "+ Add Header Condition",
                )
            }
        }

        // ── Body ──────────────────────────────────────────────────────────────────
        ExpandableSection(
            title = "Match Body",
            subtitle = when (bodyMode) {
                null -> null
                else -> bodyMode.label
            },
            warning = bodyWarning(bodyMode, bodyPattern),
            infoText = BODY_INFO,
            initiallyExpanded = bodyMode != null,
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = bodyMode == null,
                    onClick = { viewModel.updateBodyMode(null) },
                    label = { Text("None") },
                )

                BodyMatchMode.entries.forEach { mode ->
                    FilterChip(
                        selected = bodyMode == mode,
                        onClick = { viewModel.updateBodyMode(mode) },
                        label = { Text(mode.label) },
                    )
                }
            }

            if (bodyMode != null) {
                OutlinedTextField(
                    value = bodyPattern,
                    onValueChange = { viewModel.updateBodyPattern(it) },
                    label = { Text(bodyFieldLabel(bodyMode)) },
                    placeholder = { Text(bodyPlaceholder(bodyMode)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    trailingIcon = when {
                        bodyMode.isRegex() -> {
                            { RegexTesterIcon { viewModel.openRegexTester(bodyPattern, testBodyLabel) } }
                        }

                        else -> null
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun HeaderMatcherItem(
    entry: HeaderEntry,
    onUpdate: (HeaderEntry) -> Unit,
    onRemove: () -> Unit,
    onOpenRegexTester: (pattern: String) -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .padding(top = 4.dp, bottom = 8.dp),
        ) {
            // Row 1: Mode dropdown + Key + Remove
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    modifier = Modifier.width(130.dp),
                ) {
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        HeaderEntryMode.entries.forEach { mode ->
                            DropdownMenuItem(
                                text = { Text(mode.label) },
                                onClick = {
                                    onUpdate(
                                        entry.copy(
                                            mode = mode,
                                            value = if (!mode.hasValue()) "" else entry.value,
                                        ),
                                    )
                                    expanded = false
                                },
                            )
                        }
                    }

                    OutlinedTextField(
                        value = entry.mode.label,
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        label = { Text("Match") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    )
                }

                OutlinedTextField(
                    value = entry.key,
                    onValueChange = { onUpdate(entry.copy(key = it)) },
                    label = { Text("Key") },
                    placeholder = { Text("Authorization") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )

                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }

            // Row 2: Value field (only when mode needs a value)
            if (entry.mode.hasValue()) {
                OutlinedTextField(
                    value = entry.value,
                    onValueChange = { onUpdate(entry.copy(value = it)) },
                    label = {
                        Text(
                            if (entry.mode.isRegex()) "Value Regex"
                            else "Value",
                        )
                    },
                    placeholder = { Text(headerValuePlaceholder(entry.mode)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = if (entry.mode.isRegex()) {
                        { RegexTesterIcon { onOpenRegexTester(entry.value) } }
                    } else null,
                )
            }
        }
    }
}
