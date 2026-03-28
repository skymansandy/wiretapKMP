package dev.skymansandy.wiretap.ui.screens.rules.create.step.response

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.skymansandy.jsoncmp.JsonCMP
import dev.skymansandy.jsoncmp.config.rememberJsonEditorState
import dev.skymansandy.jsoncmp.helper.annotation.ExperimentalJsonCmpApi
import dev.skymansandy.wiretap.domain.model.RuleAction
import dev.skymansandy.wiretap.ui.screens.rules.create.CreateRuleViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalJsonCmpApi::class)
@Composable
internal fun ResponseStep(
    modifier: Modifier = Modifier,
    viewModel: CreateRuleViewModel,
) {
    val action by viewModel.action.collectAsStateWithLifecycle()
    val mockResponseCode by viewModel.mockResponseCode.collectAsStateWithLifecycle()
    val mockResponseBody by viewModel.mockResponseBody.collectAsStateWithLifecycle()
    val responseHeaderEntries by viewModel.responseHeaderEntries.collectAsStateWithLifecycle()
    val responseHeadersBulk by viewModel.responseHeadersBulk.collectAsStateWithLifecycle()
    val responseHeadersMode by viewModel.responseHeadersMode.collectAsStateWithLifecycle()
    val throttleDelayMs by viewModel.throttleDelayMs.collectAsStateWithLifecycle()
    val throttleDelayMaxMs by viewModel.throttleDelayMaxMs.collectAsStateWithLifecycle()
    val throttleInputMode by viewModel.throttleInputMode.collectAsStateWithLifecycle()

    Column(
        modifier = modifier,
    ) {
        Text(
            text = "Action",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = action is RuleAction.Mock,
                onClick = { viewModel.updateAction(RuleAction.Mock()) },
                label = { Text("Mock") },
            )

            FilterChip(
                selected = action is RuleAction.Throttle,
                onClick = { viewModel.updateAction(RuleAction.Throttle()) },
                label = { Text("Throttle") },
            )
        }

        when (action) {
            is RuleAction.Mock -> {
                ThrottleDelayInput(
                    throttleDelayMs = throttleDelayMs,
                    onThrottleDelayMsChange = { viewModel.updateThrottleDelayMs(it) },
                    throttleDelayMaxMs = throttleDelayMaxMs,
                    onThrottleDelayMaxMsChange = { viewModel.updateThrottleDelayMaxMs(it) },
                    throttleInputMode = throttleInputMode,
                    onThrottleInputModeChange = { viewModel.updateThrottleInputMode(it) },
                    supportingText = "Optional \u2014 adds artificial latency to this mock response",
                )

                OutlinedTextField(
                    value = mockResponseCode,
                    onValueChange = { viewModel.updateMockResponseCode(it) },
                    label = { Text("Response Code") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )

                Text(
                    "Response Body",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(4.dp))

                val editorState = rememberJsonEditorState(
                    initialJson = mockResponseBody.ifBlank { "{}" },
                    isEditing = true,
                )

                JsonCMP(
                    state = editorState,
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    onJsonChange = { json, _, _ -> viewModel.updateMockResponseBody(json) },
                )

                // Response headers with Key/Value ↔ Bulk Edit toggle
                ResponseHeadersSection(
                    modifier = Modifier.fillMaxWidth(),
                    entries = responseHeaderEntries,
                    onAdd = { viewModel.addResponseHeader() },
                    onUpdate = { idx, e -> viewModel.updateResponseHeader(idx, e) },
                    onRemove = { idx -> viewModel.removeResponseHeader(idx) },
                    bulk = responseHeadersBulk,
                    onBulkChange = { viewModel.updateResponseHeadersBulk(it) },
                    mode = responseHeadersMode,
                    onModeChange = { viewModel.updateResponseHeadersMode(it) },
                )
            }

            is RuleAction.Throttle -> {
                ThrottleDelayInput(
                    throttleDelayMs = throttleDelayMs,
                    onThrottleDelayMsChange = { viewModel.updateThrottleDelayMs(it) },
                    throttleDelayMaxMs = throttleDelayMaxMs,
                    onThrottleDelayMaxMsChange = { viewModel.updateThrottleDelayMaxMs(it) },
                    throttleInputMode = throttleInputMode,
                    onThrottleInputModeChange = { viewModel.updateThrottleInputMode(it) },
                    supportingText = "Adds artificial latency before the real network request",
                )
            }
        }
    }
}
