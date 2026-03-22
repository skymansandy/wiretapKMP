package dev.skymansandy.wiretap.ui.rules.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.ui.model.BodyMatchMode
import dev.skymansandy.wiretap.ui.model.HeaderEntry
import dev.skymansandy.wiretap.ui.model.HeaderEntryMode
import dev.skymansandy.wiretap.ui.model.UrlMatchMode
import dev.skymansandy.wiretap.ui.model.bodyPlaceholder
import dev.skymansandy.wiretap.ui.model.hasValue
import dev.skymansandy.wiretap.ui.model.headerValuePlaceholder
import dev.skymansandy.wiretap.ui.model.isRegex
import dev.skymansandy.wiretap.ui.model.label
import dev.skymansandy.wiretap.ui.model.urlPlaceholder
import dev.skymansandy.wiretap.ui.screens.rule.components.MethodSelector
import dev.skymansandy.wiretap.ui.screens.rule.components.RegexTesterIcon
import dev.skymansandy.wiretap.ui.screens.rule.components.SectionLabel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun RequestStep(
    method: String,
    onMethodChange: (String) -> Unit,
    urlMode: UrlMatchMode?,
    onUrlModeChange: (UrlMatchMode?) -> Unit,
    urlPattern: String,
    onUrlPatternChange: (String) -> Unit,
    headerEntries: List<HeaderEntry>,
    onHeaderAdd: () -> Unit,
    onHeaderUpdate: (Int, HeaderEntry) -> Unit,
    onHeaderRemove: (Int) -> Unit,
    bodyMode: BodyMatchMode?,
    onBodyModeChange: (BodyMatchMode?) -> Unit,
    bodyPattern: String,
    onBodyPatternChange: (String) -> Unit,
    onOpenRegexTester: (pattern: String, label: String) -> Unit,
) {
    // HTTP Method — at the top
    MethodSelector(
        method = method,
        onMethodChange = onMethodChange,
    )

    // ── URL ──────────────────────────────────────────────────────────────────
    SectionLabel(
        title = "URL",
    )

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = urlMode == null,
            onClick = { onUrlModeChange(null) },
            label = { Text("None") },
        )

        UrlMatchMode.entries.forEach { mode ->
            FilterChip(
                selected = urlMode == mode,
                onClick = { onUrlModeChange(mode) },
                label = { Text(mode.label()) },
            )
        }
    }

    val testUrlLabel = "Test URL"
    val testHeaderValueLabel = "Test Header Value"
    val testBodyLabel = "Test Body"

    if (urlMode != null) {
        OutlinedTextField(
            value = urlPattern,
            onValueChange = onUrlPatternChange,
            label = { Text("URL ${urlMode.label()}") },
            placeholder = { Text(urlPlaceholder(urlMode)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            trailingIcon = when {
                urlMode.isRegex() -> {
                    { RegexTesterIcon { onOpenRegexTester(urlPattern, testUrlLabel) } }
                }

                else -> null
            },
        )
    }

    // ── Headers ───────────────────────────────────────────────────────────────
    SectionLabel(
        title = "Headers",
    )

    headerEntries.forEachIndexed { idx, entry ->
        HeaderMatcherItem(
            entry = entry,
            onUpdate = { onHeaderUpdate(idx, it) },
            onRemove = { onHeaderRemove(idx) },
            onOpenRegexTester = {
                onOpenRegexTester(it, testHeaderValueLabel)
            },
        )
    }

    TextButton(
        onClick = onHeaderAdd,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = "+ Add Header Condition",
        )
    }

    // ── Body ──────────────────────────────────────────────────────────────────
    SectionLabel(
        title = "Body",
    )

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = bodyMode == null,
            onClick = { onBodyModeChange(null) },
            label = { Text("None") },
        )

        BodyMatchMode.entries.forEach { mode ->
            FilterChip(
                selected = bodyMode == mode,
                onClick = { onBodyModeChange(mode) },
                label = { Text(mode.label()) },
            )
        }
    }

    if (bodyMode != null) {
        OutlinedTextField(
            value = bodyPattern,
            onValueChange = onBodyPatternChange,
            label = { Text("Body ${bodyMode.label()}") },
            placeholder = { Text(bodyPlaceholder(bodyMode)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            trailingIcon = when {
                bodyMode.isRegex() -> {
                    { RegexTesterIcon { onOpenRegexTester(bodyPattern, testBodyLabel) } }
                }

                else -> null
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
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
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {

            // Key / Value row (50 / 50)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = entry.key,
                    onValueChange = { onUpdate(entry.copy(key = it)) },
                    label = { Text("Key") },
                    placeholder = { Text("Authorization") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
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
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        trailingIcon = if (entry.mode.isRegex()) {
                            { RegexTesterIcon { onOpenRegexTester(entry.value) } }
                        } else null,
                    )
                } else {
                    // Placeholder to keep key at 50% width
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            // Mode chips + remove button
            Row(verticalAlignment = Alignment.CenterVertically) {
                FlowRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    HeaderEntryMode.entries.forEach { mode ->
                        FilterChip(
                            selected = entry.mode == mode,
                            onClick = {
                                onUpdate(
                                    entry.copy(
                                        mode = mode,
                                        value = if (!mode.hasValue()) "" else entry.value,
                                    ),
                                )
                            },
                            label = { Text(mode.label()) },
                        )
                    }
                }
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}
