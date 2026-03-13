package dev.skymansandy.wiretap.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

actual fun currentTimeMillis(): Long = System.currentTimeMillis()

actual fun currentNanoTime(): Long = System.nanoTime()

actual fun formatTime(timestampMs: Long): String =
    SimpleDateFormat("h:mm:ss.SSS a", Locale.getDefault()).format(Date(timestampMs)).lowercase()
