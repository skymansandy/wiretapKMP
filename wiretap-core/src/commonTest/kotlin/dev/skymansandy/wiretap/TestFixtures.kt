package dev.skymansandy.wiretap

import dev.skymansandy.wiretap.data.db.entity.HttpLogEntry
import dev.skymansandy.wiretap.data.db.entity.SocketLogEntry
import dev.skymansandy.wiretap.data.db.entity.SocketMessage
import dev.skymansandy.wiretap.data.db.entity.WiretapRule
import dev.skymansandy.wiretap.domain.model.BodyMatcher
import dev.skymansandy.wiretap.domain.model.HeaderMatcher
import dev.skymansandy.wiretap.domain.model.ResponseSource
import dev.skymansandy.wiretap.domain.model.RuleAction
import dev.skymansandy.wiretap.domain.model.SocketContentType
import dev.skymansandy.wiretap.domain.model.SocketMessageDirection
import dev.skymansandy.wiretap.domain.model.SocketStatus
import dev.skymansandy.wiretap.domain.model.UrlMatcher

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
) = SocketLogEntry(
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
