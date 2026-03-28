package dev.skymansandy.wiretap.data.mappers

import dev.skymansandy.wiretap.data.db.room.entity.HttpLogEntity
import dev.skymansandy.wiretap.data.db.room.entity.HttpLogListProjection
import dev.skymansandy.wiretap.domain.model.HttpLog
import dev.skymansandy.wiretap.domain.model.ResponseSource
import dev.skymansandy.wiretap.domain.model.TimingPhase
import dev.skymansandy.wiretap.helper.util.HeadersSerializerUtil
import kotlinx.serialization.json.Json

private val timingJson = Json { ignoreUnknownKeys = true }

internal fun HttpLogEntity.toDomain(): HttpLog {
    return HttpLog(
        id = id,
        url = url,
        method = method,
        requestHeaders = HeadersSerializerUtil.deserialize(requestHeaders),
        requestBody = requestBody,
        responseCode = responseCode.toInt(),
        responseHeaders = HeadersSerializerUtil.deserialize(responseHeaders),
        responseBody = responseBody,
        responseBodySize = responseBody?.length?.toLong() ?: 0,
        durationMs = durationMs,
        source = ResponseSource.valueOf(source),
        timestamp = timestamp,
        matchedRuleId = matchedRuleId,
        protocol = protocol,
        remoteAddress = remoteAddress,
        tlsProtocol = tlsProtocol,
        cipherSuite = cipherSuite,
        certificateCn = certificateCn,
        issuerCn = issuerCn,
        certificateExpiry = certificateExpiry,
        timingPhases = timingPhases?.let {
            runCatching { timingJson.decodeFromString<List<TimingPhase>>(it) }.getOrDefault(emptyList())
        } ?: emptyList(),
    )
}

internal fun HttpLogListProjection.toDomain(): HttpLog {
    return HttpLog(
        id = id,
        url = url,
        method = method,
        requestHeaders = HeadersSerializerUtil.deserialize(requestHeaders),
        responseCode = responseCode.toInt(),
        responseHeaders = HeadersSerializerUtil.deserialize(responseHeaders),
        responseBodySize = responseBodySize,
        durationMs = durationMs,
        source = ResponseSource.valueOf(source),
        timestamp = timestamp,
        matchedRuleId = matchedRuleId,
        protocol = protocol,
        remoteAddress = remoteAddress,
        tlsProtocol = tlsProtocol,
        cipherSuite = cipherSuite,
        certificateCn = certificateCn,
        issuerCn = issuerCn,
        certificateExpiry = certificateExpiry,
    )
}

internal fun HttpLog.toRoomEntity(): HttpLogEntity {
    return HttpLogEntity(
        id = id,
        url = url,
        method = method,
        requestHeaders = HeadersSerializerUtil.serialize(requestHeaders),
        requestBody = requestBody,
        responseCode = responseCode.toLong(),
        responseHeaders = HeadersSerializerUtil.serialize(responseHeaders),
        responseBody = responseBody,
        durationMs = durationMs,
        source = source.name,
        timestamp = timestamp,
        matchedRuleId = matchedRuleId,
        protocol = protocol,
        remoteAddress = remoteAddress,
        tlsProtocol = tlsProtocol,
        cipherSuite = cipherSuite,
        certificateCn = certificateCn,
        issuerCn = issuerCn,
        certificateExpiry = certificateExpiry,
        timingPhases = timingPhases.takeIf { it.isNotEmpty() }?.let {
            timingJson.encodeToString(it)
        },
    )
}
