/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.plugin.sse

import dev.skymansandy.wiretap.domain.model.SseConnection
import dev.skymansandy.wiretap.domain.model.SseEvent
import dev.skymansandy.wiretap.domain.model.SseStatus
import dev.skymansandy.wiretap.domain.orchestrator.SseLogManager
import dev.skymansandy.wiretap.helper.markers.ExperimentalWiretapSseApi
import dev.skymansandy.wiretap.helper.util.currentTimeMillis
import io.ktor.client.call.HttpClientCall
import io.ktor.client.plugins.sse.ClientSSESession
import io.ktor.sse.ServerSentEvent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * [WiretapSseSession] implementation that logs all incoming SSE events.
 *
 * Detects session completion via flow onCompletion (cancellation, server close, error)
 * and updates the connection status accordingly.
 */
@OptIn(ExperimentalWiretapSseApi::class)
internal class LoggingSseSession(
    private val delegate: ClientSSESession,
    private val connectionId: Long,
    private val sseLogManager: SseLogManager,
) : WiretapSseSession {

    override val call: HttpClientCall = delegate.call

    private val logScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val incoming: Flow<ServerSentEvent> = delegate.incoming
        .onEach { event -> logEvent(event) }
        .onCompletion { cause -> onSessionClosed(cause) }

    private fun onSessionClosed(cause: Throwable?) {
        val url = delegate.call.request.url.toString()
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
