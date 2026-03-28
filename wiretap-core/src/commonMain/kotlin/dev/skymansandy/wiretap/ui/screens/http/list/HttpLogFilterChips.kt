package dev.skymansandy.wiretap.ui.screens.http.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.skymansandy.wiretap.domain.model.HttpLogFilter
import dev.skymansandy.wiretap.domain.model.ResponseSource
import dev.skymansandy.wiretap.domain.model.StatusGroup

private val httpMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun HttpLogFilterBottomSheet(
    modifier: Modifier = Modifier,
    viewModel: HttpLogListViewModel,
    onDismiss: () -> Unit,
) {
    val currentFilter by viewModel.filter.collectAsStateWithLifecycle()
    val availableHosts by viewModel.availableHosts.collectAsStateWithLifecycle()
    var pendingFilter by remember(currentFilter) { mutableStateOf(currentFilter) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Filters",
                style = MaterialTheme.typography.titleLarge,
            )

            // Domain section
            if (availableHosts.isNotEmpty()) {
                Text(
                    text = "Domain",
                    style = MaterialTheme.typography.titleSmall,
                )
                DomainMultiSelectDropdown(
                    availableHosts = availableHosts,
                    selectedHosts = pendingFilter.domains,
                    onSelectionChange = { domains ->
                        pendingFilter = pendingFilter.copy(domains = domains)
                    },
                )
            }

            // Status section
            Text(
                text = "Status",
                style = MaterialTheme.typography.titleSmall,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                StatusGroup.entries
                    .filter { it != StatusGroup.All }
                    .forEach { group ->
                        val selected = group in pendingFilter.statusGroups
                        FilterChip(
                            selected = selected,
                            onClick = {
                                pendingFilter = pendingFilter.copy(
                                    statusGroups = if (selected) {
                                        pendingFilter.statusGroups - group
                                    } else {
                                        pendingFilter.statusGroups + group
                                    },
                                )
                            },
                            label = {
                                Text(
                                    text = group.label,
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            },
                        )
                    }
            }

            // Method section
            Text(
                text = "Method",
                style = MaterialTheme.typography.titleSmall,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                httpMethods.forEach { method ->
                    val selected = method in pendingFilter.methods
                    FilterChip(
                        selected = selected,
                        onClick = {
                            pendingFilter = pendingFilter.copy(
                                methods = if (selected) {
                                    pendingFilter.methods - method
                                } else {
                                    pendingFilter.methods + method
                                },
                            )
                        },
                        label = {
                            Text(
                                text = method,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        },
                    )
                }
            }

            // Source section
            Text(
                text = "Source",
                style = MaterialTheme.typography.titleSmall,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                ResponseSource.entries.forEach { source ->
                    val selected = source in pendingFilter.sources
                    FilterChip(
                        selected = selected,
                        onClick = {
                            pendingFilter = pendingFilter.copy(
                                sources = if (selected) {
                                    pendingFilter.sources - source
                                } else {
                                    pendingFilter.sources + source
                                },
                            )
                        },
                        label = {
                            Text(
                                text = source.label,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        },
                    )
                }
            }
        }

        // Action buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = { pendingFilter = HttpLogFilter() },
            ) {
                Text("Clear")
            }

            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    viewModel.applyFilter(pendingFilter)
                    onDismiss()
                },
            ) {
                Text("Apply")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun DomainMultiSelectDropdown(
    availableHosts: List<String>,
    selectedHosts: Set<String>,
    onSelectionChange: (Set<String>) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    val displayText = when {
        selectedHosts.isEmpty() -> "All domains"
        selectedHosts.size == 1 -> selectedHosts.first()
        else -> "${selectedHosts.size} domains selected"
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            textStyle = MaterialTheme.typography.bodyMedium,
            trailingIcon = {
                if (selectedHosts.isNotEmpty()) {
                    IconButton(
                        onClick = { onSelectionChange(emptySet()) },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear domains",
                            modifier = Modifier.size(20.dp),
                        )
                    }
                } else {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            },
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 300.dp),
        ) {
            availableHosts.forEach { host ->
                val isSelected = host in selectedHosts
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = null,
                            )
                            Text(
                                text = host,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    },
                    onClick = {
                        onSelectionChange(
                            if (isSelected) selectedHosts - host else selectedHosts + host,
                        )
                    },
                )
            }
        }
    }

    // Selected domain chips
    if (selectedHosts.isNotEmpty()) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            selectedHosts.forEach { host ->
                FilterChip(
                    selected = true,
                    onClick = { onSelectionChange(selectedHosts - host) },
                    label = {
                        Text(
                            text = host,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove $host",
                            modifier = Modifier.size(16.dp),
                        )
                    },
                )
            }
        }
    }
}
