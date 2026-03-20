package dev.skymansandy.wiretap.data.mappers

import dev.skymansandy.wiretap.data.db.entity.HttpLogEntry
import dev.skymansandy.wiretap.db.HttpLogEntity
import dev.skymansandy.wiretap.domain.model.ResponseSource
import dev.skymansandy.wiretap.helper.util.HeadersSerializerUtil

internal fun HttpLogEntity.toDomain(): HttpLogEntry {
    return HttpLogEntry(
        id = id,
        url = url,
        method = method,
        requestHeaders = HeadersSerializerUtil.deserialize(request_headers),
        requestBody = request_body,
        responseCode = response_code.toInt(),
        responseHeaders = HeadersSerializerUtil.deserialize(response_headers),
        responseBody = response_body,
        durationMs = duration_ms,
        source = ResponseSource.valueOf(source),
        timestamp = timestamp,
        matchedRuleId = matched_rule_id,
        protocol = protocol,
        remoteAddress = remote_address,
        tlsProtocol = tls_protocol,
        cipherSuite = cipher_suite,
        certificateCn = certificate_cn,
        issuerCn = issuer_cn,
        certificateExpiry = certificate_expiry,
    )
}
