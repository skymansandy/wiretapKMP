package dev.skymansandy.wiretap.ui.rules.model

import dev.skymansandy.wiretap.data.db.entity.WiretapRule
import dev.skymansandy.wiretap.domain.model.BodyMatcher
import dev.skymansandy.wiretap.domain.model.HeaderMatcher
import dev.skymansandy.wiretap.domain.model.UrlMatcher

internal enum class UrlMatchMode {
    EXACT,
    CONTAINS,
    REGEX,
}

internal enum class BodyMatchMode {
    EXACT,
    CONTAINS,
    REGEX,
}

internal enum class HeaderEntryMode {
    KEY_EXISTS,
    VALUE_EXACT,
    VALUE_CONTAINS,
    VALUE_REGEX,
}

internal enum class ResponseHeadersEditMode {
    KEY_VALUE,
    BULK_EDIT,
}

internal enum class ThrottleInputMode {
    NONE,
    MANUAL,
    PROFILE,
}

internal enum class ThrottleProfile(
    val label: String,
    val speed: String,
    val delayMinMs: Long,
    val delayMaxMs: Long,
) {
    GPRS("2G (GPRS)", "~50 kbps", 1500, 3000),
    EDGE("2G (EDGE)", "~200 kbps", 800, 2000),
    SLOW_3G("3G (Slow)", "~400 kbps", 500, 1500),
    FAST_3G("3G", "~2 Mbps", 300, 800),
    SLOW_4G("4G (Slow)", "~5 Mbps", 150, 400),
    LTE("4G (LTE)", "~20 Mbps", 50, 200),
    SLOW_WIFI("Slow WiFi", "~1 Mbps", 500, 1000),
}

internal data class HeaderEntry(
    val key: String = "",
    val value: String = "",
    val mode: HeaderEntryMode = HeaderEntryMode.KEY_EXISTS,
)

internal data class ResponseHeaderEntry(
    val key: String = "",
    val value: String = "",
)

internal data class RegexTestResult(
    val matches: Boolean,
    val error: String?,
)
