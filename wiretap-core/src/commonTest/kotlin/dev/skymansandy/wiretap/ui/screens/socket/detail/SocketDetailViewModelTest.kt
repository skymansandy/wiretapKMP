/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.ui.screens.socket.detail

import app.cash.turbine.test
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.skymansandy.wiretap.domain.model.SocketConnection
import dev.skymansandy.wiretap.domain.model.SocketContentType
import dev.skymansandy.wiretap.domain.model.SocketMessage
import dev.skymansandy.wiretap.domain.model.SocketMessageType
import dev.skymansandy.wiretap.domain.model.SocketStatus
import dev.skymansandy.wiretap.domain.orchestrator.SocketLogManager
import dev.skymansandy.wiretap.testing.MainDispatcherSupport
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class SocketDetailViewModelTest : DescribeSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    val manager = mock<SocketLogManager>(MockMode.autoUnit)

    beforeEach { MainDispatcherSupport.setupMain() }
    afterEach { MainDispatcherSupport.teardownMain() }

    describe("initialEntry") {
        it("is loaded from the manager on init") {
            runTest {
                val entry = connection(id = 5)
                everySuspend { manager.getSocketById(5) } returns entry
                every { manager.flowSocketById(5) } returns flowOf(entry)
                every { manager.flowSocketMessagesById(5) } returns flowOf(emptyList())

                val vm = SocketDetailViewModel(socketId = 5, socketLogManager = manager)
                advanceUntilIdle()

                vm.initialEntry.value?.id shouldBe 5L
            }
        }
    }

    describe("liveEntry") {
        it("tracks the manager flow") {
            runTest {
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
        }
    }

    describe("messages") {
        it("tracks the manager flow") {
            runTest {
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
        }
    }

    describe("search") {
        it("activates and closes via actions") {
            runTest {
                everySuspend { manager.getSocketById(any()) } returns null
                every { manager.flowSocketById(any()) } returns flowOf(null)
                every { manager.flowSocketMessagesById(any()) } returns flowOf(emptyList())

                val vm = SocketDetailViewModel(socketId = 1, socketLogManager = manager)

                vm.isSearchActive.value shouldBe false

                vm.activateSearch()
                vm.isSearchActive.value shouldBe true

                vm.setSearchQuery("ping")
                vm.searchQuery.value shouldBe "ping"

                vm.closeSearch()
                vm.isSearchActive.value shouldBe false
                vm.searchQuery.value shouldBe ""
            }
        }

        it("debounces searchQuery for 450ms before publishing to debouncedQuery") {
            runTest {
                everySuspend { manager.getSocketById(any()) } returns null
                every { manager.flowSocketById(any()) } returns flowOf(null)
                every { manager.flowSocketMessagesById(any()) } returns flowOf(emptyList())

                val vm = SocketDetailViewModel(socketId = 1, socketLogManager = manager)

                vm.debouncedQuery.test {
                    awaitItem() shouldBe ""
                    vm.setSearchQuery("pi")
                    advanceTimeBy(SHORT_DEBOUNCE_MS)
                    expectNoEvents()
                    advanceTimeBy(LONG_DEBOUNCE_MS)
                    awaitItem() shouldBe "pi"
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        it("publishes an empty query immediately") {
            runTest {
                everySuspend { manager.getSocketById(any()) } returns null
                every { manager.flowSocketById(any()) } returns flowOf(null)
                every { manager.flowSocketMessagesById(any()) } returns flowOf(emptyList())

                val vm = SocketDetailViewModel(socketId = 1, socketLogManager = manager)

                vm.debouncedQuery.test {
                    awaitItem() shouldBe ""
                    vm.setSearchQuery("hi")
                    advanceTimeBy(FULL_DEBOUNCE_MS)
                    awaitItem() shouldBe "hi"
                    vm.setSearchQuery("")
                    advanceUntilIdle()
                    awaitItem() shouldBe ""
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    describe("matches") {
        it("recomputes when messages or debounced query change") {
            runTest {
                val msgs = MutableStateFlow(emptyList<SocketMessage>())
                everySuspend { manager.getSocketById(any()) } returns null
                every { manager.flowSocketById(any()) } returns flowOf(null)
                every { manager.flowSocketMessagesById(any()) } returns msgs

                val vm = SocketDetailViewModel(socketId = 1, socketLogManager = manager)

                vm.matches.test {
                    awaitItem() shouldBe emptyList()

                    msgs.value = listOf(textMessage("hello ping pong"), textMessage("nothing"))
                    advanceUntilIdle()

                    vm.setSearchQuery("ping")
                    advanceTimeBy(FULL_DEBOUNCE_MS)

                    val list = awaitItem()
                    list.size shouldBe 1
                    list[0].messageIndex shouldBe 0
                    list[0].start shouldBe 6
                    list[0].endInclusive shouldBe 9
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        it("skips non-text messages so binary/control frames do not match") {
            val msg = SocketMessage(
                socketId = 0,
                direction = SocketMessageType.Received,
                contentType = SocketContentType.Binary,
                content = "[Binary: 1.0 KB]",
                byteCount = 1024,
                timestamp = 0,
            )

            val matches = computeSocketMatches(listOf(msg), "Binary")

            matches shouldBe emptyList()
        }

        it("reports offsets into the original content for case-expanding characters") {
            // U+0130 lowercases to two characters, so a lowercased copy of the
            // content would hand back offsets that no longer index the original.
            val matches = computeSocketMatches(listOf(textMessage("\u0130X\u0130")), "x")

            matches.size shouldBe 1
            matches[0].start shouldBe 1
            matches[0].endInclusive shouldBe 1
        }
    }

    describe("match navigation") {
        it("wraps prev / next around the match list and resets when matches change") {
            runTest {
                val msgs = MutableStateFlow(
                    listOf(textMessage("hit"), textMessage("hit"), textMessage("hit")),
                )
                everySuspend { manager.getSocketById(any()) } returns null
                every { manager.flowSocketById(any()) } returns flowOf(null)
                every { manager.flowSocketMessagesById(any()) } returns msgs

                val vm = SocketDetailViewModel(socketId = 1, socketLogManager = manager)
                vm.setSearchQuery("hit")
                advanceTimeBy(FULL_DEBOUNCE_MS)
                advanceUntilIdle()

                vm.matches.value.size shouldBe 3
                vm.currentMatchIndex.value shouldBe 0

                vm.goToNextMatch()
                vm.currentMatchIndex.value shouldBe 1

                vm.goToPreviousMatch()
                vm.goToPreviousMatch()
                vm.currentMatchIndex.value shouldBe 2

                vm.goToNextMatch()
                vm.currentMatchIndex.value shouldBe 0

                msgs.value = listOf(textMessage("hit"), textMessage("hit"))
                advanceUntilIdle()
                vm.currentMatchIndex.value shouldBe 0
            }
        }

        it("is a no-op when there are no matches") {
            runTest {
                everySuspend { manager.getSocketById(any()) } returns null
                every { manager.flowSocketById(any()) } returns flowOf(null)
                every { manager.flowSocketMessagesById(any()) } returns flowOf(emptyList())

                val vm = SocketDetailViewModel(socketId = 1, socketLogManager = manager)

                vm.goToNextMatch()
                vm.goToPreviousMatch()
                vm.currentMatchIndex.value shouldBe 0
            }
        }
    }

    describe("share") {
        it("buildShareText delegates to buildSocketShareText with the live entry") {
            runTest {
                val entry = connection(id = 9).copy(url = "wss://share.example/x")
                everySuspend { manager.getSocketById(9) } returns entry
                every { manager.flowSocketById(9) } returns flowOf(entry)
                every { manager.flowSocketMessagesById(9) } returns flowOf(
                    listOf(textMessage("UNIQUE_TOKEN")),
                )

                val vm = SocketDetailViewModel(socketId = 9, socketLogManager = manager)
                advanceUntilIdle()

                val text = vm.buildShareText()

                text shouldContain "WS wss://share.example/x"
                text shouldContain "UNIQUE_TOKEN"
                vm.shareSubject shouldBe "WS wss://share.example/x"
            }
        }

        it("buildShareText returns empty string before the entry resolves") {
            runTest {
                everySuspend { manager.getSocketById(any()) } returns null
                every { manager.flowSocketById(any()) } returns flowOf(null)
                every { manager.flowSocketMessagesById(any()) } returns flowOf(emptyList())

                val vm = SocketDetailViewModel(socketId = 1, socketLogManager = manager)

                vm.buildShareText() shouldBe ""
                vm.shareSubject shouldBe ""
            }
        }
    }
})

private const val SHORT_DEBOUNCE_MS = 200L
private const val LONG_DEBOUNCE_MS = 300L
private const val FULL_DEBOUNCE_MS = 500L

private fun textMessage(content: String) = SocketMessage(
    socketId = 0L,
    direction = SocketMessageType.Sent,
    contentType = SocketContentType.Text,
    content = content,
    byteCount = content.length.toLong(),
    timestamp = 0L,
)

private fun connection(id: Long) = SocketConnection(
    id = id,
    url = "wss://x",
    status = SocketStatus.Open,
    timestamp = 0,
)
