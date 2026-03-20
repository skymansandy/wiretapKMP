package dev.skymansandy.wiretap.ui.screens.rule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.skymansandy.wiretap.data.db.entity.HttpLogEntry
import dev.skymansandy.wiretap.data.db.entity.WiretapRule
import dev.skymansandy.wiretap.domain.model.BodyMatcher
import dev.skymansandy.wiretap.domain.model.RuleAction
import dev.skymansandy.wiretap.domain.model.UrlMatcher
import dev.skymansandy.wiretap.domain.repository.RuleRepository
import dev.skymansandy.wiretap.domain.usecase.FindConflictingRulesUseCase
import dev.skymansandy.wiretap.helper.util.HeadersSerializerUtil
import dev.skymansandy.wiretap.helper.util.currentTimeMillis
import dev.skymansandy.wiretap.ui.model.BodyMatchMode
import dev.skymansandy.wiretap.ui.model.HeaderEntry
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

internal class CreateRuleViewModel(
    private val ruleRepository: RuleRepository,
    private val findConflictingRules: FindConflictingRulesUseCase,
    existingRule: WiretapRule?,
    prefillFromLog: HttpLogEntry?,
) : ViewModel() {

    val isEditing = existingRule != null
    private val existingRuleId = existingRule?.id
    private val existingCreatedAt = existingRule?.createdAt
    private val existingEnabled = existingRule?.enabled

    // Step
    val step: StateFlow<Int>
        field = MutableStateFlow(1)

    // Request state
    val method: StateFlow<String>
        field = MutableStateFlow(existingRule?.method ?: prefillFromLog?.method ?: "*")

    val urlMode: StateFlow<UrlMatchMode?>
        field = MutableStateFlow(
            existingRule?.toUrlMode()
                ?: if (prefillFromLog != null) UrlMatchMode.Contains else null,
        )

    val urlPattern: StateFlow<String>
        field = MutableStateFlow(existingRule?.urlMatcher?.pattern ?: prefillFromLog?.url ?: "")

    val headerEntries: StateFlow<List<HeaderEntry>>
        field = MutableStateFlow(
            existingRule?.headerMatchers?.map { it.toEntry() } ?: emptyList(),
        )

    val bodyMode: StateFlow<BodyMatchMode?>
        field = MutableStateFlow(existingRule?.toBodyMode())

    val bodyPattern: StateFlow<String>
        field = MutableStateFlow(existingRule?.bodyMatcher?.pattern ?: "")

    // Response state
    private val existingMock = existingRule?.action as? RuleAction.Mock
    private val existingThrottle = existingRule?.action as? RuleAction.Throttle

    val action: StateFlow<RuleAction>
        field = MutableStateFlow(existingRule?.action ?: RuleAction.Mock())

    val mockResponseCode: StateFlow<String>
        field = MutableStateFlow(existingMock?.responseCode?.toString() ?: "200")

    val mockResponseBody: StateFlow<String>
        field = MutableStateFlow(existingMock?.responseBody ?: "")

    val responseHeaderEntries: StateFlow<List<ResponseHeaderEntry>>
        field = MutableStateFlow(
            existingMock?.responseHeaders?.entries?.map { (k, v) -> ResponseHeaderEntry(k, v) }
                ?: emptyList(),
        )

    val responseHeadersBulk: StateFlow<String>
        field = MutableStateFlow(
            existingMock?.responseHeaders?.let { HeadersSerializerUtil.serialize(it) } ?: "",
        )

    val responseHeadersMode: StateFlow<ResponseHeadersEditMode>
        field = MutableStateFlow(ResponseHeadersEditMode.KeyValue)

    val throttleDelayMs: StateFlow<String>
        field = MutableStateFlow(
            (existingMock?.throttleDelayMs ?: existingThrottle?.delayMs)?.toString() ?: "",
        )

    val throttleDelayMaxMs: StateFlow<String>
        field = MutableStateFlow(
            (existingMock?.throttleDelayMaxMs ?: existingThrottle?.delayMaxMs)?.toString() ?: "",
        )

    val throttleInputMode: StateFlow<ThrottleInputMode>
        field = MutableStateFlow(
            run {
                val existingDelayMs = existingMock?.throttleDelayMs ?: existingThrottle?.delayMs
                val existingDelayMaxMs =
                    existingMock?.throttleDelayMaxMs ?: existingThrottle?.delayMaxMs
                when {
                    existingDelayMs == null || (existingDelayMs == 0L && existingDelayMaxMs.let { it == null || it == 0L }) -> ThrottleInputMode.None
                    ThrottleProfile.entries.any {
                        it.delayMinMs == existingDelayMs && it.delayMaxMs == existingDelayMaxMs
                    } -> ThrottleInputMode.Profile

                    else -> ThrottleInputMode.Manual
                }
            },
        )

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

    fun nextStep() {
        step.value++
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

    fun saveRule(onSaved: () -> Unit) {
        viewModelScope.launch {
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
            val rule = WiretapRule(
                id = existingRuleId ?: 0,
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
                enabled = existingEnabled ?: true,
                createdAt = existingCreatedAt ?: currentTimeMillis(),
            )
            val conflicts = findConflictingRules(rule)
            if (conflicts.isNotEmpty()) {
                conflictingRules.value = conflicts
                showConflictDialog.value = true
            } else {
                if (isEditing) ruleRepository.updateRule(rule) else ruleRepository.addRule(rule)
                onSaved()
            }
        }
    }
}
