/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.ktor.testing

import dev.skymansandy.wiretap.domain.model.HttpLog
import dev.skymansandy.wiretap.domain.model.SocketConnection
import dev.skymansandy.wiretap.domain.model.SocketMessage
import dev.skymansandy.wiretap.domain.model.SseConnection
import dev.skymansandy.wiretap.domain.model.SseEvent
import dev.skymansandy.wiretap.helper.logger.WiretapLogger

/** Records console output in memory so tests can assert on `consoleLogging` without stdout. */
internal class RecordingWiretapLogger : WiretapLogger {

    val httpLogs = mutableListOf<HttpLog>()
    val sockets = mutableListOf<SocketConnection>()
    val socketMessages = mutableListOf<SocketMessage>()
    val sseConnections = mutableListOf<SseConnection>()
    val sseEvents = mutableListOf<SseEvent>()

    override fun logHttp(entry: HttpLog) {
        httpLogs += entry
    }

    override fun logSocket(entry: SocketConnection) {
        sockets += entry
    }

    override fun logSocketMessage(message: SocketMessage) {
        socketMessages += message
    }

    override fun logSse(entry: SseConnection) {
        sseConnections += entry
    }

    override fun logSseEvent(event: SseEvent) {
        sseEvents += event
    }
}
