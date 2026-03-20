package dev.skymansandy.wiretapsample.di

import dev.skymansandy.wiretap.plugin.WiretapKtorPlugin
import dev.skymansandy.wiretap.plugin.WiretapKtorWebSocketPlugin
import dev.skymansandy.wiretapsample.viewmodel.HttpViewModel
import dev.skymansandy.wiretapsample.viewmodel.WebSocketViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.websocket.WebSockets
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

internal val sampleAppModule = module {

    single {
        HttpClient {
            install(WebSockets)
            install(WiretapKtorWebSocketPlugin)
            install(HttpTimeout)
            install(WiretapKtorPlugin)
        }
    }

    viewModelOf(::HttpViewModel)

    viewModelOf(::WebSocketViewModel)
}
