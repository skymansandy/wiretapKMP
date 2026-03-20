package dev.skymansandy.wiretapsample.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.skymansandy.wiretapsample.model.ApiAction
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

internal class HttpViewModel(
    private val client: HttpClient,
) : ViewModel() {

    val statusLog: StateFlow<String>
        field = MutableStateFlow("")

    private val exceptionHandler = CoroutineExceptionHandler { _, _ -> }

    fun executeAction(action: ApiAction) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            try {
                action.action(client) { statusLog.value = it }
            } catch (e: Exception) {
                statusLog.value = "Error: ${e.message}"
            }
        }
    }
}
