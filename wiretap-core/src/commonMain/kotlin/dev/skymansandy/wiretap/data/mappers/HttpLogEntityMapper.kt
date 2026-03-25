package dev.skymansandy.wiretap.data.mappers

import dev.skymansandy.wiretap.data.db.entity.HttpLogEntry
import dev.skymansandy.wiretap.data.db.room.entity.HttpLogEntity
import dev.skymansandy.wiretap.data.db.room.entity.HttpLogListProjection
import dev.skymansandy.wiretap.domain.model.ResponseSource
import dev.skymansandy.wiretap.helper.util.HeadersSerializerUtil

internal fun HttpLogEntity.toDomain(): HttpLogEntry {
    return HttpLogEntry(
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
    )
}

internal fun HttpLogListProjection.toDomain(): HttpLogEntry {
    return HttpLogEntry(
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
