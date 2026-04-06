/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.okhttp

import okhttp3.WebSocketListener

/**
 * Factory interface for creating real WebSocket logging listeners.
 *
 * In debug builds, wiretap-okhttp registers an implementation that creates
 * logging listeners. In release builds (API-only), no factory is registered
 * and [WiretapOkHttpWebSocketListener] falls back to pure pass-through.
 */
interface WiretapOkHttpWsListenerFactory {

    fun create(delegate: WebSocketListener): WebSocketListener
}
