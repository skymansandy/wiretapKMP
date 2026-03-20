package dev.skymansandy.wiretap.ui.rules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.data.db.entity.NetworkLogEntry
import dev.skymansandy.wiretap.data.db.entity.WiretapRule
import dev.skymansandy.wiretap.domain.model.BodyMatcher
import dev.skymansandy.wiretap.domain.model.RuleAction
import dev.skymansandy.wiretap.domain.model.UrlMatcher
import dev.skymansandy.wiretap.domain.repository.RuleRepository
import dev.skymansandy.wiretap.ui.rules.components.RegexTesterSheet
import dev.skymansandy.wiretap.ui.rules.components.StepIndicator
import dev.skymansandy.wiretap.ui.rules.model.BodyMatchMode
import dev.skymansandy.wiretap.ui.rules.model.HeaderEntry
import dev.skymansandy.wiretap.ui.rules.model.ResponseHeaderEntry
import dev.skymansandy.wiretap.ui.rules.model.ResponseHeadersEditMode
import dev.skymansandy.wiretap.ui.rules.model.ThrottleInputMode
import dev.skymansandy.wiretap.ui.rules.model.ThrottleProfile
import dev.skymansandy.wiretap.ui.rules.model.UrlMatchMode
import dev.skymansandy.wiretap.ui.rules.model.hasValue
import dev.skymansandy.wiretap.ui.rules.model.toBodyMode
import dev.skymansandy.wiretap.ui.rules.model.toDomain
import dev.skymansandy.wiretap.ui.rules.model.toEntry
import dev.skymansandy.wiretap.ui.rules.model.toUrlMode
import dev.skymansandy.wiretap.ui.rules.sections.RequestStep
import dev.skymansandy.wiretap.ui.rules.sections.ResponseStep
import dev.skymansandy.wiretap.util.HeadersSerializerUtil
import dev.skymansandy.wiretap.util.currentTimeMillis
import dev.skymansandy.wiretap.resources.*
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CreateRuleScreen(
    ruleRepository: RuleRepository,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    existingRule: WiretapRule? = null,
    prefillFromLog: NetworkLogEntry? = null,
    onEditConflictingRule: ((WiretapRule) -> Unit)? = null,
) {
    val isEditing = existingRule != null
    var step by remember { mutableStateOf(1) }

    // Request state — pre-fill from log entry or existing rule
    var method by remember { mutableStateOf(existingRule?.method ?: prefillFromLog?.method ?: "*") }
    var urlMode by remember {
        mutableStateOf(existingRule?.toUrlMode() ?: if (prefillFromLog != null) UrlMatchMode.Contains else null)
    }
    var urlPattern by remember {
        mutableStateOf(existingRule?.urlMatcher?.pattern ?: prefillFromLog?.url ?: "")
    }
    var headerEntries by remember {
        mutableStateOf<List<HeaderEntry>>(existingRule?.headerMatchers?.map { it.toEntry() } ?: emptyList())
    }
    var bodyMode by remember { mutableStateOf(existingRule?.toBodyMode()) }
    var bodyPattern by remember { mutableStateOf(existingRule?.bodyMatcher?.pattern ?: "") }

    // Conflict state
    var conflictingRules by remember { mutableStateOf<List<WiretapRule>>(emptyList()) }
    var showConflictDialog by remember { mutableStateOf(false) }
    var pendingRule by remember { mutableStateOf<WiretapRule?>(null) }

    // Response state
    var action by remember { mutableStateOf(existingRule?.action ?: RuleAction.Mock) }
    var mockResponseCode by remember { mutableStateOf(existingRule?.mockResponseCode?.toString() ?: "200") }
    var mockResponseBody by remember { mutableStateOf(existingRule?.mockResponseBody ?: "") }
    var responseHeaderEntries by remember {
        mutableStateOf<List<ResponseHeaderEntry>>(
            existingRule?.mockResponseHeaders?.entries?.map { (k, v) -> ResponseHeaderEntry(k, v) } ?: emptyList(),
        )
    }
    var responseHeadersBulk by remember {
        mutableStateOf(existingRule?.mockResponseHeaders?.let { HeadersSerializerUtil.serialize(it) } ?: "")
    }
    var responseHeadersMode by remember { mutableStateOf(ResponseHeadersEditMode.KeyValue) }
    var throttleDelayMs by remember { mutableStateOf(existingRule?.throttleDelayMs?.toString() ?: "") }
    var throttleDelayMaxMs by remember { mutableStateOf(existingRule?.throttleDelayMaxMs?.toString() ?: "") }
    var throttleInputMode by remember {
        mutableStateOf(
            when {
                existingRule?.throttleDelayMs == null || (existingRule.throttleDelayMs == 0L && existingRule.throttleDelayMaxMs.let { it == null || it == 0L }) -> ThrottleInputMode.None
                ThrottleProfile.entries.any {
                    it.delayMinMs == existingRule.throttleDelayMs && it.delayMaxMs == existingRule.throttleDelayMaxMs
                } -> ThrottleInputMode.Profile
                else -> ThrottleInputMode.Manual
            }
        )
    }

    // Regex tester
    var regexTesterPattern by remember { mutableStateOf("") }
    var regexTesterLabel by remember { mutableStateOf("Test Input") }
    var showRegexTester by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Validation
    val urlValid = urlMode == null || urlPattern.isNotBlank()
    val headersValid = headerEntries.all { e ->
        e.key.isNotBlank() && (!e.mode.hasValue() || e.value.isNotBlank())
    }
    val bodyValid = bodyMode == null || bodyPattern.isNotBlank()
    val hasSomeMatcher = urlMode != null || headerEntries.isNotEmpty() || bodyMode != null
    val canProceed = hasSomeMatcher && urlValid && headersValid && bodyValid

    if (showRegexTester) {
        ModalBottomSheet(
            onDismissRequest = { showRegexTester = false },
            sheetState = bottomSheetState,
        ) {
            RegexTesterSheet(
                pattern = regexTesterPattern,
                testInputLabel = regexTesterLabel,
                onDismiss = { showRegexTester = false },
            )
        }
    }

    if (showConflictDialog && conflictingRules.isNotEmpty()) {
        ConflictDialog(
            conflictingRules = conflictingRules,
            ruleRepository = ruleRepository,
            onEditConflictingRule = onEditConflictingRule,
            onDismiss = {
                showConflictDialog = false
                conflictingRules = emptyList()
                pendingRule = null
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (isEditing) Res.string.edit_rule_title else Res.string.create_rule_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.back))
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            StepIndicator(
                currentStep = step,
                labels = listOf(stringResource(Res.string.step_request), stringResource(Res.string.step_response)),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )

            HorizontalDivider()

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                when (step) {
                    1 -> RequestStep(
                        method = method,
                        onMethodChange = { method = it },
                        urlMode = urlMode,
                        onUrlModeChange = {
                            urlMode = it
                            if (it == null) urlPattern = ""
                        },
                        urlPattern = urlPattern,
                        onUrlPatternChange = { urlPattern = it },
                        headerEntries = headerEntries,
                        onHeaderAdd = { headerEntries = headerEntries + HeaderEntry() },
                        onHeaderUpdate = { idx, e ->
                            headerEntries = headerEntries.mapIndexed { i, v -> if (i == idx) e else v }
                        },
                        onHeaderRemove = { idx ->
                            headerEntries = headerEntries.filterIndexed { i, _ -> i != idx }
                        },
                        bodyMode = bodyMode,
                        onBodyModeChange = {
                            bodyMode = it
                            if (it == null) bodyPattern = ""
                        },
                        bodyPattern = bodyPattern,
                        onBodyPatternChange = { bodyPattern = it },
                        onOpenRegexTester = { pattern, label ->
                            regexTesterPattern = pattern
                            regexTesterLabel = label
                            showRegexTester = true
                        },
                    )

                    2 -> ResponseStep(
                        action = action,
                        onActionChange = { action = it },
                        mockResponseCode = mockResponseCode,
                        onMockResponseCodeChange = { mockResponseCode = it.filter { c -> c.isDigit() } },
                        mockResponseBody = mockResponseBody,
                        onMockResponseBodyChange = { mockResponseBody = it },
                        responseHeaderEntries = responseHeaderEntries,
                        onResponseHeaderAdd = { responseHeaderEntries = responseHeaderEntries + ResponseHeaderEntry() },
                        onResponseHeaderUpdate = { idx, e ->
                            responseHeaderEntries = responseHeaderEntries.mapIndexed { i, v -> if (i == idx) e else v }
                        },
                        onResponseHeaderRemove = { idx ->
                            responseHeaderEntries = responseHeaderEntries.filterIndexed { i, _ -> i != idx }
                        },
                        responseHeadersBulk = responseHeadersBulk,
                        onResponseHeadersBulkChange = { responseHeadersBulk = it },
                        responseHeadersMode = responseHeadersMode,
                        onResponseHeadersModeChange = { newMode ->
                            when (newMode) {
                                ResponseHeadersEditMode.KeyValue -> {
                                    val parsed = HeadersSerializerUtil.deserialize(responseHeadersBulk)
                                    responseHeaderEntries = parsed.entries.map { (k, v) -> ResponseHeaderEntry(k, v) }
                                }
                                ResponseHeadersEditMode.BulkEdit -> {
                                    val map = responseHeaderEntries
                                        .filter { e -> e.key.isNotBlank() }
                                        .associate { e -> e.key.trim() to e.value.trim() }
                                    responseHeadersBulk = HeadersSerializerUtil.serialize(map)
                                }
                            }
                            responseHeadersMode = newMode
                        },
                        throttleDelayMs = throttleDelayMs,
                        onThrottleDelayMsChange = { throttleDelayMs = it.filter { c -> c.isDigit() } },
                        throttleDelayMaxMs = throttleDelayMaxMs,
                        onThrottleDelayMaxMsChange = { throttleDelayMaxMs = it.filter { c -> c.isDigit() } },
                        throttleInputMode = throttleInputMode,
                        onThrottleInputModeChange = { throttleInputMode = it },
                    )
                }
            }

            HorizontalDivider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (step > 1) {
                    OutlinedButton(onClick = { step-- }, modifier = Modifier.weight(1f)) {
                        Text(stringResource(Res.string.back))
                    }
                }
                if (step < 2) {
                    Button(
                        onClick = { step++ },
                        enabled = canProceed,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(Res.string.next_response))
                    }
                } else {
                    Button(
                        onClick = {
                            val resolvedHeaders: Map<String, String>? = when (responseHeadersMode) {
                                ResponseHeadersEditMode.KeyValue ->
                                    responseHeaderEntries
                                        .filter { e -> e.key.isNotBlank() }
                                        .associate { e -> e.key.trim() to e.value.trim() }
                                        .takeIf { m -> m.isNotEmpty() }
                                ResponseHeadersEditMode.BulkEdit ->
                                    if (responseHeadersBulk.isNotBlank())
                                        HeadersSerializerUtil.deserialize(responseHeadersBulk).takeIf { m -> m.isNotEmpty() }
                                    else null
                            }
                            val rule = WiretapRule(
                                id = existingRule?.id ?: 0,
                                method = method.trim().ifBlank { "*" },
                                urlMatcher = when (urlMode) {
                                    UrlMatchMode.Exact -> UrlMatcher.Exact(urlPattern.trim())
                                    UrlMatchMode.Contains -> UrlMatcher.Contains(urlPattern.trim())
                                    UrlMatchMode.Regex -> UrlMatcher.Regex(urlPattern.trim())
                                    null -> null
                                },
                                headerMatchers = headerEntries.mapNotNull { entry -> entry.toDomain() },
                                bodyMatcher = when (bodyMode) {
                                    BodyMatchMode.Exact -> BodyMatcher.Exact(bodyPattern.trim())
                                    BodyMatchMode.Contains -> BodyMatcher.Contains(bodyPattern.trim())
                                    BodyMatchMode.Regex -> BodyMatcher.Regex(bodyPattern.trim())
                                    null -> null
                                },
                                action = action,
                                mockResponseCode = if (action == RuleAction.Mock) mockResponseCode.toIntOrNull() ?: 200 else null,
                                mockResponseBody = if (action == RuleAction.Mock) mockResponseBody.ifBlank { null } else null,
                                mockResponseHeaders = if (action == RuleAction.Mock) resolvedHeaders else null,
                                throttleDelayMs = when (action) {
                                    RuleAction.Throttle -> throttleDelayMs.toLongOrNull() ?: 1000L
                                    RuleAction.Mock -> throttleDelayMs.toLongOrNull()
                                },
                                throttleDelayMaxMs = when (action) {
                                    RuleAction.Throttle -> throttleDelayMaxMs.toLongOrNull()
                                    RuleAction.Mock -> throttleDelayMaxMs.toLongOrNull()
                                },
                                enabled = existingRule?.enabled ?: true,
                                createdAt = existingRule?.createdAt ?: currentTimeMillis(),
                            )
                            val conflicts = ruleRepository.findConflictingRules(rule)
                            if (conflicts.isNotEmpty()) {
                                pendingRule = rule
                                conflictingRules = conflicts
                                showConflictDialog = true
                            } else {
                                if (isEditing) ruleRepository.updateRule(rule) else ruleRepository.addRule(rule)
                                onSaved()
                            }
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(Res.string.save_rule))
                    }
                }
            }
        }
    }
}

@Composable
private fun ConflictDialog(
    conflictingRules: List<WiretapRule>,
    ruleRepository: RuleRepository,
    onEditConflictingRule: ((WiretapRule) -> Unit)?,
    onDismiss: () -> Unit,
) {
    val firstConflict = conflictingRules.first()
    val anyMethodLabel = stringResource(Res.string.any_method)
    val conflictSummary = conflictingRules.joinToString("\n") { rule ->
        buildString {
            append(if (rule.method == "*") anyMethodLabel else rule.method)
            rule.urlMatcher?.let { append(" ${it.pattern}") }
            append(" → ${rule.action.name}")
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.rule_conflict)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (conflictingRules.size == 1) {
                        stringResource(Res.string.conflict_single)
                    } else {
                        stringResource(Res.string.conflict_multiple, conflictingRules.size)
                    },
                )
                Text(
                    text = conflictSummary,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                )
            }
        },
        confirmButton = {
            if (onEditConflictingRule != null) {
                TextButton(onClick = {
                    onDismiss()
                    val ruleToEdit = ruleRepository.getById(firstConflict.id)
                    if (ruleToEdit != null) {
                        onEditConflictingRule(ruleToEdit)
                    }
                }) {
                    Text(stringResource(Res.string.edit_existing_rule))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.discard))
            }
        },
    )
}
