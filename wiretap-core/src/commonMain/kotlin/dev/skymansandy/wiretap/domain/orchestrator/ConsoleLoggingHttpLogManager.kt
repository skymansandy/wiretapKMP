/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.domain.orchestrator

import dev.skymansandy.wiretap.domain.model.HttpLog
import dev.skymansandy.wiretap.helper.logger.WiretapLogger

/**
 * Mirrors everything [delegate] records to the platform console.
 *
 * Console output is a per-plugin choice (`WiretapHttpConfig.consoleLogging`), while the
 * underlying [HttpLogManager] is a singleton shared by every plugin installation and by the
 * inspector UI. Wrapping lets each installation pick its own behaviour at install time without
 * the manager — or any of its call sites — knowing a preference exists.
 *
 * Reads go straight to [delegate] via interface delegation.
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
