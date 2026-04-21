/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.launcher

import dev.skymansandy.wiretap.domain.model.HttpLog
import dev.skymansandy.wiretap.domain.model.SocketConnection
import dev.skymansandy.wiretap.domain.model.SocketMessage
import dev.skymansandy.wiretap.domain.model.SseConnection
import dev.skymansandy.wiretap.domain.model.SseEvent

internal actual fun onNewHttpLog(httpLog: HttpLog) = Unit

internal actual fun onDeleteHttpLog(id: Long) = Unit

internal actual fun onClearHttpLogs() = Unit

internal actual fun onNewSocketConnection(entry: SocketConnection) = Unit

internal actual fun onNewSocketMessage(entry: SocketConnection, message: SocketMessage) = Unit

internal actual fun onClearSocketLogs() = Unit

internal actual fun onNewSseConnection(entry: SseConnection) = Unit

internal actual fun onNewSseEvent(entry: SseConnection, event: SseEvent) = Unit

internal actual fun onClearSseLogs() = Unit
