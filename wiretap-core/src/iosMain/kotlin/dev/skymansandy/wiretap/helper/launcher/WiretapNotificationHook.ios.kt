/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.launcher

import dev.skymansandy.wiretap.domain.model.HttpLog
import dev.skymansandy.wiretap.domain.model.SocketConnection
import dev.skymansandy.wiretap.domain.model.SocketMessage

internal actual fun onNewHttpLog(httpLog: HttpLog) = Unit

internal actual fun onClearHttpLogs() = Unit

internal actual fun onNewSocketConnection(entry: SocketConnection) = Unit

internal actual fun onNewSocketMessage(entry: SocketConnection, message: SocketMessage) = Unit

internal actual fun onClearSocketLogs() = Unit
