package dev.skymansandy.wiretap.data.db.entity

import dev.skymansandy.wiretap.domain.model.MatcherType
import dev.skymansandy.wiretap.domain.model.RuleAction

data class WiretapRule(
    val id: Long = 0,
    val matcherType: MatcherType = MatcherType.URL_EXACT,
    val urlPattern: String,
    val method: String = "*",
    val action: RuleAction,
    val mockResponseCode: Int? = null,
    val mockResponseBody: String? = null,
    val mockResponseHeaders: Map<String, String>? = null,
    val throttleDelayMs: Long? = null,
    val enabled: Boolean = true,
    val createdAt: Long = 0,
)
