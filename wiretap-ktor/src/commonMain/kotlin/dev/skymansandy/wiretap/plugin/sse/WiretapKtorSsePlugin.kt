/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.plugin.sse

import dev.skymansandy.wiretap.domain.model.SseConnection
import dev.skymansandy.wiretap.domain.model.SseStatus
import dev.skymansandy.wiretap.domain.model.config.sse.WiretapSseConfig
import dev.skymansandy.wiretap.helper.markers.ExperimentalWiretapSseApi
import dev.skymansandy.wiretap.helper.util.currentTimeMillis
import dev.skymansandy.wiretap.plugin.sse.util.SsePluginDeps
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpClientPlugin
import io.ktor.client.plugins.sse.SSESession
import io.ktor.client.request.HttpRequestPipeline
import io.ktor.client.statement.HttpResponseContainer
import io.ktor.client.statement.HttpResponsePipeline
import io.ktor.util.AttributeKey

internal val WiretapSseEnabledKey = AttributeKey<Boolean>("WiretapSseEnabled")

/**
 * Configuration holder for the installed plugin.
 */
@ExperimentalWiretapSseApi
class WiretapSsePluginHandler internal constructor(val enabled: Boolean)

/**
 * Ktor client plugin that automatically intercepts SSE sessions
 * to log connections and events via Wiretap.
 *
 * All SSE sessions are wrapped transparently at the [SSESession]
 * level — no manual [wiretapped] call is needed. The plugin intercepts
 * at [HttpResponsePipeline.Parse] (before Ktor's SSE plugin wraps the
 * session into [io.ktor.client.plugins.sse.ClientSSESession] at Transform),
 * ensuring all event I/O is logged transparently.
 *
 * Usage:
 * ```kotlin
 * HttpClient {
 *     install(SSE)
 *     install(WiretapKtorSsePlugin) {
 *         enabled = true
 *     }
 * }
 * ```
 */
@ExperimentalWiretapSseApi
val WiretapKtorSsePlugin =
    object : HttpClientPlugin<WiretapSseConfig, WiretapSsePluginHandler> {

        override val key = AttributeKey<WiretapSsePluginHandler>("WiretapSsePlugin")

        override fun prepare(block: WiretapSseConfig.() -> Unit): WiretapSsePluginHandler {
            val config = WiretapSseConfig().apply(block)
            return WiretapSsePluginHandler(config.enabled)
        }

        override fun install(plugin: WiretapSsePluginHandler, scope: HttpClient) {
            val deps = SsePluginDeps()

            // Store enabled flag in request attributes
            scope.requestPipeline.intercept(HttpRequestPipeline.State) {
                context.attributes.put(WiretapSseEnabledKey, plugin.enabled)
                proceed()
            }

            // Wrap the raw SSESession BEFORE Ktor's SSE plugin processes it
            // at Transform phase. This ensures all event I/O is logged
            // transparently, without requiring an explicit wiretapped() call.
            scope.responsePipeline.intercept(HttpResponsePipeline.Parse) {
                val (info, body) = subject
                val sseSession = body as? SSESession ?: return@intercept
                if (!plugin.enabled) return@intercept

                val url = context.request.url.toString()
                val requestHeaders = context.request.headers.entries()
                    .associate { (key, values) -> key to values.joinToString(", ") }

                val connectionId = deps.sseLogManager.createConnection(
                    SseConnection(
                        url = url,
                        requestHeaders = requestHeaders,
                        status = SseStatus.Open,
                        timestamp = currentTimeMillis(),
                    ),
                )

                val wrappedSession = LoggingSseSession(
                    delegate = sseSession,
                    connectionId = connectionId,
                    url = url,
                    sseLogManager = deps.sseLogManager,
                )
                proceedWith(HttpResponseContainer(info, wrappedSession))
            }
        }
    }
