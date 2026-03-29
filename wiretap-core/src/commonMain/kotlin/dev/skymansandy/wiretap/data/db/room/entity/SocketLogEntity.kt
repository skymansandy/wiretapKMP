/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.data.db.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "SocketLogEntity",
    indices = [
        Index(
            value = ["timestamp"],
            name = "idx_socket_log_timestamp",
        ),
        Index(
            value = ["status"],
            name = "idx_socket_log_status",
        ),
    ],
)
internal data class SocketLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "url")
    val url: String,
    @ColumnInfo(name = "request_headers", defaultValue = "")
    val requestHeaders: String = "",
    @ColumnInfo(defaultValue = "Connecting")
    val status: String = "Connecting",
    @ColumnInfo(name = "close_code")
    val closeCode: Long? = null,
    @ColumnInfo(name = "close_reason")
    val closeReason: String? = null,
    @ColumnInfo(name = "failure_message")
    val failureMessage: String? = null,
    @ColumnInfo(name = "message_count", defaultValue = "0")
    val messageCount: Long = 0,
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
    @ColumnInfo(name = "closed_at")
    val closedAt: Long? = null,
    @ColumnInfo(name = "protocol")
    val protocol: String? = null,
    @ColumnInfo(name = "remote_address")
    val remoteAddress: String? = null,
    @ColumnInfo(name = "history_cleared", defaultValue = "0")
    val historyCleared: Long = 0,
)
