package dev.skymansandy.wiretap.helper.util

import dev.skymansandy.wiretap.helper.constants.TIME_FORMAT
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

actual fun currentTimeMillis(): Long = System.currentTimeMillis()

actual fun currentNanoTime(): Long = System.nanoTime()

actual fun formatTime(timestampMs: Long): String =
    SimpleDateFormat(TIME_FORMAT, Locale.getDefault()).format(Date(timestampMs)).lowercase()
