/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.ui.screens.sse.detail

import app.cash.turbine.test
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.skymansandy.wiretap.domain.model.SseConnection
import dev.skymansandy.wiretap.domain.model.SseEvent
import dev.skymansandy.wiretap.domain.orchestrator.SseLogManager
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

    describe("search") {
        it("activates and closes via actions") {
            runTest {
                everySuspend { manager.getConnectionById(any()) } returns null
                every { manager.flowConnectionById(any()) } returns flowOf(null)
                every { manager.flowEventsById(any()) } returns flowOf(emptyList())

                val vm = SseDetailViewModel(connectionId = 1, sseLogManager = manager)

                vm.isSearchActive.value shouldBe false

                vm.activateSearch()
                vm.isSearchActive.value shouldBe true

                vm.setSearchQuery("hello")
                vm.searchQuery.value shouldBe "hello"

                vm.closeSearch()
                vm.isSearchActive.value shouldBe false
                vm.searchQuery.value shouldBe ""
            }
        }

        it("debounces searchQuery for 450ms before publishing to debouncedQuery") {
            runTest {
                everySuspend { manager.getConnectionById(any()) } returns null
                every { manager.flowConnectionById(any()) } returns flowOf(null)
                every { manager.flowEventsById(any()) } returns flowOf(emptyList())

                val vm = SseDetailViewModel(connectionId = 1, sseLogManager = manager)

                vm.debouncedQuery.test {
                    awaitItem() shouldBe ""
                    vm.setSearchQuery("he")
                    advanceTimeBy(SHORT_DEBOUNCE_MS)
                    expectNoEvents()
                    advanceTimeBy(LONG_DEBOUNCE_MS)
                    awaitItem() shouldBe "he"
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        it("publishes an empty query immediately") {
            runTest {
                everySuspend { manager.getConnectionById(any()) } returns null
                every { manager.flowConnectionById(any()) } returns flowOf(null)
                every { manager.flowEventsById(any()) } returns flowOf(emptyList())

                val vm = SseDetailViewModel(connectionId = 1, sseLogManager = manager)

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
        it("recomputes when events or debounced query change") {
            runTest {
                val flow = MutableStateFlow(emptyList<SseEvent>())
                everySuspend { manager.getConnectionById(any()) } returns null
                every { manager.flowConnectionById(any()) } returns flowOf(null)
                every { manager.flowEventsById(any()) } returns flow

                val vm = SseDetailViewModel(connectionId = 1, sseLogManager = manager)

                vm.matches.test {
                    awaitItem() shouldBe emptyList()

                    flow.value = listOf(event("hello world"), event("nothing here"))
                    advanceUntilIdle()

                    vm.setSearchQuery("world")
                    advanceTimeBy(FULL_DEBOUNCE_MS)

                    val list = awaitItem()
                    list.size shouldBe 1
                    list[0].field shouldBe SseMatchField.Data
                    list[0].index shouldBe 0
                    list[0].start shouldBe 6
                    list[0].endInclusive shouldBe 10
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        it("matches event type and id, ordered as the bubble renders them") {
            val events = listOf(
                event(data = "payload", eventType = "ping-type", eventId = "ping-7"),
            )

            val matches = computeSseMatches(connection = null, events = events, query = "ping")

            matches.map { it.field } shouldBe listOf(
                SseMatchField.EventType,
                SseMatchField.EventId,
            )
            matches.map { it.start } shouldBe listOf(0, 0)
        }

        it("reports offsets relative to the field the match landed in") {
            val events = listOf(event(data = "aa-tok", eventId = "tok"))

            val matches = computeSseMatches(connection = null, events = events, query = "tok")

            matches.map { it.field to it.start } shouldBe listOf(
                SseMatchField.Data to 3,
                SseMatchField.EventId to 0,
            )
        }

        it("matches the url and request headers ahead of the event stream") {
            val conn = SseConnection(
                id = 1,
                url = "https://sse.example/tok",
                timestamp = 0,
                requestHeaders = mapOf("X-Tok" to "abc"),
            )

            val matches = computeSseMatches(conn, listOf(event("tok")), "tok")

            matches.map { it.field to it.index } shouldBe listOf(
                SseMatchField.Url to 0,
                SseMatchField.RequestHeader to 0,
                SseMatchField.Data to 0,
            )
        }

        it("reports offsets into the original data for case-expanding characters") {
            // U+0130 lowercases to two characters, so a lowercased copy of the
            // data would hand back offsets that no longer index the original.
            val matches = computeSseMatches(
                connection = null,
                events = listOf(event("\u0130X\u0130")),
                query = "x",
            )

            matches.size shouldBe 1
            matches[0].field shouldBe SseMatchField.Data
            matches[0].start shouldBe 1
            matches[0].endInclusive shouldBe 1
        }
    }

    describe("match navigation") {
        it("wraps prev / next around the match list") {
            runTest {
                val flow = MutableStateFlow(listOf(event("hit"), event("hit"), event("hit")))
                everySuspend { manager.getConnectionById(any()) } returns null
                every { manager.flowConnectionById(any()) } returns flowOf(null)
                every { manager.flowEventsById(any()) } returns flow

                val vm = SseDetailViewModel(connectionId = 1, sseLogManager = manager)
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
            }
        }

        it("holds the active match while new matching events arrive") {
            runTest {
                val flow = MutableStateFlow(listOf(event("hit"), event("hit"), event("hit")))
                everySuspend { manager.getConnectionById(any()) } returns null
                every { manager.flowConnectionById(any()) } returns flowOf(null)
                every { manager.flowEventsById(any()) } returns flow

                val vm = SseDetailViewModel(connectionId = 1, sseLogManager = manager)
                vm.setSearchQuery("hit")
                advanceTimeBy(FULL_DEBOUNCE_MS)
                advanceUntilIdle()

                vm.goToNextMatch()
                vm.goToNextMatch()
                vm.currentMatchIndex.value shouldBe 2

                // A live stream keeps emitting; the reader should not be yanked
                // back to the first hit every time one lands.
                flow.value = flow.value + event("hit")
                advanceUntilIdle()

                vm.currentMatchIndex.value shouldBe 2
            }
        }

        it("returns to the first match when the query changes") {
            runTest {
                val flow = MutableStateFlow(listOf(event("hit hit"), event("hit")))
                everySuspend { manager.getConnectionById(any()) } returns null
                every { manager.flowConnectionById(any()) } returns flowOf(null)
                every { manager.flowEventsById(any()) } returns flow

                val vm = SseDetailViewModel(connectionId = 1, sseLogManager = manager)
                vm.setSearchQuery("hit")
                advanceTimeBy(FULL_DEBOUNCE_MS)
                advanceUntilIdle()

                vm.goToNextMatch()
                vm.currentMatchIndex.value shouldBe 1

                vm.setSearchQuery("hit h")
                vm.currentMatchIndex.value shouldBe 0
            }
        }

        it("steps from a clamped position when the match list shrinks") {
            runTest {
                val flow = MutableStateFlow(listOf(event("hit"), event("hit"), event("hit")))
                everySuspend { manager.getConnectionById(any()) } returns null
                every { manager.flowConnectionById(any()) } returns flowOf(null)
                every { manager.flowEventsById(any()) } returns flow

                val vm = SseDetailViewModel(connectionId = 1, sseLogManager = manager)
                vm.setSearchQuery("hit")
                advanceTimeBy(FULL_DEBOUNCE_MS)
                advanceUntilIdle()

                vm.goToNextMatch()
                vm.goToNextMatch()
                vm.currentMatchIndex.value shouldBe 2

                flow.value = listOf(event("hit"))
                advanceUntilIdle()

                vm.goToNextMatch()
                vm.currentMatchIndex.value shouldBe 0
            }
        }

        it("is a no-op when there are no matches") {
            runTest {
                everySuspend { manager.getConnectionById(any()) } returns null
                every { manager.flowConnectionById(any()) } returns flowOf(null)
                every { manager.flowEventsById(any()) } returns flowOf(emptyList())

                val vm = SseDetailViewModel(connectionId = 1, sseLogManager = manager)

                vm.goToNextMatch()
                vm.goToPreviousMatch()
                vm.currentMatchIndex.value shouldBe 0
            }
        }
    }

    describe("share") {
        it("buildShareText delegates to buildSseShareText with the live entry") {
            runTest {
                val entry = SseConnection(id = 9, url = "https://share.example/stream", timestamp = 0)
                everySuspend { manager.getConnectionById(9) } returns entry
                every { manager.flowConnectionById(9) } returns flowOf(entry)
                every { manager.flowEventsById(9) } returns flowOf(listOf(event("UNIQUE_TOKEN")))

                val vm = SseDetailViewModel(connectionId = 9, sseLogManager = manager)
                advanceUntilIdle()

                val text = vm.buildShareText()

                text shouldContain "SSE https://share.example/stream"
                text shouldContain "UNIQUE_TOKEN"
                vm.shareSubject shouldBe "SSE https://share.example/stream"
            }
        }

        it("buildShareText returns empty string before the entry resolves") {
            runTest {
                everySuspend { manager.getConnectionById(any()) } returns null
                every { manager.flowConnectionById(any()) } returns flowOf(null)
                every { manager.flowEventsById(any()) } returns flowOf(emptyList())

                val vm = SseDetailViewModel(connectionId = 1, sseLogManager = manager)

                vm.buildShareText() shouldBe ""
                vm.shareSubject shouldBe ""
            }
        }
    }
})

private const val SHORT_DEBOUNCE_MS = 200L
private const val LONG_DEBOUNCE_MS = 300L
private const val FULL_DEBOUNCE_MS = 500L

private fun event(
    data: String,
    eventType: String? = null,
    eventId: String? = null,
) = SseEvent(
    connectionId = 0L,
    eventType = eventType,
    data = data,
    eventId = eventId,
    byteCount = data.length.toLong(),
    timestamp = 0L,
)
