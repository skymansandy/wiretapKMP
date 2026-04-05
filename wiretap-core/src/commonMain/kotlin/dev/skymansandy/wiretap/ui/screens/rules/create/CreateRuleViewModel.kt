/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

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
import kotlinx.coroutines.flow.map
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
    val requestState = MutableStateFlow(RequestStepState())

    // Response state
    val responseState = MutableStateFlow(ResponseStepState())

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
    val canProceedToResponse: StateFlow<Boolean> = requestState.map { req ->
        val urlValid = req.urlMode == null || req.urlPattern.isNotBlank()
        val headersValid = req.headerEntries.all { e ->
            e.key.isNotBlank() && (!e.mode.hasValue() || e.value.isNotBlank())
        }
        val bodyValid = req.bodyMode == null || req.bodyPattern.isNotBlank()
        val hasSomeMatcher =
            req.urlMode != null || req.headerEntries.isNotEmpty() || req.bodyMode != null
        hasSomeMatcher && urlValid && headersValid && bodyValid
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false,
    )

    val canSaveRule: StateFlow<Boolean> = responseState.map { res ->
        when (res.action) {
            is RuleAction.Mock, is RuleAction.MockAndThrottle -> {
                val code = res.mockResponseCode.toIntOrNull()
                code != null && code in 100..599
            }
            is RuleAction.Throttle -> true
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true,
    )

    init {
        viewModelScope.launch {
            val existingRule =
                if (existingRuleId > 0) ruleRepository.getById(existingRuleId) else null
            val prefillFromLog =
                if (prefillFromLogId > 0) httpLogManager.getHttpLogById(prefillFromLogId) else null

            if (existingRule != null) {
                loadedRuleId = existingRule.id
                loadedCreatedAt = existingRule.createdAt
                loadedEnabled = existingRule.enabled

                requestState.value = RequestStepState(
                    method = existingRule.method,
                    urlMode = existingRule.toUrlMode(),
                    urlPattern = existingRule.urlMatcher?.pattern ?: "",
                    headerEntries = existingRule.headerMatchers.map { it.toEntry() },
                    bodyMode = existingRule.toBodyMode(),
                    bodyPattern = existingRule.bodyMatcher?.pattern ?: "",
                )

                val existingMock = existingRule.action as? RuleAction.Mock
                val existingThrottle = existingRule.action as? RuleAction.Throttle
                val existingMockAndThrottle = existingRule.action as? RuleAction.MockAndThrottle

                val mockCode = existingMock?.responseCode
                    ?: existingMockAndThrottle?.responseCode
                val mockBody = existingMock?.responseBody
                    ?: existingMockAndThrottle?.responseBody
                val mockHeaders = existingMock?.responseHeaders
                    ?: existingMockAndThrottle?.responseHeaders

                val delayMs = existingThrottle?.delayMs
                    ?: existingMockAndThrottle?.delayMs
                val delayMaxMs = existingThrottle?.delayMaxMs
                    ?: existingMockAndThrottle?.delayMaxMs

                responseState.value = ResponseStepState(
                    action = existingRule.action,
                    mockResponseCode = mockCode?.toString() ?: "200",
                    mockResponseBody = mockBody ?: "",
                    responseHeaderEntries = mockHeaders?.entries
                        ?.map { (k, v) -> ResponseHeaderEntry(k, v) } ?: emptyList(),
                    responseHeadersBulk = mockHeaders
                        ?.let { HeadersSerializerUtil.serialize(it) } ?: "",
                    throttleDelayMs = delayMs?.toString() ?: "",
                    throttleDelayMaxMs = delayMaxMs?.toString() ?: "",
                    throttleInputMode = when {
                        delayMs == null ||
                            (delayMs == 0L && delayMaxMs.let { it == null || it == 0L }) ->
                            ThrottleInputMode.None

                        ThrottleProfile.entries.any {
                            it.delayMinMs == delayMs && it.delayMaxMs == delayMaxMs
                        } -> ThrottleInputMode.Profile

                        else -> ThrottleInputMode.Manual
                    },
                )
            } else if (prefillFromLog != null) {
                requestState.value = RequestStepState(
                    method = prefillFromLog.method,
                    urlMode = UrlMatchMode.Exact,
                    urlPattern = prefillFromLog.url,
                    headerEntries = prefillFromLog.requestHeaders?.map { (k, v) ->
                        HeaderEntry(key = k, value = v, mode = HeaderEntryMode.ValueExact)
                    } ?: emptyList(),
                    bodyMode = if (prefillFromLog.requestBody != null) BodyMatchMode.Exact else null,
                    bodyPattern = prefillFromLog.requestBody ?: "",
                )
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

    // ── Request updates ─────────────────────────────────────────────────────────

    fun updateMethod(method: String) {
        requestState.updateRequest { copy(method = method) }
    }

    fun updateUrlMode(mode: UrlMatchMode?) {
        requestState.updateRequest { copy(urlMode = mode) }
    }

    fun updateUrlPattern(pattern: String) {
        requestState.updateRequest { copy(urlPattern = pattern) }
    }

    fun addHeader() {
        requestState.updateRequest { copy(headerEntries = headerEntries + HeaderEntry()) }
    }

    fun updateHeader(index: Int, entry: HeaderEntry) {
        requestState.updateRequest {
            copy(headerEntries = headerEntries.mapIndexed { i, v -> if (i == index) entry else v })
        }
    }

    fun removeHeader(index: Int) {
        requestState.updateRequest {
            copy(headerEntries = headerEntries.filterIndexed { i, _ -> i != index })
        }
    }

    fun updateBodyMode(mode: BodyMatchMode?) {
        requestState.updateRequest { copy(bodyMode = mode) }
    }

    fun updateBodyPattern(pattern: String) {
        requestState.updateRequest { copy(bodyPattern = pattern) }
    }

    // ── Response updates ────────────────────────────────────────────────────────

    fun updateAction(action: RuleAction) {
        responseState.updateResponse { copy(action = action) }
    }

    fun updateMockResponseCode(code: String) {
        responseState.updateResponse { copy(mockResponseCode = code.filter { it.isDigit() }) }
    }

    fun updateMockResponseBody(body: String) {
        responseState.updateResponse { copy(mockResponseBody = body) }
    }

    fun addResponseHeader() {
        responseState.updateResponse { copy(responseHeaderEntries = responseHeaderEntries + ResponseHeaderEntry()) }
    }

    fun updateResponseHeader(index: Int, entry: ResponseHeaderEntry) {
        responseState.updateResponse {
            copy(
                responseHeaderEntries = responseHeaderEntries.mapIndexed { i, v ->
                    if (i == index) entry else v
                },
            )
        }
    }

    fun removeResponseHeader(index: Int) {
        responseState.updateResponse {
            copy(responseHeaderEntries = responseHeaderEntries.filterIndexed { i, _ -> i != index })
        }
    }

    fun updateResponseHeadersBulk(bulk: String) {
        responseState.updateResponse { copy(responseHeadersBulk = bulk) }
    }

    fun updateResponseHeadersMode(newMode: ResponseHeadersEditMode) {
        responseState.updateResponse {
            when (newMode) {
                ResponseHeadersEditMode.KeyValue -> {
                    val parsed = HeadersSerializerUtil.deserialize(responseHeadersBulk)
                    copy(
                        responseHeadersMode = newMode,
                        responseHeaderEntries = parsed.entries.map { (k, v) ->
                            ResponseHeaderEntry(
                                k,
                                v,
                            )
                        },
                    )
                }

                ResponseHeadersEditMode.BulkEdit -> {
                    val map = responseHeaderEntries
                        .filter { e -> e.key.isNotBlank() }
                        .associate { e -> e.key.trim() to e.value.trim() }
                    copy(
                        responseHeadersMode = newMode,
                        responseHeadersBulk = HeadersSerializerUtil.serialize(map),
                    )
                }
            }
        }
    }

    fun updateThrottleDelayMs(delay: String) {
        responseState.updateResponse { copy(throttleDelayMs = delay.filter { it.isDigit() }) }
    }

    fun updateThrottleDelayMaxMs(delay: String) {
        responseState.updateResponse { copy(throttleDelayMaxMs = delay.filter { it.isDigit() }) }
    }

    fun updateThrottleInputMode(mode: ThrottleInputMode) {
        responseState.updateResponse {
            if (mode == ThrottleInputMode.None) {
                copy(throttleInputMode = mode, throttleDelayMs = "0", throttleDelayMaxMs = "0")
            } else {
                copy(throttleInputMode = mode)
            }
        }
    }

    // ── Regex tester ────────────────────────────────────────────────────────────

    fun openRegexTester(pattern: String, label: String) {
        regexTesterPattern.value = pattern
        regexTesterLabel.value = label
        showRegexTester.value = true
    }

    fun closeRegexTester() {
        showRegexTester.value = false
    }

    // ── Conflict ────────────────────────────────────────────────────────────────

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
        val req = requestState.value
        val res = responseState.value

        val resolvedHeaders: Map<String, String>? = when (res.responseHeadersMode) {
            ResponseHeadersEditMode.KeyValue ->
                res.responseHeaderEntries
                    .filter { e -> e.key.isNotBlank() }
                    .associate { e -> e.key.trim() to e.value.trim() }
                    .takeIf { m -> m.isNotEmpty() }

            ResponseHeadersEditMode.BulkEdit ->
                if (res.responseHeadersBulk.isNotBlank())
                    HeadersSerializerUtil.deserialize(res.responseHeadersBulk)
                        .takeIf { m -> m.isNotEmpty() }
                else null
        }

        val ruleAction = when (res.action) {
            is RuleAction.Mock -> RuleAction.Mock(
                responseCode = res.mockResponseCode.toIntOrNull() ?: 200,
                responseBody = res.mockResponseBody.ifBlank { null },
                responseHeaders = resolvedHeaders,
            )

            is RuleAction.Throttle -> RuleAction.Throttle(
                delayMs = res.throttleDelayMs.toLongOrNull() ?: 1000L,
                delayMaxMs = res.throttleDelayMaxMs.toLongOrNull(),
            )

            is RuleAction.MockAndThrottle -> RuleAction.MockAndThrottle(
                responseCode = res.mockResponseCode.toIntOrNull() ?: 200,
                responseBody = res.mockResponseBody.ifBlank { null },
                responseHeaders = resolvedHeaders,
                delayMs = res.throttleDelayMs.toLongOrNull() ?: 1000L,
                delayMaxMs = res.throttleDelayMaxMs.toLongOrNull(),
            )
        }

        return WiretapRule(
            id = loadedRuleId ?: 0,
            method = req.method.trim().ifBlank { "*" },
            urlMatcher = when (req.urlMode) {
                UrlMatchMode.Exact -> UrlMatcher.Exact(req.urlPattern.trim())
                UrlMatchMode.Contains -> UrlMatcher.Contains(req.urlPattern.trim())
                UrlMatchMode.Regex -> UrlMatcher.Regex(req.urlPattern.trim())
                null -> null
            },
            headerMatchers = req.headerEntries.mapNotNull { entry -> entry.toDomain() },
            bodyMatcher = when (req.bodyMode) {
                BodyMatchMode.Exact -> BodyMatcher.Exact(req.bodyPattern.trim())
                BodyMatchMode.Contains -> BodyMatcher.Contains(req.bodyPattern.trim())
                BodyMatchMode.Regex -> BodyMatcher.Regex(req.bodyPattern.trim())
                null -> null
            },
            action = ruleAction,
            enabled = loadedEnabled ?: true,
            createdAt = loadedCreatedAt ?: currentTimeMillis(),
        )
    }
}
