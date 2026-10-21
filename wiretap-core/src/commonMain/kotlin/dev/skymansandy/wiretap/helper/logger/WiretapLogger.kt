/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.logger

import dev.skymansandy.wiretap.domain.model.HttpLog
import dev.skymansandy.wiretap.domain.model.SocketConnection
import dev.skymansandy.wiretap.domain.model.SocketMessage
import dev.skymansandy.wiretap.domain.model.SseConnection
import dev.skymansandy.wiretap.domain.model.SseEvent

/**
 * Prints captured traffic to the platform console.
 *
 * No default method bodies: a partial implementation should fail to compile rather than go quiet.
 * Whether traffic reaches this logger at all is per-plugin — see `consoleLogging` on each config.
 */
interface WiretapLogger {

    fun logHttp(entry: HttpLog)

    fun logSocket(entry: SocketConnection)

    fun logSocketMessage(message: SocketMessage)

    fun logSse(entry: SseConnection)

    fun logSseEvent(event: SseEvent)
}
