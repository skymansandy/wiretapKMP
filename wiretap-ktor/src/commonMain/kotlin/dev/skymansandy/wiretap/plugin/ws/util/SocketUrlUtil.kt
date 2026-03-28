package dev.skymansandy.wiretap.plugin.ws.util

internal fun String.toWebSocketUrl(): String =
    replaceFirst("http://", "ws://")
        .replaceFirst("https://", "wss://")
