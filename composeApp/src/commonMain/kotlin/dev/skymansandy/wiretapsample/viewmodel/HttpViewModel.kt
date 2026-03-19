package dev.skymansandy.wiretapsample.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.skymansandy.wiretapsample.model.ApiAction
import io.ktor.client.HttpClient
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.delay
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
        viewModelScope.launch(exceptionHandler) {
            try {
                action.action(client) { statusLog.value = it }
            } catch (e: Exception) {
                statusLog.value = "Error: ${e.message}"
            }
        }
    }

    fun executeCancelDemo() {
        viewModelScope.launch(exceptionHandler) {
            statusLog.value = "Starting request for cancellation..."
            val job = launch {
                try {
                    client.get("https://httpbin.org/delay/10") {
                        timeout { requestTimeoutMillis = 30_000 }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    // ignored
                }
            }
            delay(500)
            job.cancel()
            statusLog.value = "Request cancelled!"
        }
    }

    fun setStatusLog(status: String) {
        statusLog.value = status
    }
}
