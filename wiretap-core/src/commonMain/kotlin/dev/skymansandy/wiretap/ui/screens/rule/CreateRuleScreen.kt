package dev.skymansandy.wiretap.ui.screens.rule

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.skymansandy.wiretap.data.db.entity.WiretapRule
import dev.skymansandy.wiretap.domain.model.RuleAction
import dev.skymansandy.wiretap.domain.model.UrlMatcher
import dev.skymansandy.wiretap.domain.repository.RuleRepository
import dev.skymansandy.wiretap.ui.common.PlatformBackHandler
import dev.skymansandy.wiretap.ui.rules.sections.RequestStep
import dev.skymansandy.wiretap.ui.rules.sections.ResponseStep
import dev.skymansandy.wiretap.ui.screens.rule.components.RegexTesterSheet
import dev.skymansandy.wiretap.ui.screens.rule.components.StepIndicator
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CreateRuleScreen(
    viewModel: CreateRuleViewModel,
    ruleRepository: RuleRepository,
    onBack: () -> Unit,
    onSaved: (WiretapRule?) -> Unit,
    onEditConflictingRule: ((WiretapRule) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val step by viewModel.step.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.resetStep()
    }

    // Request state
    val method by viewModel.method.collectAsStateWithLifecycle()
    val urlMode by viewModel.urlMode.collectAsStateWithLifecycle()
    val urlPattern by viewModel.urlPattern.collectAsStateWithLifecycle()
    val headerEntries by viewModel.headerEntries.collectAsStateWithLifecycle()
    val bodyMode by viewModel.bodyMode.collectAsStateWithLifecycle()
    val bodyPattern by viewModel.bodyPattern.collectAsStateWithLifecycle()

    // Response state
    val action by viewModel.action.collectAsStateWithLifecycle()
    val mockResponseCode by viewModel.mockResponseCode.collectAsStateWithLifecycle()
    val mockResponseBody by viewModel.mockResponseBody.collectAsStateWithLifecycle()
    val responseHeaderEntries by viewModel.responseHeaderEntries.collectAsStateWithLifecycle()
    val responseHeadersBulk by viewModel.responseHeadersBulk.collectAsStateWithLifecycle()
    val responseHeadersMode by viewModel.responseHeadersMode.collectAsStateWithLifecycle()
    val throttleDelayMs by viewModel.throttleDelayMs.collectAsStateWithLifecycle()
    val throttleDelayMaxMs by viewModel.throttleDelayMaxMs.collectAsStateWithLifecycle()
    val throttleInputMode by viewModel.throttleInputMode.collectAsStateWithLifecycle()

    // Regex tester
    val regexTesterPattern by viewModel.regexTesterPattern.collectAsStateWithLifecycle()
    val regexTesterLabel by viewModel.regexTesterLabel.collectAsStateWithLifecycle()
    val showRegexTester by viewModel.showRegexTester.collectAsStateWithLifecycle()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Conflict
    val conflictingRules by viewModel.conflictingRules.collectAsStateWithLifecycle()
    val showConflictDialog by viewModel.showConflictDialog.collectAsStateWithLifecycle()

    // Validation
    val canProceed by viewModel.canProceed.collectAsStateWithLifecycle()

    val testInputLabel = "Test Input"

    if (showRegexTester) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.closeRegexTester() },
            sheetState = bottomSheetState,
        ) {
            RegexTesterSheet(
                pattern = regexTesterPattern,
                testInputLabel = regexTesterLabel.ifEmpty { testInputLabel },
                onDismiss = { viewModel.closeRegexTester() },
            )
        }
    }

    PlatformBackHandler(enabled = step > 1) {
        viewModel.prevStep()
    }

    if (showConflictDialog && conflictingRules.isNotEmpty()) {
        ConflictDialog(
            conflictingRules = conflictingRules,
            ruleRepository = ruleRepository,
            onEditConflictingRule = onEditConflictingRule,
            onDismiss = { viewModel.dismissConflictDialog() },
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.isEditing) "Edit Rule" else "Create Rule") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (step > 1) {
                            viewModel.prevStep()
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            StepIndicator(
                currentStep = step,
                labels = listOf("Request", "Response"),
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
                        onMethodChange = { viewModel.updateMethod(it) },
                        urlMode = urlMode,
                        onUrlModeChange = { viewModel.updateUrlMode(it) },
                        urlPattern = urlPattern,
                        onUrlPatternChange = { viewModel.updateUrlPattern(it) },
                        headerEntries = headerEntries,
                        onHeaderAdd = { viewModel.addHeader() },
                        onHeaderUpdate = { idx, e -> viewModel.updateHeader(idx, e) },
                        onHeaderRemove = { idx -> viewModel.removeHeader(idx) },
                        bodyMode = bodyMode,
                        onBodyModeChange = { viewModel.updateBodyMode(it) },
                        bodyPattern = bodyPattern,
                        onBodyPatternChange = { viewModel.updateBodyPattern(it) },
                        onOpenRegexTester = { pattern, label -> viewModel.openRegexTester(pattern, label) },
                    )

                    2 -> ResponseStep(
                        action = action,
                        onActionChange = { viewModel.updateAction(it) },
                        mockResponseCode = mockResponseCode,
                        onMockResponseCodeChange = { viewModel.updateMockResponseCode(it) },
                        mockResponseBody = mockResponseBody,
                        onMockResponseBodyChange = { viewModel.updateMockResponseBody(it) },
                        responseHeaderEntries = responseHeaderEntries,
                        onResponseHeaderAdd = { viewModel.addResponseHeader() },
                        onResponseHeaderUpdate = { idx, e -> viewModel.updateResponseHeader(idx, e) },
                        onResponseHeaderRemove = { idx -> viewModel.removeResponseHeader(idx) },
                        responseHeadersBulk = responseHeadersBulk,
                        onResponseHeadersBulkChange = { viewModel.updateResponseHeadersBulk(it) },
                        responseHeadersMode = responseHeadersMode,
                        onResponseHeadersModeChange = { viewModel.updateResponseHeadersMode(it) },
                        throttleDelayMs = throttleDelayMs,
                        onThrottleDelayMsChange = { viewModel.updateThrottleDelayMs(it) },
                        throttleDelayMaxMs = throttleDelayMaxMs,
                        onThrottleDelayMaxMsChange = { viewModel.updateThrottleDelayMaxMs(it) },
                        throttleInputMode = throttleInputMode,
                        onThrottleInputModeChange = { viewModel.updateThrottleInputMode(it) },
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
                    OutlinedButton(onClick = { viewModel.prevStep() }, modifier = Modifier.weight(1f)) {
                        Text("Back")
                    }
                }
                if (step < 2) {
                    Button(
                        onClick = { viewModel.nextStep() },
                        enabled = canProceed,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Next: Response")
                    }
                } else {
                    Button(
                        onClick = { viewModel.saveRule(onSaved) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Save Rule")
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
    val scope = rememberCoroutineScope()
    val firstConflict = conflictingRules.first()
    val anyMethodLabel = "Any"
    val conflictSummary = conflictingRules.joinToString("\n") { rule ->
        buildString {
            append(if (rule.method == "*") anyMethodLabel else rule.method)
            rule.urlMatcher?.let { append(" ${it.pattern}") }
            append(" → ${rule.action.name}")
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rule Conflict") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (conflictingRules.size == 1) {
                        "An existing rule already matches the same requests:"
                    } else {
                        "${conflictingRules.size} existing rules already match the same requests:"
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
                    scope.launch {
                        val ruleToEdit = ruleRepository.getById(firstConflict.id)
                        if (ruleToEdit != null) {
                            onEditConflictingRule(ruleToEdit)
                        }
                    }
                }) {
                    Text("Edit Existing Rule")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Go Back")
            }
        },
    )
}

@Preview
@Composable
private fun Preview_ConflictDialogSingle() {
    MaterialTheme {
        ConflictDialog(
            conflictingRules = listOf(
                WiretapRule(
                    id = 1,
                    method = "GET",
                    urlMatcher = UrlMatcher.Contains("/api/users"),
                    action = RuleAction.Mock(responseCode = 200),
                    enabled = true,
                ),
            ),
            ruleRepository = NoOpRuleRepository,
            onEditConflictingRule = {},
            onDismiss = {},
        )
    }
}

@Preview
@Composable
private fun Preview_ConflictDialogMultiple() {
    MaterialTheme {
        ConflictDialog(
            conflictingRules = listOf(
                WiretapRule(
                    id = 1,
                    method = "GET",
                    urlMatcher = UrlMatcher.Contains("/api/users"),
                    action = RuleAction.Mock(responseCode = 200),
                    enabled = true,
                ),
                WiretapRule(
                    id = 2,
                    method = "*",
                    urlMatcher = UrlMatcher.Regex("/api/.*"),
                    action = RuleAction.Throttle(delayMs = 1000),
                    enabled = true,
                ),
            ),
            ruleRepository = NoOpRuleRepository,
            onEditConflictingRule = {},
            onDismiss = {},
        )
    }
}

private object NoOpRuleRepository : RuleRepository {
    override fun getAll() = flowOf(emptyList<WiretapRule>())
    override fun search(query: String) = flowOf(emptyList<WiretapRule>())
    override suspend fun getById(id: Long) = null
    override suspend fun getEnabledRules() = emptyList<WiretapRule>()
    override suspend fun addRule(rule: WiretapRule) {}
    override suspend fun updateRule(rule: WiretapRule) {}
    override suspend fun deleteById(id: Long) {}
    override suspend fun deleteAll() {}
    override suspend fun setEnabled(id: Long, enabled: Boolean) {}
}
