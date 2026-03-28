package dev.skymansandy.wiretap.ui.screens.home

import androidx.lifecycle.ViewModel
import dev.skymansandy.wiretap.ui.model.HomeTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class WiretapHomeViewModel : ViewModel() {

    val selectedTab: StateFlow<HomeTab>
        field = MutableStateFlow(HomeTab.Http)

    fun selectTab(tab: HomeTab) {
        selectedTab.value = tab
    }
}
