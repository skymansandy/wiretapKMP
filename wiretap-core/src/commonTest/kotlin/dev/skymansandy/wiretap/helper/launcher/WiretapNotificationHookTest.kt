/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.launcher

import dev.skymansandy.wiretap.domain.model.HttpLog
import dev.skymansandy.wiretap.domain.model.SocketConnection
import dev.skymansandy.wiretap.domain.model.SocketContentType
import dev.skymansandy.wiretap.domain.model.SocketMessage
import dev.skymansandy.wiretap.domain.model.SocketMessageType
import dev.skymansandy.wiretap.domain.model.SocketStatus
import dev.skymansandy.wiretap.domain.model.SseConnection
import dev.skymansandy.wiretap.domain.model.SseEvent
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

/**
 * The default hook delegates to platform functions which are no-ops on the JVM target
 * (the source set under test). Coverage here is a smoke test: every override is callable
 * and does not throw. The Android variant is covered by androidHostTest in Phase 4.
 */
class WiretapNotificationHookTest : DescribeSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    val hook: WiretapNotificationHook = DefaultWiretapNotificationHook

    describe("DefaultWiretapNotificationHook") {
        it("onNewHttpLog delegates without throwing") {
            hook.onNewHttpLog(httpLog())
        }

        it("onDeleteHttpLog delegates without throwing") {
            hook.onDeleteHttpLog(id = 42)
        }

        it("onClearHttpLogs delegates without throwing") {
            hook.onClearHttpLogs()
        }

        it("onNewSocketConnection delegates without throwing") {
            hook.onNewSocketConnection(socketConnection())
        }

        it("onNewSocketMessage delegates without throwing") {
            hook.onNewSocketMessage(socketConnection(), socketMessage())
        }

        it("onClearSocketLogs delegates without throwing") {
            hook.onClearSocketLogs()
        }

        it("onNewSseConnection delegates without throwing") {
            hook.onNewSseConnection(sseConnection())
        }

        it("onNewSseEvent delegates without throwing") {
            hook.onNewSseEvent(sseConnection(), sseEvent())
        }

        it("onClearSseLogs delegates without throwing") {
            hook.onClearSseLogs()
        }

        it("is a singleton") {
            (DefaultWiretapNotificationHook === DefaultWiretapNotificationHook) shouldBe true
        }
    }
})

private fun httpLog() = HttpLog(url = "https://x", method = "GET", timestamp = 0)

private fun socketConnection() = SocketConnection(
    url = "wss://x",
    status = SocketStatus.Open,
    timestamp = 0,
)

private fun socketMessage() = SocketMessage(
    socketId = 1,
    direction = SocketMessageType.Sent,
    contentType = SocketContentType.Text,
    content = "hi",
    byteCount = 2,
    timestamp = 0,
)

private fun sseConnection() = SseConnection(url = "https://x/events", timestamp = 0)

private fun sseEvent() = SseEvent(
    connectionId = 1,
    data = "{}",
    byteCount = 2,
    timestamp = 0,
)
