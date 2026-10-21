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
 * Mirrors captured traffic to the platform console.
 *
 * Every method is abstract on purpose: default no-op bodies once let SSE support ship with the
 * calls wired through the orchestrators but no implementation behind them, so SSE silently
 * printed nothing for several releases. An implementation that forgets a channel should fail to
 * compile, not go quiet.
 *
 * Whether traffic reaches this logger at all is a per-plugin choice — see
 * `WiretapHttpConfig.consoleLogging` and the `withConsoleLogging` decorators.
 */
interface WiretapLogger {

    fun logHttp(entry: HttpLog)

    fun logSocket(entry: SocketConnection)

    fun logSocketMessage(message: SocketMessage)

    fun logSse(entry: SseConnection)

    fun logSseEvent(event: SseEvent)
}
