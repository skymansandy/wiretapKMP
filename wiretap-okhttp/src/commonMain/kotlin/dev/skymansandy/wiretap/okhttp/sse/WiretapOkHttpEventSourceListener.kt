/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.okhttp.sse

import dev.skymansandy.wiretap.di.WiretapDi
import dev.skymansandy.wiretap.domain.model.SseConnection
import dev.skymansandy.wiretap.domain.model.SseEvent
import dev.skymansandy.wiretap.domain.model.SseStatus
import dev.skymansandy.wiretap.domain.model.config.sse.WiretapSseConfig
import dev.skymansandy.wiretap.domain.orchestrator.SseLogManager
import dev.skymansandy.wiretap.helper.markers.ExperimentalWiretapSseApi
import dev.skymansandy.wiretap.helper.util.currentTimeMillis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.concurrent.Volatile

/**
 * Wraps a consumer's [EventSourceListener] to log all SSE events via Wiretap.
 *
 * Use the [wiretapped] extension to create an instance:
 * ```kotlin
 * val factory = EventSources.createFactory(client)
 * val source = factory.newEventSource(request, myListener.wiretapped())
 * ```
 */
@ExperimentalWiretapSseApi
internal class WiretapOkHttpEventSourceListener(
    private val delegate: EventSourceListener,
    private val config: WiretapSseConfig,
) : EventSourceListener(), KoinComponent {

    override fun getKoin(): Koin = WiretapDi.getKoin()

    private val sseLogManager: SseLogManager by inject()
    private val logScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var connectionId: Long = -1
    private val isConnectionActive
        get() = connectionId >= 0

    override fun onOpen(eventSource: EventSource, response: Response) {
        if (!config.enabled) {
            delegate.onOpen(eventSource, response)
            return
        }
        // runBlocking required here: connectionId must be set before delegate.onOpen
        runBlocking {
            val url = eventSource.request().url.toString()
            val reqHeaders = eventSource.request().headers.toMap()

            connectionId = sseLogManager.createConnection(
                SseConnection(
                    url = url,
                    requestHeaders = reqHeaders,
                    status = SseStatus.Open,
                    timestamp = currentTimeMillis(),
                ),
            )
        }

        delegate.onOpen(eventSource, response)
    }

    override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
        if (isConnectionActive) {
            logScope.launch {
                sseLogManager.logEvent(
                    SseEvent(
                        connectionId = connectionId,
                        eventType = type,
                        data = data,
                        eventId = id,
                        byteCount = data.encodeToByteArray().size.toLong(),
                        timestamp = currentTimeMillis(),
                    ),
                )
            }
        }

        delegate.onEvent(eventSource, id, type, data)
    }

    override fun onClosed(eventSource: EventSource) {
        if (isConnectionActive) {
            logScope.launch {
                sseLogManager.updateConnection(
                    SseConnection(
                        id = connectionId,
                        url = eventSource.request().url.toString(),
                        status = SseStatus.Closed,
                        closedAt = currentTimeMillis(),
                        timestamp = currentTimeMillis(),
                    ),
                )
            }
        }

        delegate.onClosed(eventSource)
    }

    override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
        if (isConnectionActive) {
            logScope.launch {
                sseLogManager.updateConnection(
                    SseConnection(
                        id = connectionId,
                        url = eventSource.request().url.toString(),
                        status = SseStatus.Failed,
                        failureMessage = t?.message ?: t?.let { it::class.simpleName }
                            ?: "Unknown error",
                        closedAt = currentTimeMillis(),
                        timestamp = currentTimeMillis(),
                    ),
                )
            }
        }

        delegate.onFailure(eventSource, t, response)
    }
}

/**
 * Wraps this [EventSourceListener] with Wiretap logging.
 *
 * ```kotlin
 * val source = factory.newEventSource(request, myListener.wiretapped())
 * ```
 */
@ExperimentalWiretapSseApi
@Deprecated(
    message = "Use wiretapped() with config builder instead.",
    replaceWith = ReplaceWith("wiretapped { enabled = true }"),
)
fun EventSourceListener.wiretapped(
    enabled: Boolean = true,
): EventSourceListener = WiretapOkHttpEventSourceListener(
    delegate = this,
    config = WiretapSseConfig().apply { this.enabled = enabled },
)

@ExperimentalWiretapSseApi
fun EventSourceListener.wiretapped(
    configure: WiretapSseConfig.() -> Unit,
): EventSourceListener = WiretapOkHttpEventSourceListener(
    delegate = this,
    config = WiretapSseConfig().apply(configure),
)
