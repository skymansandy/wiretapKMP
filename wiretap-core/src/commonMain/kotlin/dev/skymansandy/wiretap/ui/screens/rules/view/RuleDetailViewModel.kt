/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.ui.screens.rules.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.skymansandy.wiretap.domain.model.WiretapRule
import dev.skymansandy.wiretap.domain.repository.RuleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class RuleDetailViewModel(
    private val ruleId: Long,
    private val ruleRepository: RuleRepository,
) : ViewModel() {

    val rule: StateFlow<WiretapRule?> = ruleRepository.flowById(ruleId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null,
        )

    val enabled: StateFlow<Boolean> = rule.map { it?.enabled ?: false }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false,
        )

    val showDeleteConfirm: StateFlow<Boolean>
        field = MutableStateFlow(false)

    fun toggleEnabled(value: Boolean) {
        viewModelScope.launch { ruleRepository.setEnabled(ruleId, value) }
    }

    fun requestDelete() {
        showDeleteConfirm.value = true
    }

    fun dismissDelete() {
        showDeleteConfirm.value = false
    }

    fun confirmDelete(onDeleted: () -> Unit) {
        viewModelScope.launch {
            ruleRepository.deleteById(ruleId)
            onDeleted()
        }
    }
}
