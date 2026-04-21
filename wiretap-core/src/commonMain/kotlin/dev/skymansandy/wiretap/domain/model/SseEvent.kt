/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.domain.model

data class SseEvent(
    val id: Long = 0,
    val connectionId: Long,
    val eventType: String? = null,
    val data: String,
    val eventId: String? = null,
    val retryMs: Long? = null,
    val byteCount: Long,
    val timestamp: Long,
)
