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
        urlMatcher = toUrlMatcher(),
        headerMatchers = headerMatchers?.let { HeaderMatcherSerializer.deserialize(it) } ?: emptyList(),
        bodyMatcher = toBodyMatcher(),
        action = toRuleAction(),
        enabled = enabled == 1L,
        createdAt = createdAt,
    )
}

private fun RuleEntity.toUrlMatcher(): UrlMatcher? {
    val type = urlMatcherType ?: return null
    val pattern = urlPattern ?: return null
    return when (MatcherType.valueOf(type)) {
        MatcherType.Exact -> UrlMatcher.Exact(pattern)
        MatcherType.Contains -> UrlMatcher.Contains(pattern)
        MatcherType.Regex -> UrlMatcher.Regex(pattern)
    }
}

private fun RuleEntity.toBodyMatcher(): BodyMatcher? {
    val type = bodyMatcherType ?: return null
    val pattern = bodyPattern ?: return null
    return when (MatcherType.valueOf(type)) {
        MatcherType.Exact -> BodyMatcher.Exact(pattern)
        MatcherType.Contains -> BodyMatcher.Contains(pattern)
        MatcherType.Regex -> BodyMatcher.Regex(pattern)
    }
}

private fun RuleEntity.toRuleAction(): RuleAction {
    return when (RuleAction.Type.valueOf(action)) {
        RuleAction.Type.Mock -> RuleAction.Mock(
            responseCode = mockResponseCode?.toInt() ?: 200,
            responseBody = mockResponseBody,
            responseHeaders = mockResponseHeaders?.let { HeadersSerializerUtil.deserialize(it) },
        )

        RuleAction.Type.Throttle -> RuleAction.Throttle(
            delayMs = throttleDelayMs ?: 0L,
            delayMaxMs = throttleDelayMaxMs,
        )

        RuleAction.Type.MockAndThrottle -> RuleAction.MockAndThrottle(
            responseCode = mockResponseCode?.toInt() ?: 200,
            responseBody = mockResponseBody,
            responseHeaders = mockResponseHeaders?.let { HeadersSerializerUtil.deserialize(it) },
            delayMs = throttleDelayMs ?: 1000L,
            delayMaxMs = throttleDelayMaxMs,
        )
    }
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
        mockResponseCode = when (action) {
            is RuleAction.Mock -> action.responseCode.toLong()
            is RuleAction.MockAndThrottle -> action.responseCode.toLong()
            is RuleAction.Throttle -> null
        },
        mockResponseBody = when (action) {
            is RuleAction.Mock -> action.responseBody
            is RuleAction.MockAndThrottle -> action.responseBody
            is RuleAction.Throttle -> null
        },
        mockResponseHeaders = when (action) {
            is RuleAction.Mock -> action.responseHeaders
                ?.let { HeadersSerializerUtil.serialize(it) }
            is RuleAction.MockAndThrottle -> action.responseHeaders
                ?.let { HeadersSerializerUtil.serialize(it) }
            is RuleAction.Throttle -> null
        },
        throttleDelayMs = when (action) {
            is RuleAction.Mock -> null
            is RuleAction.Throttle -> action.delayMs
            is RuleAction.MockAndThrottle -> action.delayMs
        },
        throttleDelayMaxMs = when (action) {
            is RuleAction.Mock -> null
            is RuleAction.Throttle -> action.delayMaxMs
            is RuleAction.MockAndThrottle -> action.delayMaxMs
        },
        enabled = if (enabled) 1L else 0L,
        createdAt = createdAt,
    )
}
