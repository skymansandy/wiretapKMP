/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.data.mappers

import dev.skymansandy.wiretap.data.db.room.entity.SseEventEntity
import dev.skymansandy.wiretap.data.db.room.entity.SseLogEntity
import dev.skymansandy.wiretap.domain.model.SseConnection
import dev.skymansandy.wiretap.domain.model.SseEvent
import dev.skymansandy.wiretap.domain.model.SseStatus
import dev.skymansandy.wiretap.helper.util.HeadersSerializerUtil

internal fun SseLogEntity.toDomain(): SseConnection {
    return SseConnection(
        id = id,
        url = url,
        requestHeaders = HeadersSerializerUtil.deserialize(requestHeaders),
        status = SseStatus.valueOf(status),
        failureMessage = failureMessage,
        eventCount = eventCount,
        timestamp = timestamp,
        closedAt = closedAt,
        lastEventId = lastEventId,
        retryMs = retryMs,
        historyCleared = historyCleared != 0L,
    )
}

internal fun SseEventEntity.toDomain(): SseEvent {
    return SseEvent(
        id = id,
        connectionId = connectionId,
        eventType = eventType,
        data = data,
        eventId = eventId,
        retryMs = retryMs,
        byteCount = byteCount,
        timestamp = timestamp,
    )
}

internal fun SseConnection.toRoomEntity(): SseLogEntity {
    return SseLogEntity(
        id = id,
        url = url,
        requestHeaders = HeadersSerializerUtil.serialize(requestHeaders),
        status = status.name,
        failureMessage = failureMessage,
        eventCount = eventCount,
        timestamp = timestamp,
        closedAt = closedAt,
        lastEventId = lastEventId,
        retryMs = retryMs,
        historyCleared = if (historyCleared) 1L else 0L,
    )
}
