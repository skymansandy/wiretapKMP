/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.okhttp.ws

import dev.skymansandy.wiretap.domain.model.SocketContentType
import dev.skymansandy.wiretap.domain.model.SocketMessage
import dev.skymansandy.wiretap.domain.model.SocketMessageType
import dev.skymansandy.wiretap.domain.model.config.ws.WiretapWsConfig
import dev.skymansandy.wiretap.domain.orchestrator.SocketLogManager
import dev.skymansandy.wiretap.helper.markers.InternalWiretapApi
import dev.skymansandy.wiretap.helper.util.currentTimeMillis
import dev.skymansandy.wiretap.helper.ws.decodeBinaryFrame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.WebSocket
import okio.ByteString

/**
 * Wraps an OkHttp WebSocket to intercept outgoing messages for logging.
 */
@OptIn(InternalWiretapApi::class)
internal class WiretapWebSocket(
    private val delegate: WebSocket,
    private val socketId: Long,
    private val socketLogManager: SocketLogManager,
    private val config: WiretapWsConfig = WiretapWsConfig(),
) : WebSocket by delegate {

    private val logScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun send(text: String): Boolean {
        logScope.launch {
            socketLogManager.logSocketMsg(
                SocketMessage(
                    socketId = socketId,
                    direction = SocketMessageType.Sent,
                    contentType = SocketContentType.Text,
                    content = text,
                    byteCount = text.encodeToByteArray().size.toLong(),
                    timestamp = currentTimeMillis(),
                ),
            )
        }
        return delegate.send(text)
    }

    override fun send(bytes: ByteString): Boolean {
        val payload = bytes.toByteArray()
        logScope.launch {
            socketLogManager.logSocketMsg(
                SocketMessage(
                    socketId = socketId,
                    direction = SocketMessageType.Sent,
                    contentType = SocketContentType.Binary,
                    content = decodeBinaryFrame(payload, config.binaryDecoding),
                    byteCount = payload.size.toLong(),
                    timestamp = currentTimeMillis(),
                ),
            )
        }
        return delegate.send(bytes)
    }
}
