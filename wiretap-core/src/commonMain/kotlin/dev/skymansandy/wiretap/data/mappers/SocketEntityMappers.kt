package dev.skymansandy.wiretap.data.mappers

import dev.skymansandy.wiretap.data.db.entity.SocketLogEntry
import dev.skymansandy.wiretap.data.db.entity.SocketMessage
import dev.skymansandy.wiretap.db.SocketLogEntity
import dev.skymansandy.wiretap.db.SocketMessageEntity
import dev.skymansandy.wiretap.domain.model.SocketContentType
import dev.skymansandy.wiretap.domain.model.SocketMessageDirection
import dev.skymansandy.wiretap.domain.model.SocketStatus
import dev.skymansandy.wiretap.helper.util.HeadersSerializerUtil

internal fun SocketLogEntity.toDomain(): SocketLogEntry {
    return SocketLogEntry(
        id = id,
        url = url,
        requestHeaders = HeadersSerializerUtil.deserialize(request_headers),
        status = SocketStatus.valueOf(status),
        closeCode = close_code?.toInt(),
        closeReason = close_reason,
        failureMessage = failure_message,
        messageCount = message_count,
        timestamp = timestamp,
        closedAt = closed_at,
        protocol = protocol,
        remoteAddress = remote_address,
        historyCleared = history_cleared != 0L,
    )
}

internal fun SocketMessageEntity.toDomain(): SocketMessage {
    return SocketMessage(
        id = id,
        socketId = socket_id,
        direction = SocketMessageDirection.valueOf(direction),
        contentType = SocketContentType.valueOf(content_type),
        content = content,
        byteCount = byte_count,
        timestamp = timestamp,
    )
}
