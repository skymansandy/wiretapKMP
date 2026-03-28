package dev.skymansandy.wiretap.data.mappers

import dev.skymansandy.wiretap.data.db.room.entity.RuleEntity
import dev.skymansandy.wiretap.domain.model.MatcherType
import dev.skymansandy.wiretap.domain.model.RuleAction
import dev.skymansandy.wiretap.domain.model.WiretapRule
import dev.skymansandy.wiretap.domain.model.matchers.BodyMatcher
import dev.skymansandy.wiretap.domain.model.matchers.UrlMatcher
import dev.skymansandy.wiretap.helper.util.HeaderMatcherSerializer
import dev.skymansandy.wiretap.helper.util.HeadersSerializerUtil

internal fun RuleEntity.toDomain(): WiretapRule {
    return WiretapRule(
        id = id,
        method = method,
        urlMatcher = urlMatcherType?.let { type ->
            urlPattern?.let { pattern ->
                when (MatcherType.valueOf(type)) {
                    MatcherType.Exact -> UrlMatcher.Exact(pattern)
                    MatcherType.Contains -> UrlMatcher.Contains(pattern)
                    MatcherType.Regex -> UrlMatcher.Regex(pattern)
                }
            }
        },
        headerMatchers = headerMatchers?.let { HeaderMatcherSerializer.deserialize(it) } ?: emptyList(),
        bodyMatcher = bodyMatcherType?.let { type ->
            bodyPattern?.let { pattern ->
                when (MatcherType.valueOf(type)) {
                    MatcherType.Exact -> BodyMatcher.Exact(pattern)
                    MatcherType.Contains -> BodyMatcher.Contains(pattern)
                    MatcherType.Regex -> BodyMatcher.Regex(pattern)
                }
            }
        },
        action = when (RuleAction.Type.valueOf(action)) {
            RuleAction.Type.Mock -> RuleAction.Mock(
                responseCode = mockResponseCode?.toInt() ?: 200,
                responseBody = mockResponseBody,
                responseHeaders = mockResponseHeaders?.let { HeadersSerializerUtil.deserialize(it) },
                throttleDelayMs = throttleDelayMs,
                throttleDelayMaxMs = throttleDelayMaxMs,
            )

            RuleAction.Type.Throttle -> RuleAction.Throttle(
                delayMs = throttleDelayMs ?: 0L,
                delayMaxMs = throttleDelayMaxMs,
            )
        },
        enabled = enabled == 1L,
        createdAt = createdAt,
    )
}

internal fun WiretapRule.toRoomEntity(): RuleEntity {
    return RuleEntity(
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
}
