/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.domain.orchestrator

import dev.skymansandy.wiretap.helper.logger.WiretapLogger
import dev.skymansandy.wiretap.helper.markers.InternalWiretapApi

/**
 * Returns a view of this manager that also prints what it records to the platform console.
 *
 * Called once per plugin installation, from the plugin module that owns the
 * `consoleLogging` config flag. Public only because the plugin modules are separate Gradle
 * modules; not part of Wiretap's consumer-facing API.
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
