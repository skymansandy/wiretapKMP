/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.launcher

import dev.skymansandy.wiretap.domain.model.HttpLog
import dev.skymansandy.wiretap.domain.model.SocketConnection
import dev.skymansandy.wiretap.domain.model.SocketMessage
import dev.skymansandy.wiretap.domain.model.SseConnection
import dev.skymansandy.wiretap.domain.model.SseEvent

internal actual fun platformOnNewHttpLog(httpLog: HttpLog) = Unit

internal actual fun platformOnDeleteHttpLog(id: Long) = Unit

internal actual fun platformOnClearHttpLogs() = Unit

internal actual fun platformOnNewSocketConnection(entry: SocketConnection) = Unit

internal actual fun platformOnNewSocketMessage(entry: SocketConnection, message: SocketMessage) = Unit

internal actual fun platformOnClearSocketLogs() = Unit

internal actual fun platformOnNewSseConnection(entry: SseConnection) = Unit

internal actual fun platformOnNewSseEvent(entry: SseConnection, event: SseEvent) = Unit

internal actual fun platformOnClearSseLogs() = Unit
