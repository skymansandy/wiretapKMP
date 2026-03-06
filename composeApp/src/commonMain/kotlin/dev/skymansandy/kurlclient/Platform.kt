package dev.skymansandy.kurlclient

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun currentTimeMillis(): Long

fun formatRelativeTime(ms: Long): String {
    if (ms == 0L) return "–"
    val diff = currentTimeMillis() - ms
    return when {
        diff < 60_000L -> "just now"
        diff < 3_600_000L -> "${diff / 60_000} min ago"
        diff < 86_400_000L -> "${diff / 3_600_000} hr ago"
        diff < 2 * 86_400_000L -> "yesterday"
        else -> "${diff / 86_400_000} days ago"
    }
}