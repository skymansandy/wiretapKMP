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
import dev.skymansandy.wiretap.ui.rules.components.MethodSelector
import dev.skymansandy.wiretap.ui.rules.components.RegexTesterIcon
import dev.skymansandy.wiretap.ui.rules.components.SectionLabel
import dev.skymansandy.wiretap.ui.rules.model.BodyMatchMode
import dev.skymansandy.wiretap.ui.rules.model.HeaderEntry
import dev.skymansandy.wiretap.ui.rules.model.HeaderEntryMode
import dev.skymansandy.wiretap.ui.rules.model.UrlMatchMode
import dev.skymansandy.wiretap.ui.rules.model.bodyPlaceholder
import dev.skymansandy.wiretap.ui.rules.model.hasValue
import dev.skymansandy.wiretap.ui.rules.model.headerValuePlaceholder
import dev.skymansandy.wiretap.ui.rules.model.isRegex
import dev.skymansandy.wiretap.ui.rules.model.label
import dev.skymansandy.wiretap.ui.rules.model.urlPlaceholder
import dev.skymansandy.wiretap_core.generated.resources.*
import org.jetbrains.compose.resources.stringResource

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
    MethodSelector(method = method, onMethodChange = onMethodChange)

    // ── URL ──────────────────────────────────────────────────────────────────
    SectionLabel("URL")
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = urlMode == null,
            onClick = { onUrlModeChange(null) },
            label = { Text(stringResource(Res.string.none)) },
        )
        UrlMatchMode.entries.forEach { mode ->
            FilterChip(
                selected = urlMode == mode,
                onClick = { onUrlModeChange(mode) },
                label = { Text(mode.label()) },
            )
        }
    }
    if (urlMode != null) {
        OutlinedTextField(
            value = urlPattern,
            onValueChange = onUrlPatternChange,
            label = { Text(stringResource(Res.string.url_label_format, urlMode.label())) },
            placeholder = { Text(urlPlaceholder(urlMode)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            trailingIcon = if (urlMode.isRegex()) {
                { RegexTesterIcon { onOpenRegexTester(urlPattern, "Test URL") } }
            } else null,
        )
    }

    // ── Headers ───────────────────────────────────────────────────────────────
    SectionLabel("Headers")
    headerEntries.forEachIndexed { idx, entry ->
        HeaderMatcherItem(
            entry = entry,
            onUpdate = { onHeaderUpdate(idx, it) },
            onRemove = { onHeaderRemove(idx) },
            onOpenRegexTester = { onOpenRegexTester(it, "Test Header Value") },
        )
    }
    TextButton(
        onClick = onHeaderAdd,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(stringResource(Res.string.add_header_condition))
    }

    // ── Body ──────────────────────────────────────────────────────────────────
    SectionLabel("Body")
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = bodyMode == null,
            onClick = { onBodyModeChange(null) },
            label = { Text(stringResource(Res.string.none)) },
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
            label = { Text("${stringResource(Res.string.body)} ${bodyMode.label()}") },
            placeholder = { Text(bodyPlaceholder(bodyMode)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            trailingIcon = if (bodyMode.isRegex()) {
                { RegexTesterIcon { onOpenRegexTester(bodyPattern, "Test Body") } }
            } else null,
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
        androidx.compose.foundation.layout.Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {

            // Key / Value row (50 / 50)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = entry.key,
                    onValueChange = { onUpdate(entry.copy(key = it)) },
                    label = { Text(stringResource(Res.string.label_key)) },
                    placeholder = { Text(stringResource(Res.string.placeholder_authorization)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                if (entry.mode.hasValue()) {
                    OutlinedTextField(
                        value = entry.value,
                        onValueChange = { onUpdate(entry.copy(value = it)) },
                        label = { Text(stringResource(if (entry.mode.isRegex()) Res.string.label_value_regex else Res.string.label_value)) },
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
                            onClick = { onUpdate(entry.copy(mode = mode, value = if (!mode.hasValue()) "" else entry.value)) },
                            label = { Text(mode.label()) },
                        )
                    }
                }
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(Res.string.remove),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}
