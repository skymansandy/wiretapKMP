package dev.skymansandy.wiretap.util

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.timeIntervalSince1970

actual fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()

actual fun currentNanoTime(): Long =
    (platform.Foundation.NSProcessInfo.processInfo.systemUptime * 1_000_000_000.0).toLong()

actual fun formatTime(timestampMs: Long): String {
    val formatter = NSDateFormatter()
    formatter.dateFormat = "h:mm:ss.SSS a"
    formatter.locale = NSLocale.currentLocale
    val date = NSDate.dateWithTimeIntervalSince1970(timestampMs / 1000.0)
    return formatter.stringFromDate(date).lowercase()
}
