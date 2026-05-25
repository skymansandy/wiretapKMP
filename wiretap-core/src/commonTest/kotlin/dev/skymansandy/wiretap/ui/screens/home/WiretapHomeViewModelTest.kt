/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.ui.screens.home

import dev.skymansandy.wiretap.ui.model.HomeTab
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class WiretapHomeViewModelTest {

    @Test
    fun `default selected tab is Http`() {
        val vm = WiretapHomeViewModel()

        vm.selectedTab.value shouldBe HomeTab.Http
    }

    @Test
    fun `selectTab updates the selectedTab flow`() {
        val vm = WiretapHomeViewModel()

        HomeTab.entries.forEach { tab ->
            vm.selectTab(tab)
            vm.selectedTab.value shouldBe tab
        }
    }
}
