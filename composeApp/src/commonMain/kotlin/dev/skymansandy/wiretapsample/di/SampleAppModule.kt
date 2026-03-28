package dev.skymansandy.wiretapsample.di

import dev.skymansandy.wiretap.plugin.WiretapKtorHttpPlugin
import dev.skymansandy.wiretap.plugin.WiretapKtorWebSocketPlugin
import dev.skymansandy.wiretapsample.viewmodel.KtorHttpViewModel
import dev.skymansandy.wiretapsample.viewmodel.KtorWebSocketViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.websocket.WebSockets
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import kotlin.time.Duration.Companion.seconds

val sampleAppModule = module {

    single {
        HttpClient {
            install(HttpTimeout)
            install(WebSockets) {
                pingIntervalMillis = 5.seconds.inWholeMilliseconds
            }

            install(WiretapKtorWebSocketPlugin)
            install(WiretapKtorHttpPlugin)
        }
    }

    viewModelOf(::KtorHttpViewModel)

    viewModelOf(::KtorWebSocketViewModel)
}
