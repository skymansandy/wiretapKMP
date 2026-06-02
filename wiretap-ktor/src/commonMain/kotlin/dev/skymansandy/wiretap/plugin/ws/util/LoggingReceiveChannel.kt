/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.plugin.ws.util

import io.ktor.websocket.Frame
import kotlinx.coroutines.channels.ChannelIterator
import kotlinx.coroutines.channels.ChannelResult
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.selects.SelectClause1

/**
 * Wraps a [ReceiveChannel] to log frames as they are consumed.
 * No coroutines are launched — logging is triggered inline on each receive.
 */
internal class LoggingReceiveChannel(
    private val delegate: ReceiveChannel<Frame>,
    private val logAction: (Frame) -> Unit,
    private val onChannelClosed: (Throwable?) -> Unit,
) : ReceiveChannel<Frame> by delegate {

    override suspend fun receive(): Frame {
        return delegate.receive().also { logAction(it) }
    }

    override suspend fun receiveCatching(): ChannelResult<Frame> {
        return delegate.receiveCatching().also { result ->
            result.getOrNull()?.let { logAction(it) }
            if (result.isClosed) onChannelClosed(result.exceptionOrNull())
        }
    }

    override fun tryReceive(): ChannelResult<Frame> {
        return delegate.tryReceive().also { result ->
            result.getOrNull()?.let { logAction(it) }
            if (result.isClosed) onChannelClosed(result.exceptionOrNull())
        }
    }

    override fun iterator(): ChannelIterator<Frame> {
        val delegateIterator = delegate.iterator()
        return object : ChannelIterator<Frame> {
            override suspend fun hasNext(): Boolean {
                return delegateIterator.hasNext().also { hasMore ->
                    if (!hasMore) onChannelClosed(null)
                }
            }
            override fun next(): Frame = delegateIterator.next().also { logAction(it) }
        }
    }

    override val onReceive: SelectClause1<Frame>
        get() = delegate.onReceive

    override val onReceiveCatching: SelectClause1<ChannelResult<Frame>>
        get() = delegate.onReceiveCatching
}
