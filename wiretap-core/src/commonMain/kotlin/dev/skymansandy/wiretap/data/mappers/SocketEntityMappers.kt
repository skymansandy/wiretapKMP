/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.data.mappers

import dev.skymansandy.wiretap.data.db.room.entity.SocketLogEntity
import dev.skymansandy.wiretap.data.db.room.entity.SocketMessageEntity
import dev.skymansandy.wiretap.domain.model.SocketConnection
import dev.skymansandy.wiretap.domain.model.SocketContentType
import dev.skymansandy.wiretap.domain.model.SocketMessage
import dev.skymansandy.wiretap.domain.model.SocketMessageType
import dev.skymansandy.wiretap.domain.model.SocketStatus
import dev.skymansandy.wiretap.helper.util.HeadersSerializerUtil

internal fun SocketLogEntity.toDomain(): SocketConnection {
    return SocketConnection(
        id = id,
        url = url,
        requestHeaders = HeadersSerializerUtil.deserialize(requestHeaders),
        status = SocketStatus.valueOf(status),
        closeCode = closeCode?.toInt(),
        closeReason = closeReason,
        failureMessage = failureMessage,
        messageCount = messageCount,
        timestamp = timestamp,
        closedAt = closedAt,
        protocol = protocol,
        remoteAddress = remoteAddress,
        historyCleared = historyCleared != 0L,
    )
}

internal fun SocketMessageEntity.toDomain(): SocketMessage {
    return SocketMessage(
        id = id,
        socketId = socketId,
        direction = SocketMessageType.valueOf(direction),
        contentType = SocketContentType.valueOf(contentType),
        content = content,
        byteCount = byteCount,
        timestamp = timestamp,
    )
}

internal fun SocketConnection.toRoomEntity(): SocketLogEntity {
    return SocketLogEntity(
        id = id,
        url = url,
        requestHeaders = HeadersSerializerUtil.serialize(requestHeaders),
        status = status.name,
        closeCode = closeCode?.toLong(),
        closeReason = closeReason,
        failureMessage = failureMessage,
        messageCount = messageCount,
        timestamp = timestamp,
        closedAt = closedAt,
        protocol = protocol,
        remoteAddress = remoteAddress,
        historyCleared = if (historyCleared) 1L else 0L,
    )
}
