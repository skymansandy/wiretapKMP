package dev.skymansandy.wiretapsample.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.skymansandy.wiretapsample.model.HttpSampleActions
import dev.skymansandy.wiretapsample.model.SampleAction
import dev.skymansandy.wiretapsample.model.ktorHttpActions
import dev.skymansandy.wiretapsample.ui.theme.actionColor
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class KtorSampleViewModel(
    private val client: HttpClient,
) : ViewModel(), HttpSampleActions {

    private val _statusLog = MutableStateFlow("")
    override val statusLog: StateFlow<String> = _statusLog.asStateFlow()

    override val actions: List<SampleAction> = ktorHttpActions.map {
        SampleAction(it.label, actionColor.getValue(it.category))
    }

    private val exceptionHandler = CoroutineExceptionHandler { _, _ -> }

    override fun executeAction(index: Int) {
        val action = ktorHttpActions[index]
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            try {
                action.action(client) { _statusLog.value = it }
            } catch (e: Exception) {
                _statusLog.value = "Error: ${e.message}"
            }
        }
    }
}
