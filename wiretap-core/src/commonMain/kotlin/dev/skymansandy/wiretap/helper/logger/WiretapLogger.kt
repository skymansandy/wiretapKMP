/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.logger

import dev.skymansandy.wiretap.domain.model.HttpLog
import dev.skymansandy.wiretap.domain.model.SocketConnection
import dev.skymansandy.wiretap.domain.model.SocketMessage
import dev.skymansandy.wiretap.domain.model.SseConnection
import dev.skymansandy.wiretap.domain.model.SseEvent

interface WiretapLogger {

    fun logHttp(entry: HttpLog)

    fun logSocket(entry: SocketConnection) = Unit

    fun logSocketMessage(message: SocketMessage) = Unit

    fun logSse(entry: SseConnection) = Unit

    fun logSseEvent(event: SseEvent) = Unit
}
