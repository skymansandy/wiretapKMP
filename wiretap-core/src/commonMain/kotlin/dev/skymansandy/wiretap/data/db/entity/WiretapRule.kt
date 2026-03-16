package dev.skymansandy.wiretap.data.db.entity

import dev.skymansandy.wiretap.domain.model.BodyMatcher
import dev.skymansandy.wiretap.domain.model.HeaderMatcher
import dev.skymansandy.wiretap.domain.model.RuleAction
import dev.skymansandy.wiretap.domain.model.UrlMatcher

data class WiretapRule(
    val id: Long = 0,
    val method: String = "*",
    val urlMatcher: UrlMatcher? = null,
    val headerMatchers: List<HeaderMatcher> = emptyList(),
    val bodyMatcher: BodyMatcher? = null,
    val action: RuleAction,
    val mockResponseCode: Int? = null,
    val mockResponseBody: String? = null,
    val mockResponseHeaders: Map<String, String>? = null,
    val throttleDelayMs: Long? = null,
    val throttleDelayMaxMs: Long? = null,
    val enabled: Boolean = true,
    val createdAt: Long = 0,
)
