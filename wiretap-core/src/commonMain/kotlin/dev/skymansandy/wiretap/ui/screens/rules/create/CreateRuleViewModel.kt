package dev.skymansandy.wiretap.ui.screens.rules.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.skymansandy.wiretap.domain.model.RuleAction
import dev.skymansandy.wiretap.domain.model.WiretapRule
import dev.skymansandy.wiretap.domain.model.matchers.BodyMatcher
import dev.skymansandy.wiretap.domain.model.matchers.UrlMatcher
import dev.skymansandy.wiretap.domain.orchestrator.HttpLogManager
import dev.skymansandy.wiretap.domain.repository.RuleRepository
import dev.skymansandy.wiretap.domain.usecase.FindConflictingRulesUseCase
import dev.skymansandy.wiretap.helper.util.HeadersSerializerUtil
import dev.skymansandy.wiretap.helper.util.currentTimeMillis
import dev.skymansandy.wiretap.ui.model.BodyMatchMode
import dev.skymansandy.wiretap.ui.model.HeaderEntry
import dev.skymansandy.wiretap.ui.model.HeaderEntryMode
import dev.skymansandy.wiretap.ui.model.ResponseHeaderEntry
import dev.skymansandy.wiretap.ui.model.ResponseHeadersEditMode
import dev.skymansandy.wiretap.ui.model.ThrottleInputMode
import dev.skymansandy.wiretap.ui.model.ThrottleProfile
import dev.skymansandy.wiretap.ui.model.UrlMatchMode
import dev.skymansandy.wiretap.ui.model.hasValue
import dev.skymansandy.wiretap.ui.model.toBodyMode
import dev.skymansandy.wiretap.ui.model.toDomain
import dev.skymansandy.wiretap.ui.model.toEntry
import dev.skymansandy.wiretap.ui.model.toUrlMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Suppress("TooManyFunctions")
internal class CreateRuleViewModel(
    private val existingRuleId: Long,
    private val prefillFromLogId: Long,
    private val httpLogManager: HttpLogManager,
    private val findConflictingRules: FindConflictingRulesUseCase,
    val ruleRepository: RuleRepository,
) : ViewModel() {

    val isEditing = existingRuleId > 0
    private var loadedRuleId: Long? = null
    private var loadedCreatedAt: Long? = null
    private var loadedEnabled: Boolean? = null

    val loaded: StateFlow<Boolean>
        field = MutableStateFlow(false)

    // Step
    val step: StateFlow<Int>
        field = MutableStateFlow(1)

    // Request state
    val method: StateFlow<String>
        field = MutableStateFlow("*")

    val urlMode: StateFlow<UrlMatchMode?>
        field = MutableStateFlow(null)

    val urlPattern: StateFlow<String>
        field = MutableStateFlow("")

    val headerEntries: StateFlow<List<HeaderEntry>>
        field = MutableStateFlow(emptyList())

    val bodyMode: StateFlow<BodyMatchMode?>
        field = MutableStateFlow(null)

    val bodyPattern: StateFlow<String>
        field = MutableStateFlow("")

    // Response state
    val action: StateFlow<RuleAction>
        field = MutableStateFlow<RuleAction>(RuleAction.Mock())

    val mockResponseCode: StateFlow<String>
        field = MutableStateFlow("200")

    val mockResponseBody: StateFlow<String>
        field = MutableStateFlow("")

    val responseHeaderEntries: StateFlow<List<ResponseHeaderEntry>>
        field = MutableStateFlow(emptyList())

    val responseHeadersBulk: StateFlow<String>
        field = MutableStateFlow("")

    val responseHeadersMode: StateFlow<ResponseHeadersEditMode>
        field = MutableStateFlow(ResponseHeadersEditMode.KeyValue)

    val throttleDelayMs: StateFlow<String>
        field = MutableStateFlow("")

    val throttleDelayMaxMs: StateFlow<String>
        field = MutableStateFlow("")

    val throttleInputMode: StateFlow<ThrottleInputMode>
        field = MutableStateFlow(ThrottleInputMode.None)

    // Regex tester
    val regexTesterPattern: StateFlow<String>
        field = MutableStateFlow("")

    val regexTesterLabel: StateFlow<String>
        field = MutableStateFlow("")

    val showRegexTester: StateFlow<Boolean>
        field = MutableStateFlow(false)

    // Conflict state
    val conflictingRules: StateFlow<List<WiretapRule>>
        field = MutableStateFlow(emptyList())

    val showConflictDialog: StateFlow<Boolean>
        field = MutableStateFlow(false)

    // Validation
    val canProceed: StateFlow<Boolean> = combine(
        urlMode,
        urlPattern,
        headerEntries,
        bodyMode,
        bodyPattern,
    ) { urlMode, urlPattern, headerEntries, bodyMode, bodyPattern ->
        val urlValid = urlMode == null || urlPattern.isNotBlank()
        val headersValid = headerEntries.all { e ->
            e.key.isNotBlank() && (!e.mode.hasValue() || e.value.isNotBlank())
        }
        val bodyValid = bodyMode == null || bodyPattern.isNotBlank()
        val hasSomeMatcher = urlMode != null || headerEntries.isNotEmpty() || bodyMode != null
        hasSomeMatcher && urlValid && headersValid && bodyValid
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false,
    )

    init {
        viewModelScope.launch {
            val existingRule = if (existingRuleId > 0) ruleRepository.getById(existingRuleId) else null
            val prefillFromLog = if (prefillFromLogId > 0) httpLogManager.getHttpLogById(prefillFromLogId) else null

            if (existingRule != null) {
                loadedRuleId = existingRule.id
                loadedCreatedAt = existingRule.createdAt
                loadedEnabled = existingRule.enabled

                method.value = existingRule.method
                urlMode.value = existingRule.toUrlMode()
                urlPattern.value = existingRule.urlMatcher?.pattern ?: ""
                headerEntries.value = existingRule.headerMatchers.map { it.toEntry() }
                bodyMode.value = existingRule.toBodyMode()
                bodyPattern.value = existingRule.bodyMatcher?.pattern ?: ""
                action.value = existingRule.action

                val existingMock = existingRule.action as? RuleAction.Mock
                val existingThrottle = existingRule.action as? RuleAction.Throttle
                mockResponseCode.value = existingMock?.responseCode?.toString() ?: "200"
                mockResponseBody.value = existingMock?.responseBody ?: ""
                responseHeaderEntries.value =
                    existingMock?.responseHeaders?.entries?.map { (k, v) -> ResponseHeaderEntry(k, v) }
                        ?: emptyList()
                responseHeadersBulk.value =
                    existingMock?.responseHeaders?.let { HeadersSerializerUtil.serialize(it) } ?: ""
                throttleDelayMs.value =
                    (existingMock?.throttleDelayMs ?: existingThrottle?.delayMs)?.toString() ?: ""
                throttleDelayMaxMs.value =
                    (existingMock?.throttleDelayMaxMs ?: existingThrottle?.delayMaxMs)?.toString() ?: ""

                val delayMs = existingMock?.throttleDelayMs ?: existingThrottle?.delayMs
                val delayMaxMs = existingMock?.throttleDelayMaxMs ?: existingThrottle?.delayMaxMs
                throttleInputMode.value = when {
                    delayMs == null ||
                        (delayMs == 0L && delayMaxMs.let { it == null || it == 0L }) ->
                        ThrottleInputMode.None
                    ThrottleProfile.entries.any {
                        it.delayMinMs == delayMs && it.delayMaxMs == delayMaxMs
                    } -> ThrottleInputMode.Profile
                    else -> ThrottleInputMode.Manual
                }
            } else if (prefillFromLog != null) {
                method.value = prefillFromLog.method
                urlMode.value = UrlMatchMode.Exact
                urlPattern.value = prefillFromLog.url
                headerEntries.value = prefillFromLog.requestHeaders?.map { (k, v) ->
                    HeaderEntry(key = k, value = v, mode = HeaderEntryMode.ValueExact)
                } ?: emptyList()
                if (prefillFromLog.requestBody != null) {
                    bodyMode.value = BodyMatchMode.Exact
                    bodyPattern.value = prefillFromLog.requestBody
                }
            }

            loaded.value = true
        }
    }

    fun nextStep() {
        step.value++
    }

    fun resetStep() {
        step.value = 1
    }

    fun prevStep() {
        step.value--
    }

    fun updateMethod(method: String) {
        this.method.value = method
    }

    fun updateUrlMode(mode: UrlMatchMode?) {
        urlMode.value = mode
        if (mode == null) urlPattern.value = ""
    }

    fun updateUrlPattern(pattern: String) {
        urlPattern.value = pattern
    }

    fun addHeader() {
        headerEntries.value += HeaderEntry()
    }

    fun updateHeader(index: Int, entry: HeaderEntry) {
        headerEntries.value =
            headerEntries.value.mapIndexed { i, v -> if (i == index) entry else v }
    }

    fun removeHeader(index: Int) {
        headerEntries.value = headerEntries.value.filterIndexed { i, _ -> i != index }
    }

    fun updateBodyMode(mode: BodyMatchMode?) {
        bodyMode.value = mode
        if (mode == null) bodyPattern.value = ""
    }

    fun updateBodyPattern(pattern: String) {
        bodyPattern.value = pattern
    }

    fun updateAction(action: RuleAction) {
        this.action.value = action
    }

    fun updateMockResponseCode(code: String) {
        mockResponseCode.value = code.filter { it.isDigit() }
    }

    fun updateMockResponseBody(body: String) {
        mockResponseBody.value = body
    }

    fun addResponseHeader() {
        responseHeaderEntries.value += ResponseHeaderEntry()
    }

    fun updateResponseHeader(index: Int, entry: ResponseHeaderEntry) {
        responseHeaderEntries.value =
            responseHeaderEntries.value.mapIndexed { i, v -> if (i == index) entry else v }
    }

    fun removeResponseHeader(index: Int) {
        responseHeaderEntries.value =
            responseHeaderEntries.value.filterIndexed { i, _ -> i != index }
    }

    fun updateResponseHeadersBulk(bulk: String) {
        responseHeadersBulk.value = bulk
    }

    fun updateResponseHeadersMode(newMode: ResponseHeadersEditMode) {
        when (newMode) {
            ResponseHeadersEditMode.KeyValue -> {
                val parsed = HeadersSerializerUtil.deserialize(responseHeadersBulk.value)
                responseHeaderEntries.value =
                    parsed.entries.map { (k, v) -> ResponseHeaderEntry(k, v) }
            }

            ResponseHeadersEditMode.BulkEdit -> {
                val map = responseHeaderEntries.value
                    .filter { e -> e.key.isNotBlank() }
                    .associate { e -> e.key.trim() to e.value.trim() }
                responseHeadersBulk.value = HeadersSerializerUtil.serialize(map)
            }
        }
        responseHeadersMode.value = newMode
    }

    fun updateThrottleDelayMs(delay: String) {
        throttleDelayMs.value = delay.filter { it.isDigit() }
    }

    fun updateThrottleDelayMaxMs(delay: String) {
        throttleDelayMaxMs.value = delay.filter { it.isDigit() }
    }

    fun updateThrottleInputMode(mode: ThrottleInputMode) {
        throttleInputMode.value = mode
        if (mode == ThrottleInputMode.None) {
            throttleDelayMs.value = "0"
            throttleDelayMaxMs.value = "0"
        }
    }

    fun openRegexTester(pattern: String, label: String) {
        regexTesterPattern.value = pattern
        regexTesterLabel.value = label
        showRegexTester.value = true
    }

    fun closeRegexTester() {
        showRegexTester.value = false
    }

    fun dismissConflictDialog() {
        showConflictDialog.value = false
        conflictingRules.value = emptyList()
    }

    fun saveRule(onSaved: (WiretapRule?) -> Unit) {
        viewModelScope.launch {
            val rule = buildRuleFromForm()
            val conflicts = findConflictingRules(rule)
            if (conflicts.isNotEmpty()) {
                conflictingRules.value = conflicts
                showConflictDialog.value = true
            } else {
                if (isEditing) {
                    ruleRepository.updateRule(rule)
                    onSaved(rule)
                } else {
                    ruleRepository.addRule(rule)
                    onSaved(null)
                }
            }
        }
    }

    private fun buildRuleFromForm(): WiretapRule {
        val resolvedHeaders: Map<String, String>? = when (responseHeadersMode.value) {
            ResponseHeadersEditMode.KeyValue ->
                responseHeaderEntries.value
                    .filter { e -> e.key.isNotBlank() }
                    .associate { e -> e.key.trim() to e.value.trim() }
                    .takeIf { m -> m.isNotEmpty() }

            ResponseHeadersEditMode.BulkEdit ->
                if (responseHeadersBulk.value.isNotBlank())
                    HeadersSerializerUtil.deserialize(responseHeadersBulk.value)
                        .takeIf { m -> m.isNotEmpty() }
                else null
        }
        val ruleAction = when (action.value) {
            is RuleAction.Mock -> RuleAction.Mock(
                responseCode = mockResponseCode.value.toIntOrNull() ?: 200,
                responseBody = mockResponseBody.value.ifBlank { null },
                responseHeaders = resolvedHeaders,
                throttleDelayMs = throttleDelayMs.value.toLongOrNull(),
                throttleDelayMaxMs = throttleDelayMaxMs.value.toLongOrNull(),
            )

            is RuleAction.Throttle -> RuleAction.Throttle(
                delayMs = throttleDelayMs.value.toLongOrNull() ?: 1000L,
                delayMaxMs = throttleDelayMaxMs.value.toLongOrNull(),
            )
        }
        return WiretapRule(
            id = loadedRuleId ?: 0,
            method = method.value.trim().ifBlank { "*" },
            urlMatcher = when (urlMode.value) {
                UrlMatchMode.Exact -> UrlMatcher.Exact(urlPattern.value.trim())
                UrlMatchMode.Contains -> UrlMatcher.Contains(urlPattern.value.trim())
                UrlMatchMode.Regex -> UrlMatcher.Regex(urlPattern.value.trim())
                null -> null
            },
            headerMatchers = headerEntries.value.mapNotNull { entry -> entry.toDomain() },
            bodyMatcher = when (bodyMode.value) {
                BodyMatchMode.Exact -> BodyMatcher.Exact(bodyPattern.value.trim())
                BodyMatchMode.Contains -> BodyMatcher.Contains(bodyPattern.value.trim())
                BodyMatchMode.Regex -> BodyMatcher.Regex(bodyPattern.value.trim())
                null -> null
            },
            action = ruleAction,
            enabled = loadedEnabled ?: true,
            createdAt = loadedCreatedAt ?: currentTimeMillis(),
        )
    }
}
