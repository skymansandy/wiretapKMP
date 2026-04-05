/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.ui.screens.rules.create.step.request

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.ui.model.HeaderEntry
import dev.skymansandy.wiretap.ui.model.HeaderEntryMode
import dev.skymansandy.wiretap.ui.model.hasValue
import dev.skymansandy.wiretap.ui.model.headerValuePlaceholder
import dev.skymansandy.wiretap.ui.model.isRegex
import dev.skymansandy.wiretap.ui.screens.rules.components.RegexTesterIcon

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun HeaderMatcherItem(
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
