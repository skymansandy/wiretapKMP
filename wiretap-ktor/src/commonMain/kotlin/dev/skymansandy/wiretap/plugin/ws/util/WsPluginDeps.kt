/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.plugin.ws.util

import dev.skymansandy.wiretap.di.WiretapDi
import dev.skymansandy.wiretap.domain.orchestrator.SocketLogManager
import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal class WsPluginDeps : KoinComponent {

    override fun getKoin(): Koin = WiretapDi.getKoin()

    val socketLogManager by inject<SocketLogManager>()
}
