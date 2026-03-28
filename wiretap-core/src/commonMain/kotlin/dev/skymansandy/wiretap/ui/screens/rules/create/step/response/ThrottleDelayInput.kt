package dev.skymansandy.wiretap.ui.screens.rules.create.step.response

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.ui.model.ThrottleInputMode
import dev.skymansandy.wiretap.ui.model.ThrottleProfile
import dev.skymansandy.wiretap.ui.model.labelText
import dev.skymansandy.wiretap.ui.model.speedText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ThrottleDelayInput(
    modifier: Modifier = Modifier,
    throttleDelayMs: String,
    onThrottleDelayMsChange: (String) -> Unit,
    throttleDelayMaxMs: String,
    onThrottleDelayMaxMsChange: (String) -> Unit,
    throttleInputMode: ThrottleInputMode,
    onThrottleInputModeChange: (ThrottleInputMode) -> Unit,
    supportingText: String,
) {
    Column(
        modifier = modifier,
    ) {
        Text(
            text = "Throttle Delay",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = throttleInputMode == ThrottleInputMode.None,
                label = { Text("None") },
                onClick = {
                    onThrottleInputModeChange(ThrottleInputMode.None)
                    onThrottleDelayMsChange("0")
                    onThrottleDelayMaxMsChange("0")
                },
            )

            FilterChip(
                selected = throttleInputMode == ThrottleInputMode.Manual,
                label = { Text("Manual") },
                onClick = { onThrottleInputModeChange(ThrottleInputMode.Manual) },
            )

            FilterChip(
                selected = throttleInputMode == ThrottleInputMode.Profile,
                label = { Text("Network Profile") },
                onClick = { onThrottleInputModeChange(ThrottleInputMode.Profile) },
            )
        }

        when (throttleInputMode) {
            ThrottleInputMode.None -> Unit

            ThrottleInputMode.Manual -> {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = throttleDelayMs,
                        onValueChange = onThrottleDelayMsChange,
                        label = { Text("Min (ms)") },
                        placeholder = { Text("e.g. 500") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )

                    OutlinedTextField(
                        value = throttleDelayMaxMs,
                        onValueChange = onThrottleDelayMaxMsChange,
                        label = { Text("Max (ms)") },
                        placeholder = { Text("e.g. 2000") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                }

                Text(
                    text = supportingText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            ThrottleInputMode.Profile -> {
                var expanded by remember { mutableStateOf(false) }
                val selectedProfile = ThrottleProfile.entries.find {
                    it.delayMinMs == throttleDelayMs.toLongOrNull() && it.delayMaxMs == throttleDelayMaxMs.toLongOrNull()
                }

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = {
                        expanded = it
                    },
                ) {
                    OutlinedTextField(
                        value = selectedProfile?.let {
                            "${it.labelText}  " +
                                "(${it.speedText} \u00B7 ${it.delayMinMs}\u2013${it.delayMaxMs}ms)"
                        } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Network Profile") },
                        placeholder = { Text("Select a profile") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                        singleLine = true,
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        ThrottleProfile.entries.forEach { profile ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Text(text = profile.labelText)

                                        Text(
                                            text = "${profile.speedText} \u00B7 ${profile.delayMinMs}\u2013${profile.delayMaxMs}ms",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                },
                                onClick = {
                                    onThrottleDelayMsChange(profile.delayMinMs.toString())
                                    onThrottleDelayMaxMsChange(profile.delayMaxMs.toString())
                                    expanded = false
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview_ThrottleDelayInputManual() {
    MaterialTheme {
        Column {
            ThrottleDelayInput(
                throttleDelayMs = "500",
                onThrottleDelayMsChange = {},
                throttleDelayMaxMs = "2000",
                onThrottleDelayMaxMsChange = {},
                throttleInputMode = ThrottleInputMode.Manual,
                onThrottleInputModeChange = {},
                supportingText = "Adds artificial latency to the response",
            )
        }
    }
}

@Preview
@Composable
private fun Preview_ThrottleDelayInputProfile() {
    MaterialTheme {
        Column {
            ThrottleDelayInput(
                throttleDelayMs = "150",
                onThrottleDelayMsChange = {},
                throttleDelayMaxMs = "450",
                onThrottleDelayMaxMsChange = {},
                throttleInputMode = ThrottleInputMode.Profile,
                onThrottleInputModeChange = {},
                supportingText = "Simulates network conditions",
            )
        }
    }
}
