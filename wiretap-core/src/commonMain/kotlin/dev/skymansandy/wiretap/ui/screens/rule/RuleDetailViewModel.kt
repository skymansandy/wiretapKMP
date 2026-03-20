package dev.skymansandy.wiretap.ui.screens.rule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.skymansandy.wiretap.domain.repository.RuleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

internal class RuleDetailViewModel(
    private val ruleId: Long,
    initialEnabled: Boolean,
    private val ruleRepository: RuleRepository,
) : ViewModel() {

    val enabled: StateFlow<Boolean>
        field = MutableStateFlow(initialEnabled)

    val showDeleteConfirm: StateFlow<Boolean>
        field = MutableStateFlow(false)

    fun toggleEnabled(value: Boolean) {
        enabled.value = value
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
