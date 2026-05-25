/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.ui.screens.rules.criteria

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.skymansandy.wiretap.domain.model.HttpLog
import dev.skymansandy.wiretap.domain.orchestrator.HttpLogManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal data class SelectRuleCriteriaState(
    val httpLog: HttpLog? = null,
    val includeUrl: Boolean = true,
    val selectedHeaderKeys: Set<String> = emptySet(),
    val includeBody: Boolean = false,
) {

    val includeHeaders: Boolean
        get() = selectedHeaderKeys.isNotEmpty()

    val allHeadersSelected: Boolean
        get() = httpLog != null &&
            httpLog.requestHeaders.isNotEmpty() &&
            selectedHeaderKeys.containsAll(httpLog.requestHeaders.keys)
}

internal class SelectRuleCriteriaViewModel(
    private val logId: Long,
    private val httpLogManager: HttpLogManager,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    private val _state: MutableStateFlow<SelectRuleCriteriaState> = MutableStateFlow(SelectRuleCriteriaState())
    val state: StateFlow<SelectRuleCriteriaState> get() = _state

    init {
        viewModelScope.launch {
            val log = withContext(dispatcher) {
                httpLogManager.getHttpLogById(logId)
            } ?: return@launch
            _state.value = SelectRuleCriteriaState(
                httpLog = log,
                includeUrl = true,
                selectedHeaderKeys = emptySet(),
                includeBody = !log.requestBody.isNullOrEmpty(),
            )
        }
    }

    fun toggleUrl() {
        _state.update { it.copy(includeUrl = !it.includeUrl) }
    }

    fun toggleAllHeaders() {
        _state.update {
            if (it.allHeadersSelected) {
                it.copy(selectedHeaderKeys = emptySet())
            } else {
                it.copy(selectedHeaderKeys = it.httpLog?.requestHeaders?.keys ?: emptySet())
            }
        }
    }

    fun toggleHeaderKey(key: String) {
        _state.update {
            it.copy(
                selectedHeaderKeys = if (key in it.selectedHeaderKeys) {
                    it.selectedHeaderKeys - key
                } else {
                    it.selectedHeaderKeys + key
                },
            )
        }
    }

    fun toggleBody() {
        _state.update { it.copy(includeBody = !it.includeBody) }
    }
}
