/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.launcher

import dev.skymansandy.wiretap.domain.model.HttpLog
import dev.skymansandy.wiretap.domain.model.SocketConnection
import dev.skymansandy.wiretap.domain.model.SocketMessage
import dev.skymansandy.wiretap.domain.model.SseConnection
import dev.skymansandy.wiretap.domain.model.SseEvent

internal expect fun platformOnNewHttpLog(httpLog: HttpLog)

internal expect fun platformOnDeleteHttpLog(id: Long)

internal expect fun platformOnClearHttpLogs()

internal expect fun platformOnNewSocketConnection(entry: SocketConnection)

internal expect fun platformOnNewSocketMessage(entry: SocketConnection, message: SocketMessage)

internal expect fun platformOnClearSocketLogs()

internal expect fun platformOnNewSseConnection(entry: SseConnection)

internal expect fun platformOnNewSseEvent(entry: SseConnection, event: SseEvent)

internal expect fun platformOnClearSseLogs()
