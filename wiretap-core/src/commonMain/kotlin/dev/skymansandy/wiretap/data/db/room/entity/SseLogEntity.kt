/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.data.db.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "SseLogEntity",
    indices = [
        Index(
            value = ["timestamp"],
            name = "idx_sse_log_timestamp",
        ),
        Index(
            value = ["status"],
            name = "idx_sse_log_status",
        ),
    ],
)
internal data class SseLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "url")
    val url: String,
    @ColumnInfo(name = "request_headers", defaultValue = "")
    val requestHeaders: String = "",
    @ColumnInfo(defaultValue = "Connecting")
    val status: String = "Connecting",
    @ColumnInfo(name = "failure_message")
    val failureMessage: String? = null,
    @ColumnInfo(name = "event_count", defaultValue = "0")
    val eventCount: Long = 0,
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
    @ColumnInfo(name = "closed_at")
    val closedAt: Long? = null,
    @ColumnInfo(name = "last_event_id")
    val lastEventId: String? = null,
    @ColumnInfo(name = "retry_ms")
    val retryMs: Long? = null,
    @ColumnInfo(name = "history_cleared", defaultValue = "0")
    val historyCleared: Long = 0,
)
