package dev.skymansandy.wiretap.util

import kotlin.math.abs

internal fun formatOneDecimal(value: Float): String {
    val intPart = value.toLong()
    val decPart = ((value - intPart) * 10).toInt().let { abs(it) }
    return "$intPart.$decPart"
}

internal fun formatSize(bytes: Long?): String {
    if (bytes == null || bytes == 0L) return "0 B"
    return when {
        bytes >= 1_048_576 -> "${formatOneDecimal(bytes / 1_048_576f)} MB"
        bytes >= 1_024 -> "${formatOneDecimal(bytes / 1_024f)} kB"
        else -> "$bytes B"
    }
}

internal fun formatBytes(bytes: Long): String = when {
    bytes >= 1_048_576 -> "${bytes / 1_048_576} MB"
    bytes >= 1_024 -> "${bytes / 1_024} kB"
    else -> "$bytes B"
}
