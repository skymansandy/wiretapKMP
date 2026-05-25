/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.ui.screens.home

import dev.skymansandy.wiretap.ui.model.HomeTab
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class WiretapHomeViewModelTest : DescribeSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    describe("WiretapHomeViewModel") {
        it("default selected tab is Http") {
            val vm = WiretapHomeViewModel()

            vm.selectedTab.value shouldBe HomeTab.Http
        }

        it("selectTab updates the selectedTab flow") {
            val vm = WiretapHomeViewModel()

            HomeTab.entries.forEach { tab ->
                vm.selectTab(tab)
                vm.selectedTab.value shouldBe tab
            }
        }
    }
})
