package dev.skymansandy.kurl.core

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin

internal actual fun createHttpClient(): HttpClient = HttpClient(Darwin) {
    engine {
        configureSession {
            // HTTP/2 is enabled by default in NSURLSession
            // Nothing extra needed — iOS handles it automatically
        }
    }
}
