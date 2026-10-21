/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.domain.orchestrator

import dev.skymansandy.wiretap.helper.logger.WiretapLogger
import dev.skymansandy.wiretap.helper.markers.InternalWiretapApi

/**
 * Wraps this manager so it also prints what it records to the platform console.
 *
 * Called once per plugin installation; public only so the plugin modules can reach it.
 */
@InternalWiretapApi
fun HttpLogManager.withConsoleLogging(logger: WiretapLogger): HttpLogManager =
    ConsoleLoggingHttpLogManager(this, logger)

/** WebSocket counterpart of [withConsoleLogging]. */
@InternalWiretapApi
fun SocketLogManager.withConsoleLogging(logger: WiretapLogger): SocketLogManager =
    ConsoleLoggingSocketLogManager(this, logger)

/** SSE counterpart of [withConsoleLogging]. */
@InternalWiretapApi
fun SseLogManager.withConsoleLogging(logger: WiretapLogger): SseLogManager =
    ConsoleLoggingSseLogManager(this, logger)
