package dev.skymansandy.wiretap.ui.rules.model

import dev.skymansandy.wiretap.data.db.entity.WiretapRule
import dev.skymansandy.wiretap.domain.model.BodyMatcher
import dev.skymansandy.wiretap.domain.model.HeaderMatcher
import dev.skymansandy.wiretap.domain.model.UrlMatcher

internal enum class UrlMatchMode {
    Exact,
    Contains,
    Regex,
}

internal enum class BodyMatchMode {
    Exact,
    Contains,
    Regex,
}

internal enum class HeaderEntryMode {
    KeyExists,
    ValueExact,
    ValueContains,
    ValueRegex,
}

internal enum class ResponseHeadersEditMode {
    KeyValue,
    BulkEdit,
}

internal enum class ThrottleInputMode {
    None,
    Manual,
    Profile,
}

internal enum class ThrottleProfile(
    val label: String,
    val speed: String,
    val delayMinMs: Long,
    val delayMaxMs: Long,
) {
    Gprs("2G (GPRS)", "~50 kbps", 1500, 3000),
    Edge("2G (EDGE)", "~200 kbps", 800, 2000),
    Slow3g("3G (Slow)", "~400 kbps", 500, 1500),
    Fast3g("3G", "~2 Mbps", 300, 800),
    Slow4g("4G (Slow)", "~5 Mbps", 150, 400),
    Lte("4G (LTE)", "~20 Mbps", 50, 200),
    SlowWifi("Slow WiFi", "~1 Mbps", 500, 1000),
}

internal data class HeaderEntry(
    val key: String = "",
    val value: String = "",
    val mode: HeaderEntryMode = HeaderEntryMode.KeyExists,
)

internal data class ResponseHeaderEntry(
    val key: String = "",
    val value: String = "",
)

internal data class RegexTestResult(
    val matches: Boolean,
    val error: String?,
)
