package dev.skymansandy.wiretap

import dev.skymansandy.wiretap.data.db.entity.HttpLogEntry
import dev.skymansandy.wiretap.data.db.entity.SocketEntry
import dev.skymansandy.wiretap.data.db.entity.SocketMessage
import dev.skymansandy.wiretap.data.db.entity.WiretapRule
import dev.skymansandy.wiretap.data.db.room.entity.HttpLogEntity
import dev.skymansandy.wiretap.data.db.room.entity.HttpLogListProjection
import dev.skymansandy.wiretap.data.db.room.entity.RuleEntity
import dev.skymansandy.wiretap.data.db.room.entity.SocketLogEntity
import dev.skymansandy.wiretap.data.db.room.entity.SocketMessageEntity
import dev.skymansandy.wiretap.domain.model.BodyMatcher
import dev.skymansandy.wiretap.domain.model.HeaderMatcher
import dev.skymansandy.wiretap.domain.model.ResponseSource
import dev.skymansandy.wiretap.domain.model.RuleAction
import dev.skymansandy.wiretap.domain.model.SocketContentType
import dev.skymansandy.wiretap.domain.model.SocketMessageDirection
import dev.skymansandy.wiretap.domain.model.SocketStatus
import dev.skymansandy.wiretap.domain.model.UrlMatcher
import dev.skymansandy.wiretap.helper.util.HeaderMatcherSerializer
import dev.skymansandy.wiretap.helper.util.HeadersSerializerUtil

@Suppress("LongParameterList")
internal fun httpLogEntry(
    id: Long = 0,
    url: String = "https://api.example.com/users",
    method: String = "GET",
    requestHeaders: Map<String, String> = emptyMap(),
    requestBody: String? = null,
    responseCode: Int = 200,
    responseHeaders: Map<String, String> = emptyMap(),
    responseBody: String? = null,
    durationMs: Long = 100,
    durationNs: Long = 0,
    source: ResponseSource = ResponseSource.Network,
    timestamp: Long = 1000L,
    matchedRuleId: Long? = null,
    protocol: String? = null,
    remoteAddress: String? = null,
    tlsProtocol: String? = null,
    cipherSuite: String? = null,
    certificateCn: String? = null,
    issuerCn: String? = null,
    certificateExpiry: String? = null,
) = HttpLogEntry(
    id = id,
    url = url,
    method = method,
    requestHeaders = requestHeaders,
    requestBody = requestBody,
    responseCode = responseCode,
    responseHeaders = responseHeaders,
    responseBody = responseBody,
    durationMs = durationMs,
    durationNs = durationNs,
    source = source,
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

@Suppress("LongParameterList")
internal fun wiretapRule(
    id: Long = 1,
    method: String = "*",
    urlMatcher: UrlMatcher? = null,
    headerMatchers: List<HeaderMatcher> = emptyList(),
    bodyMatcher: BodyMatcher? = null,
    action: RuleAction = RuleAction.Mock(responseCode = 200),
    enabled: Boolean = true,
    createdAt: Long = 0,
) = WiretapRule(
    id = id,
    method = method,
    urlMatcher = urlMatcher,
    headerMatchers = headerMatchers,
    bodyMatcher = bodyMatcher,
    action = action,
    enabled = enabled,
    createdAt = createdAt,
)

@Suppress("LongParameterList")
internal fun socketLogEntry(
    id: Long = 0,
    url: String = "wss://api.example.com/ws",
    requestHeaders: Map<String, String> = emptyMap(),
    status: SocketStatus = SocketStatus.Open,
    closeCode: Int? = null,
    closeReason: String? = null,
    failureMessage: String? = null,
    messageCount: Long = 0,
    timestamp: Long = 1000L,
    closedAt: Long? = null,
    protocol: String? = null,
    remoteAddress: String? = null,
    historyCleared: Boolean = false,
) = SocketEntry(
    id = id,
    url = url,
    requestHeaders = requestHeaders,
    status = status,
    closeCode = closeCode,
    closeReason = closeReason,
    failureMessage = failureMessage,
    messageCount = messageCount,
    timestamp = timestamp,
    closedAt = closedAt,
    protocol = protocol,
    remoteAddress = remoteAddress,
    historyCleared = historyCleared,
)

internal fun socketMessage(
    id: Long = 0,
    socketId: Long = 1,
    direction: SocketMessageDirection = SocketMessageDirection.Received,
    contentType: SocketContentType = SocketContentType.Text,
    content: String = """{"hello":"world"}""",
    byteCount: Long = 17,
    timestamp: Long = 1000L,
) = SocketMessage(
    id = id,
    socketId = socketId,
    direction = direction,
    contentType = contentType,
    content = content,
    byteCount = byteCount,
    timestamp = timestamp,
)

// Room entity conversion helpers for tests

internal fun HttpLogEntry.toRoomEntity() = HttpLogEntity(
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
)

internal fun HttpLogEntry.toHttpLogListProjection() = HttpLogListProjection(
    id = id,
    url = url,
    method = method,
    requestHeaders = HeadersSerializerUtil.serialize(requestHeaders),
    responseCode = responseCode.toLong(),
    responseHeaders = HeadersSerializerUtil.serialize(responseHeaders),
    responseBodySize = responseBody?.length?.toLong() ?: 0,
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
)

internal fun WiretapRule.toRoomEntity() = RuleEntity(
    id = id,
    method = method,
    urlMatcherType = urlMatcher?.type?.name,
    urlPattern = urlMatcher?.pattern,
    headerMatchers = headerMatchers.takeIf { it.isNotEmpty() }
        ?.let { HeaderMatcherSerializer.serialize(it) },
    bodyMatcherType = bodyMatcher?.type?.name,
    bodyPattern = bodyMatcher?.pattern,
    action = action.name,
    mockResponseCode = (action as? RuleAction.Mock)?.responseCode?.toLong(),
    mockResponseBody = (action as? RuleAction.Mock)?.responseBody,
    mockResponseHeaders = (action as? RuleAction.Mock)?.responseHeaders
        ?.let { HeadersSerializerUtil.serialize(it) },
    throttleDelayMs = when (action) {
        is RuleAction.Mock -> action.throttleDelayMs
        is RuleAction.Throttle -> action.delayMs
    },
    throttleDelayMaxMs = when (action) {
        is RuleAction.Mock -> action.throttleDelayMaxMs
        is RuleAction.Throttle -> action.delayMaxMs
    },
    enabled = if (enabled) 1L else 0L,
    createdAt = createdAt,
)

internal fun SocketEntry.toRoomEntity() = SocketLogEntity(
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

internal fun SocketMessage.toRoomEntity() = SocketMessageEntity(
    id = id,
    socketId = socketId,
    direction = direction.name,
    contentType = contentType.name,
    content = content,
    byteCount = byteCount,
    timestamp = timestamp,
)
