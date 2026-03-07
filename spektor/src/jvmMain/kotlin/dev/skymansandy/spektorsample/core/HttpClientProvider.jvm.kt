package dev.skymansandy.spektorsample.core

import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java

internal actual fun createHttpClient(): HttpClient = HttpClient(Java) {
    engine {
        config {
            version(java.net.http.HttpClient.Version.HTTP_2)
            sslContext(createCapturingSslContext())
        }
    }
}