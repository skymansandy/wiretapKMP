package dev.skymansandy.wiretap.ui.screens.rule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.skymansandy.wiretap.data.db.entity.WiretapRule
import dev.skymansandy.wiretap.domain.repository.RuleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

internal class RuleDetailViewModel(
    private val ruleId: Long,
    private val ruleRepository: RuleRepository,
) : ViewModel() {

    val rule: StateFlow<WiretapRule?>
        field = MutableStateFlow(null)

    val enabled: StateFlow<Boolean>
        field = MutableStateFlow(false)

    val showDeleteConfirm: StateFlow<Boolean>
        field = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            val loaded = ruleRepository.getById(ruleId)
            rule.value = loaded
            enabled.value = loaded?.enabled ?: false
        }
    }

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
