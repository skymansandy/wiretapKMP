/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.util

import dev.skymansandy.wiretap.helper.constants.TIME_FORMAT
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.NSProcessInfo
import platform.Foundation.currentLocale
import platform.Foundation.timeIntervalSince1970

actual fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()

actual fun currentNanoTime(): Long =
    (NSProcessInfo.processInfo.systemUptime * 1_000_000_000.0).toLong()

actual fun formatTime(timestampMs: Long): String {
    val formatter = NSDateFormatter()
    formatter.dateFormat = TIME_FORMAT
    formatter.locale = NSLocale.currentLocale
    // NSDate reference date is 2001-01-01, Unix epoch is 1970-01-01; difference is 978307200 seconds
    val date = NSDate(timeIntervalSinceReferenceDate = (timestampMs / 1000.0) - 978307200.0)
    return formatter.stringFromDate(date).lowercase()
}
