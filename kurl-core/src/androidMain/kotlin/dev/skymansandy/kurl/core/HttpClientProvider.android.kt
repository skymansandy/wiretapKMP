package dev.skymansandy.kurl.core

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.Protocol

internal actual fun createHttpClient(): HttpClient = HttpClient(OkHttp) {
    engine {
        config {
            protocols(listOf(Protocol.HTTP_2, Protocol.HTTP_1_1))
        }
    }
}