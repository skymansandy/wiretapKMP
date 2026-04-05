/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.ui.screens.rules.criteria

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.skymansandy.wiretap.domain.model.HttpLog
import dev.skymansandy.wiretap.domain.orchestrator.HttpLogManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
) : ViewModel() {

    val state: StateFlow<SelectRuleCriteriaState>
        field = MutableStateFlow(SelectRuleCriteriaState())

    init {
        viewModelScope.launch {
            val log = httpLogManager.getHttpLogById(logId) ?: return@launch
            state.value = SelectRuleCriteriaState(
                httpLog = log,
                includeUrl = true,
                selectedHeaderKeys = emptySet(),
                includeBody = !log.requestBody.isNullOrEmpty(),
            )
        }
    }

    fun toggleUrl() {
        state.update { it.copy(includeUrl = !it.includeUrl) }
    }

    fun toggleAllHeaders() {
        state.update {
            if (it.allHeadersSelected) {
                it.copy(selectedHeaderKeys = emptySet())
            } else {
                it.copy(selectedHeaderKeys = it.httpLog?.requestHeaders?.keys ?: emptySet())
            }
        }
    }

    fun toggleHeaderKey(key: String) {
        state.update {
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
        state.update { it.copy(includeBody = !it.includeBody) }
    }
}
