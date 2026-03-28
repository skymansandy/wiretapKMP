package dev.skymansandy.wiretap.ui.screens.http.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.skymansandy.wiretap.domain.model.HttpLog
import dev.skymansandy.wiretap.domain.orchestrator.HttpLogManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

internal class HttpLogDetailViewModel(
    logId: Long,
    private val httpLogManager: HttpLogManager,
) : ViewModel() {

    val entry: StateFlow<HttpLog?>
        field = MutableStateFlow(null)

    init {
        viewModelScope.launch {
            entry.value = httpLogManager.getHttpLogById(logId)
        }
    }
}
