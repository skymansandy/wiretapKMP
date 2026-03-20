package dev.skymansandy.wiretap.data.mappers

import dev.skymansandy.wiretap.data.db.entity.WiretapRule
import dev.skymansandy.wiretap.db.RuleEntity
import dev.skymansandy.wiretap.domain.model.BodyMatcher
import dev.skymansandy.wiretap.domain.model.MatcherType
import dev.skymansandy.wiretap.domain.model.RuleAction
import dev.skymansandy.wiretap.domain.model.UrlMatcher
import dev.skymansandy.wiretap.helper.util.HeaderMatcherSerializer
import dev.skymansandy.wiretap.helper.util.HeadersSerializerUtil

internal fun RuleEntity.toDomain(): WiretapRule {
    return WiretapRule(
        id = id,
        method = method,
        urlMatcher = url_matcher_type?.let { type ->
            url_pattern?.let { pattern ->
                when (MatcherType.valueOf(type)) {
                    MatcherType.Exact -> UrlMatcher.Exact(pattern)
                    MatcherType.Contains -> UrlMatcher.Contains(pattern)
                    MatcherType.Regex -> UrlMatcher.Regex(pattern)
                }
            }
        },
        headerMatchers = header_matchers?.let { HeaderMatcherSerializer.deserialize(it) } ?: emptyList(),
        bodyMatcher = body_matcher_type?.let { type ->
            body_pattern?.let { pattern ->
                when (MatcherType.valueOf(type)) {
                    MatcherType.Exact -> BodyMatcher.Exact(pattern)
                    MatcherType.Contains -> BodyMatcher.Contains(pattern)
                    MatcherType.Regex -> BodyMatcher.Regex(pattern)
                }
            }
        },
        action = when (RuleAction.Type.valueOf(action)) {
            RuleAction.Type.Mock -> RuleAction.Mock(
                responseCode = mock_response_code?.toInt() ?: 200,
                responseBody = mock_response_body,
                responseHeaders = mock_response_headers?.let { HeadersSerializerUtil.deserialize(it) },
                throttleDelayMs = throttle_delay_ms,
                throttleDelayMaxMs = throttle_delay_max_ms,
            )

            RuleAction.Type.Throttle -> RuleAction.Throttle(
                delayMs = throttle_delay_ms ?: 0L,
                delayMaxMs = throttle_delay_max_ms,
            )
        },
        enabled = enabled == 1L,
        createdAt = created_at,
    )
}
