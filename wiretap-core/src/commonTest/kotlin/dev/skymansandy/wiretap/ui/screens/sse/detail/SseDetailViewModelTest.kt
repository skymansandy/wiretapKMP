/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.ui.screens.sse.detail

import app.cash.turbine.test
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.skymansandy.wiretap.domain.model.SseConnection
import dev.skymansandy.wiretap.domain.model.SseEvent
import dev.skymansandy.wiretap.domain.orchestrator.SseLogManager
import dev.skymansandy.wiretap.testing.MainDispatcherSupport
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class SseDetailViewModelTest : DescribeSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    val manager = mock<SseLogManager>(MockMode.autoUnit)

    beforeEach { MainDispatcherSupport.setupMain() }
    afterEach { MainDispatcherSupport.teardownMain() }

    describe("initialEntry") {
        it("is loaded from the manager on init") {
            runTest {
                val entry = SseConnection(id = 5, url = "x", timestamp = 0)
                everySuspend { manager.getConnectionById(5) } returns entry
                every { manager.flowConnectionById(5) } returns flowOf(entry)
                every { manager.flowEventsById(5) } returns flowOf(emptyList())

                val vm = SseDetailViewModel(connectionId = 5, sseLogManager = manager)
                advanceUntilIdle()

                vm.initialEntry.value?.id shouldBe 5L
            }
        }
    }

    describe("liveEntry") {
        it("tracks the manager flow") {
            runTest {
                val entry = SseConnection(id = 7, url = "x", timestamp = 0)
                everySuspend { manager.getConnectionById(7) } returns null
                every { manager.flowConnectionById(7) } returns flowOf(entry)
                every { manager.flowEventsById(7) } returns flowOf(emptyList())

                val vm = SseDetailViewModel(connectionId = 7, sseLogManager = manager)

                vm.liveEntry.test {
                    awaitItem() shouldBe null
                    awaitItem()?.id shouldBe 7L
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    describe("events") {
        it("tracks the manager flow") {
            runTest {
                val event = SseEvent(connectionId = 7, data = "x", byteCount = 1, timestamp = 0)
                everySuspend { manager.getConnectionById(7) } returns null
                every { manager.flowConnectionById(7) } returns flowOf(null)
                every { manager.flowEventsById(7) } returns flowOf(listOf(event))

                val vm = SseDetailViewModel(connectionId = 7, sseLogManager = manager)

                vm.events.test {
                    awaitItem() shouldBe emptyList()
                    awaitItem() shouldBe listOf(event)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }
})
