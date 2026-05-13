/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.plugin.sse

import dev.skymansandy.wiretap.domain.model.SseConnection
import dev.skymansandy.wiretap.domain.model.SseEvent
import dev.skymansandy.wiretap.domain.model.SseStatus
import dev.skymansandy.wiretap.domain.orchestrator.SseLogManager
import dev.skymansandy.wiretap.helper.util.currentTimeMillis
import io.ktor.client.plugins.sse.SSESession
import io.ktor.sse.ServerSentEvent
import io.ktor.utils.io.InternalAPI
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * [SSESession] wrapper that logs all incoming SSE events via Wiretap.
 *
 * Intercepts the delegate's [incoming] flow with [onEach] / [onCompletion]
 * operators so every event and session lifecycle change is recorded.
 */
internal class LoggingSseSession(
    private val delegate: SSESession,
    private val connectionId: Long,
    private val url: String,
    private val sseLogManager: SseLogManager,
) : SSESession {

    override val coroutineContext: CoroutineContext = delegate.coroutineContext

    private val logScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val incoming: Flow<ServerSentEvent> = delegate.incoming
        .onEach { event -> logEvent(event) }
        .onCompletion { cause -> onSessionClosed(cause) }

    @InternalAPI
    override fun bodyBuffer(): ByteArray = delegate.bodyBuffer()

    private fun onSessionClosed(cause: Throwable?) {
        logScope.launch {
            if (cause != null && cause !is CancellationException) {
                sseLogManager.updateConnection(
                    SseConnection(
                        id = connectionId,
                        url = url,
                        status = SseStatus.Failed,
                        failureMessage = cause.message ?: cause::class.simpleName ?: "Unknown error",
                        closedAt = currentTimeMillis(),
                        timestamp = currentTimeMillis(),
                    ),
                )
            } else {
                sseLogManager.updateConnection(
                    SseConnection(
                        id = connectionId,
                        url = url,
                        status = SseStatus.Closed,
                        closedAt = currentTimeMillis(),
                        timestamp = currentTimeMillis(),
                    ),
                )
            }
        }
    }

    private suspend fun logEvent(event: ServerSentEvent) {
        val data = event.data ?: return
        val byteCount = data.encodeToByteArray().size.toLong()

        sseLogManager.logEvent(
            SseEvent(
                connectionId = connectionId,
                eventType = event.event,
                data = data,
                eventId = event.id,
                retryMs = event.retry,
                byteCount = byteCount,
                timestamp = currentTimeMillis(),
            ),
        )
    }
}
