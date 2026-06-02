/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.plugin.ws

import dev.skymansandy.wiretap.domain.model.SocketConnection
import dev.skymansandy.wiretap.domain.model.SocketStatus
import dev.skymansandy.wiretap.domain.model.config.ws.WiretapWsConfig
import dev.skymansandy.wiretap.helper.util.currentTimeMillis
import dev.skymansandy.wiretap.plugin.ws.util.WsPluginDeps
import dev.skymansandy.wiretap.plugin.ws.util.toWebSocketUrl
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpClientPlugin
import io.ktor.client.request.HttpRequestPipeline
import io.ktor.client.statement.HttpReceivePipeline
import io.ktor.client.statement.HttpResponseContainer
import io.ktor.client.statement.HttpResponsePipeline
import io.ktor.util.AttributeKey
import io.ktor.websocket.WebSocketSession

internal val WiretapSocketIdKey = AttributeKey<Long>("WiretapSocketId")
internal val WiretapWsEnabledKey = AttributeKey<Boolean>("WiretapWsEnabled")

/**
 * Configuration holder for the installed plugin.
 *
 * The class is public because [HttpClientPlugin] surfaces it as a type
 * parameter, but the constructor and [enabled] flag are internal —
 * consumers configure the plugin via the [WiretapWsConfig] DSL, not by
 * reading state off the handler.
 */
class WiretapWsPluginHandler internal constructor(
    internal val enabled: Boolean,
)

/**
 * Ktor client plugin that automatically intercepts WebSocket sessions
 * to log connections and messages via Wiretap.
 *
 * All WebSocket sessions are wrapped transparently at the raw engine
 * level — no manual [wiretapped] call is needed. This ensures libraries
 * that manage their own WebSocket sessions internally (e.g. SignalRKore)
 * are also captured.
 *
 * **Ktor-version coupling:** this implementation depends on Ktor internals
 * that [createClientPlugin] does not expose — specifically the response
 * pipeline ([HttpResponsePipeline.Parse]) and the [HttpClientPlugin]
 * interface. The `Parse` phase is intentional: it runs before Ktor's own
 * `WebSockets` plugin transforms the raw [WebSocketSession] into
 * `DefaultClientWebSocketSession`, giving us a chance to wrap the raw
 * session first. If Ktor renames these phases or restructures the
 * `WebSockets` plugin's transform, this plugin will need to be revisited.
 *
 * Usage:
 * ```kotlin
 * HttpClient {
 *     install(WebSockets)
 *     install(WiretapKtorWebSocketPlugin) {
 *         enabled = true
 *     }
 * }
 * ```
 */
val WiretapKtorWebSocketPlugin =
    object : HttpClientPlugin<WiretapWsConfig, WiretapWsPluginHandler> {

        override val key = AttributeKey<WiretapWsPluginHandler>("WiretapWebSocketPlugin")

        override fun prepare(block: WiretapWsConfig.() -> Unit): WiretapWsPluginHandler {
            val config = WiretapWsConfig().apply(block)
            return WiretapWsPluginHandler(config.enabled)
        }

        override fun install(plugin: WiretapWsPluginHandler, scope: HttpClient) {
            val deps = WsPluginDeps()

            // Store enabled flag in request attributes
            scope.requestPipeline.intercept(HttpRequestPipeline.State) {
                context.attributes.put(WiretapWsEnabledKey, plugin.enabled)
                proceed()
            }

            // Detect 101 upgrade, create socket entry, store socketId
            scope.receivePipeline.intercept(HttpReceivePipeline.After) {
                val response = subject
                if (response.status.value != 101 || !plugin.enabled) {
                    proceed()
                    return@intercept
                }

                val url = response.call.request.url.toString().toWebSocketUrl()
                val requestHeaders = response.call.request.headers.entries()
                    .associate { (key, values) -> key to values.joinToString(", ") }

                val socketId = deps.socketLogManager.createSocket(
                    SocketConnection(
                        url = url,
                        requestHeaders = requestHeaders,
                        status = SocketStatus.Open,
                        timestamp = currentTimeMillis(),
                        protocol = response.version.let { "${it.name}/${it.major}.${it.minor}" },
                    ),
                )

                response.call.request.attributes.put(WiretapSocketIdKey, socketId)
                proceed()
            }

            // Wrap the raw WebSocketSession BEFORE Ktor's WebSockets plugin
            // processes it at Transform phase. This ensures all frame I/O
            // is logged transparently, even for libraries that manage their
            // own sessions internally.
            scope.responsePipeline.intercept(HttpResponsePipeline.Parse) {
                val (info, body) = subject
                val rawSession = body as? WebSocketSession ?: return@intercept
                if (!plugin.enabled) return@intercept

                val socketId = context.request.attributes.getOrNull(WiretapSocketIdKey)
                    ?: return@intercept

                val url = context.request.url.toString().toWebSocketUrl()
                val wrappedSession = LoggingRawWebSocketSession(
                    delegate = rawSession,
                    socketId = socketId,
                    url = url,
                    socketLogManager = deps.socketLogManager,
                )
                proceedWith(HttpResponseContainer(info, wrappedSession))
            }
        }
    }
