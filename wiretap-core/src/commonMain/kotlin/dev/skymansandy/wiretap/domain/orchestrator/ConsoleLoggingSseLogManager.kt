/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.domain.orchestrator

import dev.skymansandy.wiretap.domain.model.SseConnection
import dev.skymansandy.wiretap.domain.model.SseEvent
import dev.skymansandy.wiretap.helper.logger.WiretapLogger

/**
 * Prints the connections and events [delegate] records to the platform console.
 *
 * Driven per installation by `WiretapSseConfig.consoleLogging`. See [ConsoleLoggingHttpLogManager]
 * for why this is a decorator.
 */
internal class ConsoleLoggingSseLogManager(
    private val delegate: SseLogManager,
    private val logger: WiretapLogger,
) : SseLogManager by delegate {

    override suspend fun createConnection(entry: SseConnection): Long {
        val id = delegate.createConnection(entry)
        logger.logSse(entry.copy(id = id))
        return id
    }

    override suspend fun updateConnection(entry: SseConnection) {
        delegate.updateConnection(entry)
        logger.logSse(entry)
    }

    override suspend fun logEvent(event: SseEvent) {
        delegate.logEvent(event)
        logger.logSseEvent(event)
    }
}
