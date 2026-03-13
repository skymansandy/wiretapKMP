package dev.skymansandy.wiretap.util

expect fun currentTimeMillis(): Long

expect fun currentNanoTime(): Long

expect fun formatTime(timestampMs: Long): String
