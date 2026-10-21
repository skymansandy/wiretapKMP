/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.plugin.ws.util

import dev.skymansandy.wiretap.di.WiretapDi
import dev.skymansandy.wiretap.domain.orchestrator.SocketLogManager
import dev.skymansandy.wiretap.domain.orchestrator.withConsoleLogging
import dev.skymansandy.wiretap.helper.logger.WiretapLogger
import dev.skymansandy.wiretap.helper.markers.InternalWiretapApi
import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@OptIn(InternalWiretapApi::class)
internal class WsPluginDeps(consoleLogging: Boolean) : KoinComponent {

    override fun getKoin(): Koin = WiretapDi.getKoin()

    private val wiretapLogger by inject<WiretapLogger>()
    private val sharedSocketLogManager by inject<SocketLogManager>()

    val socketLogManager: SocketLogManager by lazy {
        if (consoleLogging) sharedSocketLogManager.withConsoleLogging(wiretapLogger)
        else sharedSocketLogManager
    }
}
