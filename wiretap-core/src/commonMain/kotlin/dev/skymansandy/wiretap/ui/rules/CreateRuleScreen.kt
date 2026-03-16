package dev.skymansandy.wiretap.ui.rules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.data.db.entity.WiretapRule
import dev.skymansandy.wiretap.domain.model.BodyMatcher
import dev.skymansandy.wiretap.domain.model.HeaderMatcher
import dev.skymansandy.wiretap.domain.model.RuleAction
import dev.skymansandy.wiretap.domain.model.UrlMatcher
import dev.skymansandy.wiretap.domain.repository.RuleRepository
import dev.skymansandy.wiretap.util.HeadersSerializerUtil
import dev.skymansandy.wiretap.util.currentTimeMillis

// ── UI-layer enums (not persisted) ───────────────────────────────────────────

private enum class UrlMatchMode { EXACT, CONTAINS, REGEX }
private enum class BodyMatchMode { EXACT, CONTAINS, REGEX }
private enum class HeaderEntryMode { KEY_EXISTS, VALUE_EXACT, VALUE_CONTAINS, VALUE_REGEX }
private enum class ResponseHeadersEditMode { KEY_VALUE, BULK_EDIT }
private enum class ThrottleInputMode { MANUAL, PROFILE }

private enum class ThrottleProfile(
    val label: String,
    val speed: String,
    val delayMinMs: Long,
    val delayMaxMs: Long,
) {
    GPRS("2G (GPRS)", "~50 kbps", 1500, 3000),
    EDGE("2G (EDGE)", "~200 kbps", 800, 2000),
    SLOW_3G("3G (Slow)", "~400 kbps", 500, 1500),
    FAST_3G("3G", "~2 Mbps", 300, 800),
    SLOW_4G("4G (Slow)", "~5 Mbps", 150, 400),
    LTE("4G (LTE)", "~20 Mbps", 50, 200),
    SLOW_WIFI("Slow WiFi", "~1 Mbps", 500, 1000),
}

private data class HeaderEntry(
    val key: String = "",
    val value: String = "",
    val mode: HeaderEntryMode = HeaderEntryMode.KEY_EXISTS,
)

private data class ResponseHeaderEntry(
    val key: String = "",
    val value: String = "",
)

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun UrlMatchMode.label() = when (this) {
    UrlMatchMode.EXACT -> "Exact"
    UrlMatchMode.CONTAINS -> "Contains"
    UrlMatchMode.REGEX -> "Regex"
}

private fun BodyMatchMode.label() = when (this) {
    BodyMatchMode.EXACT -> "Exact"
    BodyMatchMode.CONTAINS -> "Contains"
    BodyMatchMode.REGEX -> "Regex"
}

private fun HeaderEntryMode.label() = when (this) {
    HeaderEntryMode.KEY_EXISTS -> "Key Exists"
    HeaderEntryMode.VALUE_EXACT -> "Exact"
    HeaderEntryMode.VALUE_CONTAINS -> "Contains"
    HeaderEntryMode.VALUE_REGEX -> "Regex"
}

private fun UrlMatchMode.isRegex() = this == UrlMatchMode.REGEX
private fun BodyMatchMode.isRegex() = this == BodyMatchMode.REGEX
private fun HeaderEntryMode.isRegex() = this == HeaderEntryMode.VALUE_REGEX
private fun HeaderEntryMode.hasValue() = this != HeaderEntryMode.KEY_EXISTS

private fun WiretapRule.toUrlMode() = when (urlMatcher) {
    is UrlMatcher.Exact -> UrlMatchMode.EXACT
    is UrlMatcher.Contains -> UrlMatchMode.CONTAINS
    is UrlMatcher.Regex -> UrlMatchMode.REGEX
    null -> null
}

private fun WiretapRule.toBodyMode() = when (bodyMatcher) {
    is BodyMatcher.Exact -> BodyMatchMode.EXACT
    is BodyMatcher.Contains -> BodyMatchMode.CONTAINS
    is BodyMatcher.Regex -> BodyMatchMode.REGEX
    null -> null
}

private fun HeaderMatcher.toEntry() = when (this) {
    is HeaderMatcher.KeyExists -> HeaderEntry(key = key, mode = HeaderEntryMode.KEY_EXISTS)
    is HeaderMatcher.ValueExact -> HeaderEntry(key = key, value = value, mode = HeaderEntryMode.VALUE_EXACT)
    is HeaderMatcher.ValueContains -> HeaderEntry(key = key, value = value, mode = HeaderEntryMode.VALUE_CONTAINS)
    is HeaderMatcher.ValueRegex -> HeaderEntry(key = key, value = pattern, mode = HeaderEntryMode.VALUE_REGEX)
}

private fun HeaderEntry.toDomain(): HeaderMatcher? {
    if (key.isBlank()) return null
    return when (mode) {
        HeaderEntryMode.KEY_EXISTS -> HeaderMatcher.KeyExists(key.trim())
        HeaderEntryMode.VALUE_EXACT -> HeaderMatcher.ValueExact(key.trim(), value.trim())
        HeaderEntryMode.VALUE_CONTAINS -> HeaderMatcher.ValueContains(key.trim(), value.trim())
        HeaderEntryMode.VALUE_REGEX -> HeaderMatcher.ValueRegex(key.trim(), value.trim())
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CreateRuleScreen(
    ruleRepository: RuleRepository,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    existingRule: WiretapRule? = null,
) {
    val isEditing = existingRule != null
    var step by remember { mutableStateOf(1) }

    // Request state
    var method by remember { mutableStateOf(existingRule?.method ?: "*") }
    var urlMode by remember { mutableStateOf(existingRule?.toUrlMode()) }
    var urlPattern by remember { mutableStateOf(existingRule?.urlMatcher?.pattern ?: "") }
    var headerEntries by remember {
        mutableStateOf<List<HeaderEntry>>(existingRule?.headerMatchers?.map { it.toEntry() } ?: emptyList())
    }
    var bodyMode by remember { mutableStateOf(existingRule?.toBodyMode()) }
    var bodyPattern by remember { mutableStateOf(existingRule?.bodyMatcher?.pattern ?: "") }

    // Response state
    var action by remember { mutableStateOf(existingRule?.action ?: RuleAction.MOCK) }
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
    var responseHeadersMode by remember { mutableStateOf(ResponseHeadersEditMode.KEY_VALUE) }
    var throttleDelayMs by remember { mutableStateOf(existingRule?.throttleDelayMs?.toString() ?: "") }
    var throttleDelayMaxMs by remember { mutableStateOf(existingRule?.throttleDelayMaxMs?.toString() ?: "") }
    var throttleInputMode by remember {
        mutableStateOf(
            if (existingRule?.throttleDelayMs != null &&
                ThrottleProfile.entries.any {
                    it.delayMinMs == existingRule.throttleDelayMs && it.delayMaxMs == existingRule.throttleDelayMaxMs
                }
            ) ThrottleInputMode.PROFILE else ThrottleInputMode.MANUAL
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Rule" else "Create Rule") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
                        onMethodChange = { method = it },
                        urlMode = urlMode,
                        onUrlModeChange = { urlMode = it; if (it == null) urlPattern = "" },
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
                        onBodyModeChange = { bodyMode = it; if (it == null) bodyPattern = "" },
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
                                ResponseHeadersEditMode.KEY_VALUE -> {
                                    val parsed = HeadersSerializerUtil.deserialize(responseHeadersBulk)
                                    responseHeaderEntries = parsed.entries.map { (k, v) -> ResponseHeaderEntry(k, v) }
                                }
                                ResponseHeadersEditMode.BULK_EDIT -> {
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
                        Text("Back")
                    }
                }
                if (step < 2) {
                    Button(
                        onClick = { step++ },
                        enabled = canProceed,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Next: Response")
                    }
                } else {
                    Button(
                        onClick = {
                            val resolvedHeaders: Map<String, String>? = when (responseHeadersMode) {
                                ResponseHeadersEditMode.KEY_VALUE ->
                                    responseHeaderEntries
                                        .filter { e -> e.key.isNotBlank() }
                                        .associate { e -> e.key.trim() to e.value.trim() }
                                        .takeIf { m -> m.isNotEmpty() }
                                ResponseHeadersEditMode.BULK_EDIT ->
                                    if (responseHeadersBulk.isNotBlank())
                                        HeadersSerializerUtil.deserialize(responseHeadersBulk).takeIf { m -> m.isNotEmpty() }
                                    else null
                            }
                            val rule = WiretapRule(
                                id = existingRule?.id ?: 0,
                                method = method.trim().ifBlank { "*" },
                                urlMatcher = when (urlMode) {
                                    UrlMatchMode.EXACT -> UrlMatcher.Exact(urlPattern.trim())
                                    UrlMatchMode.CONTAINS -> UrlMatcher.Contains(urlPattern.trim())
                                    UrlMatchMode.REGEX -> UrlMatcher.Regex(urlPattern.trim())
                                    null -> null
                                },
                                headerMatchers = headerEntries.mapNotNull { entry -> entry.toDomain() },
                                bodyMatcher = when (bodyMode) {
                                    BodyMatchMode.EXACT -> BodyMatcher.Exact(bodyPattern.trim())
                                    BodyMatchMode.CONTAINS -> BodyMatcher.Contains(bodyPattern.trim())
                                    BodyMatchMode.REGEX -> BodyMatcher.Regex(bodyPattern.trim())
                                    null -> null
                                },
                                action = action,
                                mockResponseCode = if (action == RuleAction.MOCK) mockResponseCode.toIntOrNull() ?: 200 else null,
                                mockResponseBody = if (action == RuleAction.MOCK) mockResponseBody.ifBlank { null } else null,
                                mockResponseHeaders = if (action == RuleAction.MOCK) resolvedHeaders else null,
                                throttleDelayMs = when (action) {
                                    RuleAction.THROTTLE -> throttleDelayMs.toLongOrNull() ?: 1000L
                                    RuleAction.MOCK -> throttleDelayMs.toLongOrNull()
                                },
                                throttleDelayMaxMs = when (action) {
                                    RuleAction.THROTTLE -> throttleDelayMaxMs.toLongOrNull()
                                    RuleAction.MOCK -> throttleDelayMaxMs.toLongOrNull()
                                },
                                enabled = existingRule?.enabled ?: true,
                                createdAt = existingRule?.createdAt ?: currentTimeMillis(),
                            )
                            if (isEditing) ruleRepository.updateRule(rule) else ruleRepository.addRule(rule)
                            onSaved()
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Save Rule")
                    }
                }
            }
        }
    }
}

// ── Step 1: Request ───────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun RequestStep(
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
            label = { Text("None") },
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
            label = { Text("URL ${urlMode.label()}") },
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
        Text("+ Add Header Condition")
    }

    // ── Body ──────────────────────────────────────────────────────────────────
    SectionLabel("Body")
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = bodyMode == null,
            onClick = { onBodyModeChange(null) },
            label = { Text("None") },
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
            label = { Text("Body ${bodyMode.label()}") },
            placeholder = { Text(bodyPlaceholder(bodyMode)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            trailingIcon = if (bodyMode.isRegex()) {
                { RegexTesterIcon { onOpenRegexTester(bodyPattern, "Test Body") } }
            } else null,
        )
    }
}

// ── Header matcher item ───────────────────────────────────────────────────────

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
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {

            // Key / Value row (50 / 50)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = entry.key,
                    onValueChange = { onUpdate(entry.copy(key = it)) },
                    label = { Text("Key") },
                    placeholder = { Text("Authorization") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                if (entry.mode.hasValue()) {
                    OutlinedTextField(
                        value = entry.value,
                        onValueChange = { onUpdate(entry.copy(value = it)) },
                        label = { Text(if (entry.mode.isRegex()) "Value Regex" else "Value") },
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
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

// ── Step 2: Response ──────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ResponseStep(
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
    Text("Action", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = action == RuleAction.MOCK,
            onClick = { onActionChange(RuleAction.MOCK) },
            label = { Text("Mock") },
        )
        FilterChip(
            selected = action == RuleAction.THROTTLE,
            onClick = { onActionChange(RuleAction.THROTTLE) },
            label = { Text("Throttle") },
        )
    }

    when (action) {
        RuleAction.MOCK -> {
            ThrottleDelayInput(
                throttleDelayMs = throttleDelayMs,
                onThrottleDelayMsChange = onThrottleDelayMsChange,
                throttleDelayMaxMs = throttleDelayMaxMs,
                onThrottleDelayMaxMsChange = onThrottleDelayMaxMsChange,
                throttleInputMode = throttleInputMode,
                onThrottleInputModeChange = onThrottleInputModeChange,
                supportingText = "Optional — adds artificial latency to this mock response",
            )

            OutlinedTextField(
                value = mockResponseCode,
                onValueChange = onMockResponseCodeChange,
                label = { Text("Response Code") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            OutlinedTextField(
                value = mockResponseBody,
                onValueChange = onMockResponseBodyChange,
                label = { Text("Response Body") },
                placeholder = { Text("{\"key\": \"value\"}") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                maxLines = 10,
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
        RuleAction.THROTTLE -> {
            ThrottleDelayInput(
                throttleDelayMs = throttleDelayMs,
                onThrottleDelayMsChange = onThrottleDelayMsChange,
                throttleDelayMaxMs = throttleDelayMaxMs,
                onThrottleDelayMaxMsChange = onThrottleDelayMaxMsChange,
                throttleInputMode = throttleInputMode,
                onThrottleInputModeChange = onThrottleInputModeChange,
                supportingText = "Adds artificial latency before the real network request",
            )
        }
    }
}

// ── Throttle delay input (Manual / Profile) ──────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThrottleDelayInput(
    throttleDelayMs: String,
    onThrottleDelayMsChange: (String) -> Unit,
    throttleDelayMaxMs: String,
    onThrottleDelayMaxMsChange: (String) -> Unit,
    throttleInputMode: ThrottleInputMode,
    onThrottleInputModeChange: (ThrottleInputMode) -> Unit,
    supportingText: String,
) {
    Text("Throttle Delay", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = throttleInputMode == ThrottleInputMode.MANUAL,
            onClick = { onThrottleInputModeChange(ThrottleInputMode.MANUAL) },
            label = { Text("Manual") },
        )
        FilterChip(
            selected = throttleInputMode == ThrottleInputMode.PROFILE,
            onClick = { onThrottleInputModeChange(ThrottleInputMode.PROFILE) },
            label = { Text("Network Profile") },
        )
    }

    when (throttleInputMode) {
        ThrottleInputMode.MANUAL -> {
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
        ThrottleInputMode.PROFILE -> {
            var expanded by remember { mutableStateOf(false) }
            val selectedProfile = ThrottleProfile.entries.find {
                it.delayMinMs == throttleDelayMs.toLongOrNull() && it.delayMaxMs == throttleDelayMaxMs.toLongOrNull()
            }

            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = selectedProfile?.let { "${it.label}  (${it.speed} · ${it.delayMinMs}–${it.delayMaxMs}ms)" } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Network Profile") },
                    placeholder = { Text("Select a profile") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    singleLine = true,
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    ThrottleProfile.entries.forEach { profile ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(profile.label)
                                    Text(
                                        "${profile.speed} · ${profile.delayMinMs}–${profile.delayMaxMs}ms",
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

// ── Response headers section (Key/Value ↔ Bulk Edit) ─────────────────────────

@Composable
private fun ResponseHeadersSection(
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
            text = "Response Headers",
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
                contentDescription = if (mode == ResponseHeadersEditMode.KEY_VALUE) "Switch to bulk edit" else "Switch to key/value",
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
                Text("+ Add Header")
            }
        }
        ResponseHeadersEditMode.BULK_EDIT -> {
            OutlinedTextField(
                value = bulk,
                onValueChange = onBulkChange,
                label = { Text("Headers") },
                placeholder = { Text("Content-Type: application/json\nCache-Control: no-cache") },
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
            label = { Text("Key") },
            placeholder = { Text("Content-Type") },
            modifier = Modifier.weight(1f),
            singleLine = true,
        )
        OutlinedTextField(
            value = entry.value,
            onValueChange = { onUpdate(entry.copy(value = it)) },
            label = { Text("Value") },
            placeholder = { Text("application/json") },
            modifier = Modifier.weight(1f),
            singleLine = true,
        )
        IconButton(onClick = onRemove) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove",
                tint = MaterialTheme.colorScheme.error,
            )
        }
    }
}

// ── Shared composables ────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.width(8.dp))
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun RegexTesterIcon(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(Icons.Default.PlayArrow, contentDescription = "Test regex", tint = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun StepIndicator(currentStep: Int, labels: List<String>, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        labels.forEachIndexed { index, label ->
            val step = index + 1
            val isActive = step == currentStep
            val isCompleted = step < currentStep

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        color = if (isActive || isCompleted) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (isCompleted) {
                    Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                } else {
                    Text(
                        text = step.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.padding(start = 6.dp),
            )

            if (index < labels.size - 1) {
                HorizontalDivider(modifier = Modifier.weight(1f).padding(horizontal = 12.dp))
            }
        }
    }
}

@Composable
private fun RegexTesterSheet(
    pattern: String,
    testInputLabel: String,
    onDismiss: () -> Unit,
) {
    var testInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Regex Tester",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = pattern,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(10.dp),
            )
        }

        OutlinedTextField(
            value = testInput,
            onValueChange = { testInput = it },
            label = { Text(testInputLabel) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        if (testInput.isNotBlank()) {
            val result = testRegex(pattern, testInput)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (result.matches) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = if (result.matches) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(end = 8.dp),
                )
                Column {
                    Text(
                        text = if (result.matches) "Match found" else "No match",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (result.matches) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                    )
                    if (result.error != null) {
                        Text(
                            text = result.error,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MethodSelector(method: String, onMethodChange: (String) -> Unit) {
    val methods = listOf("*", "GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS")
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = if (method == "*") "Any" else method,
            onValueChange = {},
            readOnly = true,
            label = { Text("HTTP Method") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            methods.forEach { m ->
                DropdownMenuItem(
                    text = { Text(if (m == "*") "Any" else m) },
                    onClick = { onMethodChange(m); expanded = false },
                )
            }
        }
    }
}

// ── Pure helpers ──────────────────────────────────────────────────────────────

private data class RegexTestResult(val matches: Boolean, val error: String?)

private fun testRegex(pattern: String, input: String): RegexTestResult {
    return try {
        RegexTestResult(matches = pattern.toRegex(RegexOption.IGNORE_CASE).containsMatchIn(input), error = null)
    } catch (e: Exception) {
        RegexTestResult(matches = false, error = "Invalid regex: ${e.message}")
    }
}

private fun urlPlaceholder(mode: UrlMatchMode) = when (mode) {
    UrlMatchMode.EXACT -> "https://api.example.com/users/123"
    UrlMatchMode.CONTAINS -> "/users/"
    UrlMatchMode.REGEX -> "api\\.example\\.com/users/\\d+"
}

private fun bodyPlaceholder(mode: BodyMatchMode) = when (mode) {
    BodyMatchMode.EXACT -> "{\"status\": \"error\"}"
    BodyMatchMode.CONTAINS -> "\"error\""
    BodyMatchMode.REGEX -> "\"id\":\\s*\\d+"
}

private fun headerValuePlaceholder(mode: HeaderEntryMode) = when (mode) {
    HeaderEntryMode.VALUE_EXACT -> "Bearer token123"
    HeaderEntryMode.VALUE_CONTAINS -> "Bearer"
    HeaderEntryMode.VALUE_REGEX -> "Bearer\\s+\\S+"
    else -> ""
}
