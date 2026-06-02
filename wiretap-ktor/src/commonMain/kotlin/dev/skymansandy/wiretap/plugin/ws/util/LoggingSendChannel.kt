/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.plugin.ws.util

import io.ktor.websocket.Frame
import kotlinx.coroutines.channels.SendChannel

/**
 * Wraps a [SendChannel] to log frames before they are sent.
 */
internal class LoggingSendChannel(
    private val delegate: SendChannel<Frame>,
    private val logAction: (Frame) -> Unit,
) : SendChannel<Frame> by delegate {

    override suspend fun send(element: Frame) {
        logAction(element)
        delegate.send(element)
    }
}
