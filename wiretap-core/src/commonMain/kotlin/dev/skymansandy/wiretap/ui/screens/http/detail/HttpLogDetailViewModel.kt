package dev.skymansandy.wiretap.ui.screens.http.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.skymansandy.wiretap.domain.model.HttpLog
import dev.skymansandy.wiretap.domain.orchestrator.HttpLogManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

internal class HttpLogDetailViewModel(
    logId: Long,
    httpLogManager: HttpLogManager,
) : ViewModel() {

    val entry: StateFlow<HttpLog?> = httpLogManager.flowHttpLogById(logId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null,
        )
}
