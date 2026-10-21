/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.domain.orchestrator

import dev.skymansandy.wiretap.domain.model.SocketConnection
import dev.skymansandy.wiretap.domain.model.SocketMessage
import dev.skymansandy.wiretap.helper.logger.WiretapLogger

/**
 * Prints the connections and frames [delegate] records to the platform console.
 *
 * Driven per installation by `WiretapWsConfig.consoleLogging`. See [ConsoleLoggingHttpLogManager]
 * for why this is a decorator.
 */
internal class ConsoleLoggingSocketLogManager(
    private val delegate: SocketLogManager,
    private val logger: WiretapLogger,
) : SocketLogManager by delegate {

    override suspend fun createSocket(entry: SocketConnection): Long {
        val id = delegate.createSocket(entry)
        logger.logSocket(entry.copy(id = id))
        return id
    }

    override suspend fun updateSocket(entry: SocketConnection) {
        delegate.updateSocket(entry)
        logger.logSocket(entry)
    }

    override suspend fun logSocketMsg(message: SocketMessage) {
        delegate.logSocketMsg(message)
        logger.logSocketMessage(message)
    }
}
