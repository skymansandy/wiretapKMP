/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.util

expect fun currentTimeMillis(): Long

expect fun currentNanoTime(): Long

expect fun formatTime(timestampMs: Long): String
