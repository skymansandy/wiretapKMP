/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.ui.model

internal enum class UrlMatchMode(val label: String) {

    Exact("Exact"),
    Contains("Contains"),
    Regex("Regex"),
}

internal enum class BodyMatchMode(val label: String) {

    Exact("Exact"),
    Contains("Contains"),
    Regex("Regex"),
}

internal enum class HeaderEntryMode(val label: String) {

    KeyExists("Key Exists"),
    ValueExact("Exact"),
    ValueContains("Contains"),
    ValueRegex("Regex"),
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
    val delayMinMs: Long,
    val delayMaxMs: Long,
) {

    Gprs("2G (GPRS)", 1500, 3000),
    Edge("2G (EDGE)", 800, 2000),
    Slow3g("3G (Slow)", 500, 1500),
    Fast3g("3G", 300, 800),
    Slow4g("4G (Slow)", 150, 400),
    Lte("4G (LTE)", 50, 200),
    SlowWifi("Slow WiFi", 500, 1000),
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
