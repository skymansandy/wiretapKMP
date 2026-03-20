package dev.skymansandy.wiretap.ui.rules.sections

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.skymansandy.jsoncmp.JsonCMP
import dev.skymansandy.jsoncmp.config.rememberJsonEditorState
import dev.skymansandy.wiretap.domain.model.RuleAction
import dev.skymansandy.wiretap.resources.Res
import dev.skymansandy.wiretap.resources.label_action
import dev.skymansandy.wiretap.resources.mock
import dev.skymansandy.wiretap.resources.mock_latency_hint
import dev.skymansandy.wiretap.resources.response_body
import dev.skymansandy.wiretap.resources.response_code_label
import dev.skymansandy.wiretap.resources.throttle
import dev.skymansandy.wiretap.resources.throttle_latency_hint
import dev.skymansandy.wiretap.ui.model.ResponseHeaderEntry
import dev.skymansandy.wiretap.ui.model.ResponseHeadersEditMode
import dev.skymansandy.wiretap.ui.model.ThrottleInputMode
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ResponseStep(
    action: RuleAction,
    onActionChange: (RuleAction) -> Unit,
    mockResponseCode: String,
    onMockResponseCodeChange: (String) -> Unit,
    mockResponseBody: String,
    onMockResponseBodyChange: (String) -> Unit,
    responseHeaderEntries: List<ResponseHeaderEntry>,
    onResponseHeaderAdd: () -> Unit,
    onResponseHeaderUpdate: (Int, ResponseHeaderEntry) -> Unit,
    onResponseHeaderRemove: (Int) -> Unit,
    responseHeadersBulk: String,
    onResponseHeadersBulkChange: (String) -> Unit,
    responseHeadersMode: ResponseHeadersEditMode,
    onResponseHeadersModeChange: (ResponseHeadersEditMode) -> Unit,
    throttleDelayMs: String,
    onThrottleDelayMsChange: (String) -> Unit,
    throttleDelayMaxMs: String,
    onThrottleDelayMaxMsChange: (String) -> Unit,
    throttleInputMode: ThrottleInputMode,
    onThrottleInputModeChange: (ThrottleInputMode) -> Unit,
) {
    Text(
        text = stringResource(Res.string.label_action),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = action is RuleAction.Mock,
            onClick = { onActionChange(RuleAction.Mock()) },
            label = { Text(stringResource(Res.string.mock)) },
        )

        FilterChip(
            selected = action is RuleAction.Throttle,
            onClick = { onActionChange(RuleAction.Throttle()) },
            label = { Text(stringResource(Res.string.throttle)) },
        )
    }

    when (action) {
        is RuleAction.Mock -> {
            ThrottleDelayInput(
                throttleDelayMs = throttleDelayMs,
                onThrottleDelayMsChange = onThrottleDelayMsChange,
                throttleDelayMaxMs = throttleDelayMaxMs,
                onThrottleDelayMaxMsChange = onThrottleDelayMaxMsChange,
                throttleInputMode = throttleInputMode,
                onThrottleInputModeChange = onThrottleInputModeChange,
                supportingText = stringResource(Res.string.mock_latency_hint),
            )

            OutlinedTextField(
                value = mockResponseCode,
                onValueChange = onMockResponseCodeChange,
                label = { Text(stringResource(Res.string.response_code_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )

            Text(
                stringResource(Res.string.response_body),
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
                modifier = Modifier.fillMaxWidth(),
                onJsonChange = { json, _, _ -> onMockResponseBodyChange(json) },
            )

            // Response headers with Key/Value ↔ Bulk Edit toggle
            ResponseHeadersSection(
                entries = responseHeaderEntries,
                onAdd = onResponseHeaderAdd,
                onUpdate = onResponseHeaderUpdate,
                onRemove = onResponseHeaderRemove,
                bulk = responseHeadersBulk,
                onBulkChange = onResponseHeadersBulkChange,
                mode = responseHeadersMode,
                onModeChange = onResponseHeadersModeChange,
            )
        }

        is RuleAction.Throttle -> {
            ThrottleDelayInput(
                throttleDelayMs = throttleDelayMs,
                onThrottleDelayMsChange = onThrottleDelayMsChange,
                throttleDelayMaxMs = throttleDelayMaxMs,
                onThrottleDelayMaxMsChange = onThrottleDelayMaxMsChange,
                throttleInputMode = throttleInputMode,
                onThrottleInputModeChange = onThrottleInputModeChange,
                supportingText = stringResource(Res.string.throttle_latency_hint),
            )
        }
    }
}
