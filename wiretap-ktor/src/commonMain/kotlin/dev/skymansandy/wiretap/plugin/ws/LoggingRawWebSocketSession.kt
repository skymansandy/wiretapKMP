/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.plugin.ws

import dev.skymansandy.wiretap.domain.model.SocketConnection
import dev.skymansandy.wiretap.domain.model.SocketContentType
import dev.skymansandy.wiretap.domain.model.SocketMessage
import dev.skymansandy.wiretap.domain.model.SocketMessageType
import dev.skymansandy.wiretap.domain.model.SocketStatus
import dev.skymansandy.wiretap.domain.orchestrator.SocketLogManager
import dev.skymansandy.wiretap.helper.util.currentTimeMillis
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketExtension
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.readBytes
import io.ktor.websocket.readText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.ChannelIterator
import kotlinx.coroutines.channels.ChannelResult
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.SelectClause1

/**
 * Wraps a raw [WebSocketSession] from the engine to log all frames
 * passing through, **before** Ktor's `WebSockets` plugin wraps it
 * in `DefaultWebSocketSession` / `DefaultClientWebSocketSession`.
 *
 * This enables transparent interception of libraries that manage
 * their own WebSocket sessions internally (e.g. SignalRKore),
 * without requiring an explicit `wiretapped()` call.
 */
internal class LoggingRawWebSocketSession(
    private val delegate: WebSocketSession,
    private val socketId: Long,
    private val url: String,
    private val socketLogManager: SocketLogManager,
) : WebSocketSession {

    override val coroutineContext = delegate.coroutineContext
    override var masking: Boolean
        get() = runCatching { delegate.masking }.getOrDefault(true)
        set(value) { runCatching { delegate.masking = value } }
    override var maxFrameSize: Long
        get() = runCatching { delegate.maxFrameSize }.getOrDefault(Long.MAX_VALUE)
        set(value) { runCatching { delegate.maxFrameSize = value } }
    override val extensions: List<WebSocketExtension<*>>
        get() = runCatching { delegate.extensions }.getOrDefault(emptyList())

    private val logScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        delegate.coroutineContext[Job]?.invokeOnCompletion { cause ->
            logScope.launch {
                if (cause != null && cause !is CancellationException) {
                    socketLogManager.updateSocket(
                        SocketConnection(
                            id = socketId,
                            url = url,
                            status = SocketStatus.Failed,
                            failureMessage = cause.message ?: cause::class.simpleName ?: "Unknown error",
                            closedAt = currentTimeMillis(),
                            timestamp = currentTimeMillis(),
                        ),
                    )
                } else {
                    socketLogManager.updateSocket(
                        SocketConnection(
                            id = socketId,
                            url = url,
                            status = SocketStatus.Closed,
                            closeReason = if (cause is CancellationException) "Cancelled" else null,
                            closedAt = currentTimeMillis(),
                            timestamp = currentTimeMillis(),
                        ),
                    )
                }
            }
        }
    }

    /**
     * Wraps the delegate's incoming channel without launching any coroutines.
     * This avoids scope/lifecycle issues — the channel stays open as long as
     * the delegate's channel does, regardless of coroutine Job completion.
     */
    override val incoming: ReceiveChannel<Frame> = LoggingReceiveChannel(
        delegate = delegate.incoming,
        logAction = { frame -> logFrame(frame, SocketMessageType.Received) },
    )

    override val outgoing: SendChannel<Frame> = LoggingSendChannel(
        delegate = delegate.outgoing,
        logAction = { frame -> logFrame(frame, SocketMessageType.Sent) },
    )

    override suspend fun send(frame: Frame) {
        logFrame(frame, SocketMessageType.Sent)
        delegate.send(frame)
    }

    override suspend fun flush() {
        delegate.flush()
    }

    @Deprecated("Use cancel() instead.", replaceWith = ReplaceWith("cancel()"), level = DeprecationLevel.ERROR)
    override fun terminate() {
        @Suppress("DEPRECATION_ERROR")
        delegate.terminate()
    }

    private fun logFrame(frame: Frame, direction: SocketMessageType) {
        logScope.launch {
            val (contentType, content, byteCount) = when (frame) {
                is Frame.Text -> {
                    val text = frame.readText()
                    Triple(SocketContentType.Text, text, text.encodeToByteArray().size.toLong())
                }

                is Frame.Binary -> {
                    val bytes = frame.readBytes()
                    Triple(SocketContentType.Binary, "[Binary: ${bytes.size} bytes]", bytes.size.toLong())
                }

                is Frame.Ping -> Triple(SocketContentType.Ping, "", frame.data.size.toLong())
                is Frame.Pong -> Triple(SocketContentType.Pong, "", frame.data.size.toLong())
                is Frame.Close -> {
                    val bytes = frame.data
                    val closeContent = if (bytes.size >= 2) {
                        val closeCode = (bytes[0].toInt() and 0xFF shl 8) or (bytes[1].toInt() and 0xFF)
                        val closeReason = if (bytes.size > 2) bytes.decodeToString(2, bytes.size) else ""
                        if (closeReason.isNotEmpty()) "$closeCode $closeReason" else "$closeCode"
                    } else {
                        ""
                    }
                    Triple(SocketContentType.Close, closeContent, bytes.size.toLong())
                }

                else -> return@launch
            }

            socketLogManager.logSocketMsg(
                SocketMessage(
                    socketId = socketId,
                    direction = direction,
                    contentType = contentType,
                    content = content,
                    byteCount = byteCount,
                    timestamp = currentTimeMillis(),
                ),
            )
        }
    }
}

/**
 * Wraps a [ReceiveChannel] to log frames as they are consumed.
 * No coroutines are launched — logging is triggered inline on each receive.
 */
private class LoggingReceiveChannel(
    private val delegate: ReceiveChannel<Frame>,
    private val logAction: (Frame) -> Unit,
) : ReceiveChannel<Frame> by delegate {

    override suspend fun receive(): Frame {
        return delegate.receive().also { logAction(it) }
    }

    override suspend fun receiveCatching(): ChannelResult<Frame> {
        return delegate.receiveCatching().also { result ->
            result.getOrNull()?.let { logAction(it) }
        }
    }

    override fun tryReceive(): ChannelResult<Frame> {
        return delegate.tryReceive().also { result ->
            result.getOrNull()?.let { logAction(it) }
        }
    }

    override fun iterator(): ChannelIterator<Frame> {
        val delegateIterator = delegate.iterator()
        return object : ChannelIterator<Frame> {
            override suspend fun hasNext(): Boolean = delegateIterator.hasNext()
            override fun next(): Frame = delegateIterator.next().also { logAction(it) }
        }
    }

    override val onReceive: SelectClause1<Frame>
        get() = delegate.onReceive

    override val onReceiveCatching: SelectClause1<ChannelResult<Frame>>
        get() = delegate.onReceiveCatching
}

/**
 * Wraps a [SendChannel] to log frames before they are sent.
 */
private class LoggingSendChannel(
    private val delegate: SendChannel<Frame>,
    private val logAction: (Frame) -> Unit,
) : SendChannel<Frame> by delegate {

    override suspend fun send(element: Frame) {
        logAction(element)
        delegate.send(element)
    }
}
