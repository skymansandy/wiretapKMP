package dev.skymansandy.wiretap.presentation.okhttp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.skymansandy.wiretapsample.model.HttpSampleActions
import dev.skymansandy.wiretapsample.model.SampleAction
import dev.skymansandy.wiretapsample.ui.theme.actionColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

internal class OkHttpViewModel(
    private val client: OkHttpClient,
) : ViewModel(), HttpSampleActions {

    private val _statusLog = MutableStateFlow("")
    override val statusLog: StateFlow<String> = _statusLog.asStateFlow()

    override val actions: List<SampleAction> = okHttpActions.map {
        SampleAction(it.label, actionColor.getValue(it.category))
    }

    override fun executeAction(index: Int) {
        val action = okHttpActions[index]
        viewModelScope.launch(Dispatchers.IO) {
            try {
                action.action(client) { _statusLog.value = it }
            } catch (e: Exception) {
                _statusLog.value = "Error: ${e.message}"
            }
        }
    }

}
