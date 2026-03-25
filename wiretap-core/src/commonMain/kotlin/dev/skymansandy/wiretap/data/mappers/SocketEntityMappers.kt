package dev.skymansandy.wiretap.data.mappers

import dev.skymansandy.wiretap.data.db.entity.SocketEntry
import dev.skymansandy.wiretap.data.db.entity.SocketMessage
import dev.skymansandy.wiretap.data.db.room.entity.SocketLogEntity
import dev.skymansandy.wiretap.data.db.room.entity.SocketMessageEntity
import dev.skymansandy.wiretap.domain.model.SocketContentType
import dev.skymansandy.wiretap.domain.model.SocketMessageDirection
import dev.skymansandy.wiretap.domain.model.SocketStatus
import dev.skymansandy.wiretap.helper.util.HeadersSerializerUtil

internal fun SocketLogEntity.toDomain(): SocketEntry {
    return SocketEntry(
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
        direction = SocketMessageDirection.valueOf(direction),
        contentType = SocketContentType.valueOf(contentType),
        content = content,
        byteCount = byteCount,
        timestamp = timestamp,
    )
}
