/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.testing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

/**
 * Helpers for ViewModel tests that use [androidx.lifecycle.viewModelScope], which schedules on
 * [Dispatchers.Main]. Call [setupMain] in `@BeforeTest` and [teardownMain] in `@AfterTest`.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal object MainDispatcherSupport {

    fun setupMain(): TestDispatcher = StandardTestDispatcher().also { Dispatchers.setMain(it) }

    fun teardownMain() {
        Dispatchers.resetMain()
    }
}
