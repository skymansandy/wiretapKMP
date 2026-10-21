/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.domain.orchestrator

import dev.skymansandy.wiretap.domain.model.HttpLog
import dev.skymansandy.wiretap.helper.logger.WiretapLogger

/**
 * Prints what [delegate] records to the platform console.
 *
 * [HttpLogManager] is a singleton shared by every installation and the inspector UI, but
 * console output is per-plugin (`WiretapHttpConfig.consoleLogging`) — so the choice is made by
 * wrapping at install time, not by a flag inside the manager.
 */
internal class ConsoleLoggingHttpLogManager(
    private val delegate: HttpLogManager,
    private val logger: WiretapLogger,
) : HttpLogManager by delegate {

    override suspend fun logHttp(entry: HttpLog) {
        delegate.logHttp(entry)
        logger.logHttp(entry)
    }

    override suspend fun logHttpAndGetId(entry: HttpLog): Long {
        val id = delegate.logHttpAndGetId(entry)
        logger.logHttp(entry.copy(id = id))
        return id
    }

    override suspend fun updateHttp(entry: HttpLog) {
        delegate.updateHttp(entry)
        logger.logHttp(entry)
    }
}
