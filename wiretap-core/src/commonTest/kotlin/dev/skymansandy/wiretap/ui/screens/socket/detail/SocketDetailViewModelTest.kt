/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.ui.screens.socket.detail

import app.cash.turbine.test
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.skymansandy.wiretap.domain.model.SocketConnection
import dev.skymansandy.wiretap.domain.model.SocketContentType
import dev.skymansandy.wiretap.domain.model.SocketMessage
import dev.skymansandy.wiretap.domain.model.SocketMessageType
import dev.skymansandy.wiretap.domain.model.SocketStatus
import dev.skymansandy.wiretap.domain.orchestrator.SocketLogManager
import dev.skymansandy.wiretap.testing.MainDispatcherSupport
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SocketDetailViewModelTest {

    private val manager = mock<SocketLogManager>(MockMode.autoUnit)

    @BeforeTest fun setUp() { MainDispatcherSupport.setupMain() }
    @AfterTest fun tearDown() { MainDispatcherSupport.teardownMain() }

    @Test
    fun `initialEntry is loaded from the manager on init`() = runTest {
        val entry = connection(id = 5)
        everySuspend { manager.getSocketById(5) } returns entry
        every { manager.flowSocketById(5) } returns flowOf(entry)
        every { manager.flowSocketMessagesById(5) } returns flowOf(emptyList())

        val vm = SocketDetailViewModel(socketId = 5, socketLogManager = manager)
        advanceUntilIdle()

        vm.initialEntry.value?.id shouldBe 5L
    }

    @Test
    fun `liveEntry tracks the manager flow`() = runTest {
        val entry = connection(id = 7)
        everySuspend { manager.getSocketById(7) } returns null
        every { manager.flowSocketById(7) } returns flowOf(entry)
        every { manager.flowSocketMessagesById(7) } returns flowOf(emptyList())

        val vm = SocketDetailViewModel(socketId = 7, socketLogManager = manager)

        vm.liveEntry.test {
            awaitItem() shouldBe null
            awaitItem()?.id shouldBe 7L
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `messages tracks the manager flow`() = runTest {
        val msg = SocketMessage(
            socketId = 7,
            direction = SocketMessageType.Sent,
            contentType = SocketContentType.Text,
            content = "hi",
            byteCount = 2,
            timestamp = 0,
        )
        everySuspend { manager.getSocketById(7) } returns null
        every { manager.flowSocketById(7) } returns flowOf(null)
        every { manager.flowSocketMessagesById(7) } returns flowOf(listOf(msg))

        val vm = SocketDetailViewModel(socketId = 7, socketLogManager = manager)

        vm.messages.test {
            awaitItem() shouldBe emptyList()
            awaitItem() shouldBe listOf(msg)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun connection(id: Long) = SocketConnection(
        id = id,
        url = "wss://x",
        status = SocketStatus.Open,
        timestamp = 0,
    )
}
