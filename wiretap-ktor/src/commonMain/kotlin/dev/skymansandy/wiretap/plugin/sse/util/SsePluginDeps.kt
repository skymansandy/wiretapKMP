/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.plugin.sse.util

import dev.skymansandy.wiretap.di.WiretapDi
import dev.skymansandy.wiretap.domain.orchestrator.SseLogManager
import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal class SsePluginDeps : KoinComponent {

    override fun getKoin(): Koin = WiretapDi.getKoin()

    val sseLogManager by inject<SseLogManager>()
}
