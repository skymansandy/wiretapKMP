/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.plugin.ws

import co.touchlab.stately.concurrency.AtomicBoolean
import dev.skymansandy.wiretap.domain.model.SocketConnection
import dev.skymansandy.wiretap.domain.model.SocketContentType
import dev.skymansandy.wiretap.domain.model.SocketMessage
import dev.skymansandy.wiretap.domain.model.SocketMessageType
import dev.skymansandy.wiretap.domain.model.SocketStatus
import dev.skymansandy.wiretap.domain.orchestrator.SocketLogManager
import dev.skymansandy.wiretap.helper.util.currentTimeMillis
import dev.skymansandy.wiretap.plugin.ws.util.toWebSocketUrl
import io.ktor.client.call.HttpClientCall
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.websocket.CloseReason
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readBytes
import io.ktor.websocket.readText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.launch

/**
 * [WiretapWebSocketSession] implementation that logs all sent and received messages.
 *
 * Automatically detects session completion (timeout, server close, error) and
 * updates the socket status accordingly — no manual `markClosed()`/`markFailed()` needed.
 */
internal class LoggingWebSocketSession(
    private val delegate: DefaultClientWebSocketSession,
    private val socketId: Long,
    private val socketLogManager: SocketLogManager,
) : WiretapWebSocketSession, DefaultWebSocketSession by delegate {

    override val call: HttpClientCall = delegate.call

    @OptIn(ExperimentalCoroutinesApi::class)
    override val incoming: ReceiveChannel<Frame> = delegate.produce {
        for (frame in delegate.incoming) {
            logFrame(frame, SocketMessageType.Received)
            send(frame)
        }
    }

    private val logScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val statusUpdated = AtomicBoolean(false)

    init {
        installAutoClose()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun installAutoClose() {
        delegate.coroutineContext[Job]?.invokeOnCompletion { cause ->
            if (!statusUpdated.compareAndSet(expected = false, new = true)) return@invokeOnCompletion

            val url = delegate.call.request.url.toString().toWebSocketUrl()
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
                    val closeReason = runCatching { delegate.closeReason.getCompleted() }.getOrNull()
                    socketLogManager.updateSocket(
                        SocketConnection(
                            id = socketId,
                            url = url,
                            status = SocketStatus.Closed,
                            closeCode = closeReason?.code?.toInt(),
                            closeReason = closeReason?.message ?: if (cause is CancellationException) "Cancelled" else null,
                            closedAt = currentTimeMillis(),
                            timestamp = currentTimeMillis(),
                        ),
                    )
                }
            }
        }
    }

    override suspend fun send(frame: Frame) {
        logFrame(frame, SocketMessageType.Sent)
        delegate.send(frame)
    }

    suspend fun close(code: Short = 1000.toShort(), reason: String? = null) {
        statusUpdated.value = true

        val url = delegate.call.request.url.toString().toWebSocketUrl()
        socketLogManager.updateSocket(
            SocketConnection(
                id = socketId,
                url = url,
                status = SocketStatus.Closed,
                closeCode = code.toInt(),
                closeReason = reason,
                closedAt = currentTimeMillis(),
                timestamp = currentTimeMillis(),
            ),
        )

        delegate.close(CloseReason(code, reason ?: ""))
    }

    private suspend fun logFrame(frame: Frame, direction: SocketMessageType) {
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

            else -> return
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
