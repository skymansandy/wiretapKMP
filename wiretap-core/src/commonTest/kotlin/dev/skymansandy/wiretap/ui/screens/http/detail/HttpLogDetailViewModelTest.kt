/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.ui.screens.http.detail

import app.cash.turbine.test
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import dev.skymansandy.wiretap.domain.model.HttpLog
import dev.skymansandy.wiretap.domain.orchestrator.HttpLogManager
import dev.skymansandy.wiretap.testing.MainDispatcherSupport
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class HttpLogDetailViewModelTest {

    private val httpLogManager = mock<HttpLogManager>(MockMode.autoUnit)

    @BeforeTest
    fun setUp() {
        MainDispatcherSupport.setupMain()
    }

    @AfterTest
    fun tearDown() {
        MainDispatcherSupport.teardownMain()
    }

    @Test
    fun `entry exposes the log emitted by the manager`() = runTest {
        val log = HttpLog(id = 7, url = "https://x", method = "GET", timestamp = 0)
        every { httpLogManager.flowHttpLogById(7) } returns flowOf(log)

        val vm = HttpLogDetailViewModel(logId = 7, httpLogManager = httpLogManager)

        vm.entry.test {
            awaitItem() shouldBe null // initial
            awaitItem()?.id shouldBe 7L
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `entry emits null when manager flow yields null`() = runTest {
        every { httpLogManager.flowHttpLogById(99) } returns flowOf(null)

        val vm = HttpLogDetailViewModel(logId = 99, httpLogManager = httpLogManager)

        vm.entry.test {
            awaitItem() shouldBe null
            // No further emission expected within timeout
            cancelAndIgnoreRemainingEvents()
        }
    }
}
