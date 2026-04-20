/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.ui.screens.home

import androidx.lifecycle.ViewModel
import dev.skymansandy.wiretap.ui.model.HomeTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class WiretapHomeViewModel : ViewModel() {

    private val _selectedTab: MutableStateFlow<HomeTab> = MutableStateFlow(HomeTab.Http)
    val selectedTab: StateFlow<HomeTab> get() = _selectedTab

    fun selectTab(tab: HomeTab) {
        _selectedTab.value = tab
    }
}
