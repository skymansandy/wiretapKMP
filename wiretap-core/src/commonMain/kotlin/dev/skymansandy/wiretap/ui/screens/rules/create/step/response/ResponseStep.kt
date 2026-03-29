package dev.skymansandy.wiretap.ui.screens.rules.create.step.response

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.skymansandy.jsoncmp.domain.ExperimentalJsonCmpApi
import dev.skymansandy.jsoncmp.ui.editor.JsonEditorCMP
import dev.skymansandy.jsoncmp.ui.editor.rememberJsonEditorState
import dev.skymansandy.wiretap.domain.model.RuleAction
import dev.skymansandy.wiretap.ui.screens.rules.create.CreateRuleViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalLayoutApi::class, ExperimentalJsonCmpApi::class)
@Composable
internal fun ResponseStep(
    modifier: Modifier = Modifier,
    viewModel: CreateRuleViewModel,
) {
    val state by viewModel.responseState.collectAsStateWithLifecycle()
    val action = state.action
    val mockResponseCode = state.mockResponseCode
    val mockResponseBody = state.mockResponseBody
    val responseHeaderEntries = state.responseHeaderEntries
    val responseHeadersBulk = state.responseHeadersBulk
    val responseHeadersMode = state.responseHeadersMode
    val throttleDelayMs = state.throttleDelayMs
    val throttleDelayMaxMs = state.throttleDelayMaxMs
    val throttleInputMode = state.throttleInputMode

    Column(
        modifier = modifier,
    ) {
        Text(
            text = "Action",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )

        FlowRow(
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

            FilterChip(
                selected = action is RuleAction.MockAndThrottle,
                onClick = { viewModel.updateAction(RuleAction.MockAndThrottle()) },
                label = { Text("Mock + Throttle") },
            )
        }

        when (action) {
            is RuleAction.Mock -> {
                MockResponseFields(
                    mockResponseCode = mockResponseCode,
                    mockResponseBody = mockResponseBody,
                    responseHeaderEntries = responseHeaderEntries,
                    responseHeadersBulk = responseHeadersBulk,
                    responseHeadersMode = responseHeadersMode,
                    viewModel = viewModel,
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

            is RuleAction.MockAndThrottle -> {
                ThrottleDelayInput(
                    throttleDelayMs = throttleDelayMs,
                    onThrottleDelayMsChange = { viewModel.updateThrottleDelayMs(it) },
                    throttleDelayMaxMs = throttleDelayMaxMs,
                    onThrottleDelayMaxMsChange = { viewModel.updateThrottleDelayMaxMs(it) },
                    throttleInputMode = throttleInputMode,
                    onThrottleInputModeChange = { viewModel.updateThrottleInputMode(it) },
                    supportingText = "Adds artificial latency before returning the mock response",
                )

                MockResponseFields(
                    mockResponseCode = mockResponseCode,
                    mockResponseBody = mockResponseBody,
                    responseHeaderEntries = responseHeaderEntries,
                    responseHeadersBulk = responseHeadersBulk,
                    responseHeadersMode = responseHeadersMode,
                    viewModel = viewModel,
                )
            }
        }
    }
}

@OptIn(ExperimentalJsonCmpApi::class, FlowPreview::class)
@Composable
private fun MockResponseFields(
    mockResponseCode: String,
    mockResponseBody: String,
    responseHeaderEntries: List<dev.skymansandy.wiretap.ui.model.ResponseHeaderEntry>,
    responseHeadersBulk: String,
    responseHeadersMode: dev.skymansandy.wiretap.ui.model.ResponseHeadersEditMode,
    viewModel: CreateRuleViewModel,
) {
    val codeInt = mockResponseCode.toIntOrNull()
    val isError = mockResponseCode.isNotEmpty() && (codeInt == null || codeInt !in 100..599)

    OutlinedTextField(
        value = mockResponseCode,
        onValueChange = { viewModel.updateMockResponseCode(it) },
        label = { Text("Response Code") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        isError = isError,
        supportingText = if (isError) {
            { Text("Must be 100–599") }
        } else {
            null
        },
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
    )

    LaunchedEffect(Unit) {
        snapshotFlow { editorState.json }
            .debounce(450.milliseconds)
            .collect {
                viewModel.updateMockResponseBody(editorState.json)
            }
    }

    JsonEditorCMP(
        modifier = Modifier.fillMaxWidth().height(300.dp),
        state = editorState,
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
