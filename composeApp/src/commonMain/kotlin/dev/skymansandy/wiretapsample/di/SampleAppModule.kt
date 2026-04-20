package dev.skymansandy.wiretapsample.di

import de.jensklingenberg.ktorfit.Ktorfit
import dev.skymansandy.wiretap.plugin.http.WiretapKtorHttpPlugin
import dev.skymansandy.wiretap.plugin.ws.WiretapKtorWebSocketPlugin
import dev.skymansandy.wiretapsample.api._ExternalApiProvider
import dev.skymansandy.wiretapsample.api._HttpBinApiProvider
import dev.skymansandy.wiretapsample.api._JsonPlaceholderApiProvider
import dev.skymansandy.wiretapsample.model.KtorfitApis
import dev.skymansandy.wiretapsample.viewmodel.KtorHttpViewModel
import dev.skymansandy.wiretapsample.viewmodel.KtorWebSocketViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
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

            install(ContentNegotiation) {
                json()
            }

            install(WiretapKtorWebSocketPlugin)
            install(WiretapKtorHttpPlugin)
        }
    }

    single {
        val client: HttpClient = get()

        val jsonPlaceholderKtorfit = Ktorfit.Builder()
            .baseUrl("https://jsonplaceholder.typicode.com/")
            .httpClient(client)
            .build()
        val jsonPlaceholder = _JsonPlaceholderApiProvider().create(jsonPlaceholderKtorfit)

        val httpBinKtorfit = Ktorfit.Builder()
            .baseUrl("https://httpbin.org/")
            .httpClient(client)
            .build()
        val httpBin = _HttpBinApiProvider().create(httpBinKtorfit)

        val httpBinHttpKtorfit = Ktorfit.Builder()
            .baseUrl("http://httpbin.org/")
            .httpClient(client)
            .build()
        val httpBinHttp = _HttpBinApiProvider().create(httpBinHttpKtorfit)

        val externalKtorfit = Ktorfit.Builder()
            .baseUrl("https://example.com/")
            .httpClient(client)
            .build()
        val external = _ExternalApiProvider().create(externalKtorfit)

        KtorfitApis(
            jsonPlaceholder = jsonPlaceholder,
            httpBin = httpBin,
            httpBinHttp = httpBinHttp,
            external = external,
            client = client,
        )
    }

    viewModelOf(::KtorHttpViewModel)

    viewModelOf(::KtorWebSocketViewModel)
}
