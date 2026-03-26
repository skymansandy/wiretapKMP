package dev.skymansandy.wiretap.ui.screens.console.http

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.skymansandy.wiretap.data.db.entity.HttpLogEntry
import dev.skymansandy.wiretap.domain.orchestrator.WiretapOrchestrator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

internal class HttpLogDetailViewModel(
    entryId: Long,
    private val orchestrator: WiretapOrchestrator,
) : ViewModel() {

    val entry: StateFlow<HttpLogEntry?>
        field = MutableStateFlow(null)

    init {
        viewModelScope.launch {
            entry.value = orchestrator.getHttpLogById(entryId)
        }
    }
}
