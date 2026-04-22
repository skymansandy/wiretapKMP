/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.launcher

import dev.skymansandy.wiretap.domain.model.HttpLog
import dev.skymansandy.wiretap.domain.model.SocketConnection
import dev.skymansandy.wiretap.domain.model.SocketMessage
import dev.skymansandy.wiretap.domain.model.SseConnection
import dev.skymansandy.wiretap.domain.model.SseEvent

internal expect fun onNewHttpLog(httpLog: HttpLog)

internal expect fun onDeleteHttpLog(id: Long)

internal expect fun onClearHttpLogs()

internal expect fun onNewSocketConnection(entry: SocketConnection)

internal expect fun onNewSocketMessage(entry: SocketConnection, message: SocketMessage)

internal expect fun onClearSocketLogs()

internal expect fun onNewSseConnection(entry: SseConnection)

internal expect fun onNewSseEvent(entry: SseConnection, event: SseEvent)

internal expect fun onClearSseLogs()
