/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.okhttp

import okhttp3.WebSocketListener

/**
 * Factory that creates [RealOkHttpWebSocketListener] instances for Wiretap logging.
 */
internal class RealOkHttpWsListenerFactory : WiretapOkHttpWsListenerFactory {

    override fun create(delegate: WebSocketListener): WebSocketListener =
        RealOkHttpWebSocketListener(delegate)
}
