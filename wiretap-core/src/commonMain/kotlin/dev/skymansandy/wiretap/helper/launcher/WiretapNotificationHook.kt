/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.launcher

import dev.skymansandy.wiretap.domain.model.HttpLog
import dev.skymansandy.wiretap.domain.model.SocketConnection
import dev.skymansandy.wiretap.domain.model.SocketMessage
import dev.skymansandy.wiretap.domain.model.SseConnection
import dev.skymansandy.wiretap.domain.model.SseEvent

/**
 * Fan-out point for orchestrator → launcher notifications. Implementations
 * react to log lifecycle events (e.g. Android refreshes its status-bar group).
 *
 * Orchestrators take this collaborator via constructor so tests can substitute
 * a mock; production wiring uses [DefaultWiretapNotificationHook], which calls
 * the platform-specific functions in `PlatformNotifications`.
 */
internal interface WiretapNotificationHook {

    fun onNewHttpLog(log: HttpLog)

    fun onDeleteHttpLog(id: Long)

    fun onClearHttpLogs()

    fun onNewSocketConnection(entry: SocketConnection)

    fun onNewSocketMessage(entry: SocketConnection, message: SocketMessage)

    fun onClearSocketLogs()

    fun onNewSseConnection(entry: SseConnection)

    fun onNewSseEvent(entry: SseConnection, event: SseEvent)

    fun onClearSseLogs()
}

internal object DefaultWiretapNotificationHook : WiretapNotificationHook {

    override fun onNewHttpLog(log: HttpLog) = platformOnNewHttpLog(log)

    override fun onDeleteHttpLog(id: Long) = platformOnDeleteHttpLog(id)

    override fun onClearHttpLogs() = platformOnClearHttpLogs()

    override fun onNewSocketConnection(entry: SocketConnection) = platformOnNewSocketConnection(entry)

    override fun onNewSocketMessage(entry: SocketConnection, message: SocketMessage) =
        platformOnNewSocketMessage(entry, message)

    override fun onClearSocketLogs() = platformOnClearSocketLogs()

    override fun onNewSseConnection(entry: SseConnection) = platformOnNewSseConnection(entry)

    override fun onNewSseEvent(entry: SseConnection, event: SseEvent) = platformOnNewSseEvent(entry, event)

    override fun onClearSseLogs() = platformOnClearSseLogs()
}
