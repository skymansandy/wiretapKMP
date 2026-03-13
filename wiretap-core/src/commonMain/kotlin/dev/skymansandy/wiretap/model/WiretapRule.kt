package dev.skymansandy.wiretap.model

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
