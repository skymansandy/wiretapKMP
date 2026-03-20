package dev.skymansandy.wiretap.okhttp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

internal class OkHttpViewModel : ViewModel() {

    val statusLog: StateFlow<String>
        field = MutableStateFlow("")

    private val client = createOkHttpClient()

    fun executeAction(action: OkHttpAction) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                action.action(client) { statusLog.value = it }
            } catch (e: Exception) {
                statusLog.value = "Error: ${e.message}"
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        client.dispatcher.executorService.shutdown()
        client.connectionPool.evictAll()
    }
}
