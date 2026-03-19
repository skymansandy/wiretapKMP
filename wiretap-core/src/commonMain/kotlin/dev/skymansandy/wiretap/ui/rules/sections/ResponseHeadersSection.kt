package dev.skymansandy.wiretap.ui.rules.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.ui.rules.model.ResponseHeaderEntry
import dev.skymansandy.wiretap.ui.rules.model.ResponseHeadersEditMode
import dev.skymansandy.wiretap_core.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ResponseHeadersSection(
    entries: List<ResponseHeaderEntry>,
    onAdd: () -> Unit,
    onUpdate: (Int, ResponseHeaderEntry) -> Unit,
    onRemove: (Int) -> Unit,
    bulk: String,
    onBulkChange: (String) -> Unit,
    mode: ResponseHeadersEditMode,
    onModeChange: (ResponseHeadersEditMode) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(Res.string.response_headers),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
        IconButton(
            onClick = {
                onModeChange(
                    if (mode == ResponseHeadersEditMode.KEY_VALUE) ResponseHeadersEditMode.BULK_EDIT
                    else ResponseHeadersEditMode.KEY_VALUE,
                )
            },
        ) {
            Icon(
                imageVector = if (mode == ResponseHeadersEditMode.KEY_VALUE) Icons.Default.Edit else Icons.AutoMirrored.Filled.List,
                contentDescription = stringResource(if (mode == ResponseHeadersEditMode.KEY_VALUE) Res.string.switch_to_bulk_edit else Res.string.switch_to_key_value),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }

    when (mode) {
        ResponseHeadersEditMode.KEY_VALUE -> {
            entries.forEachIndexed { idx, entry ->
                ResponseHeaderEntryRow(
                    entry = entry,
                    onUpdate = { onUpdate(idx, it) },
                    onRemove = { onRemove(idx) },
                )
            }
            TextButton(onClick = onAdd, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(Res.string.add_header))
            }
        }
        ResponseHeadersEditMode.BULK_EDIT -> {
            OutlinedTextField(
                value = bulk,
                onValueChange = onBulkChange,
                label = { Text(stringResource(Res.string.headers_bulk_label)) },
                placeholder = { Text(stringResource(Res.string.placeholder_headers_bulk)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 8,
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
            )
        }
    }
}

@Composable
private fun ResponseHeaderEntryRow(
    entry: ResponseHeaderEntry,
    onUpdate: (ResponseHeaderEntry) -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = entry.key,
            onValueChange = { onUpdate(entry.copy(key = it)) },
            label = { Text(stringResource(Res.string.label_key)) },
            placeholder = { Text(stringResource(Res.string.placeholder_content_type)) },
            modifier = Modifier.weight(1f),
            singleLine = true,
        )
        OutlinedTextField(
            value = entry.value,
            onValueChange = { onUpdate(entry.copy(value = it)) },
            label = { Text(stringResource(Res.string.label_value)) },
            placeholder = { Text(stringResource(Res.string.placeholder_application_json)) },
            modifier = Modifier.weight(1f),
            singleLine = true,
        )
        IconButton(onClick = onRemove) {
            Icon(
                Icons.Default.Close,
                contentDescription = stringResource(Res.string.remove),
                tint = MaterialTheme.colorScheme.error,
            )
        }
    }
}
