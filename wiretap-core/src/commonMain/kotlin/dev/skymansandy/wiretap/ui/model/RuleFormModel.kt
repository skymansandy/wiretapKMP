package dev.skymansandy.wiretap.ui.model

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
    val delayMinMs: Long,
    val delayMaxMs: Long,
) {
    Gprs(1500, 3000),
    Edge(800, 2000),
    Slow3g(500, 1500),
    Fast3g(300, 800),
    Slow4g(150, 400),
    Lte(50, 200),
    SlowWifi(500, 1000),
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
