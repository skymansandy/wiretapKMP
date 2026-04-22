/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.data.db.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "SseEventEntity",
    indices = [
        Index(
            value = ["connection_id"],
        ),
    ],
    foreignKeys = [
        ForeignKey(
            entity = SseLogEntity::class,
            parentColumns = ["id"],
            childColumns = ["connection_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
internal data class SseEventEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "connection_id")
    val connectionId: Long,
    @ColumnInfo(name = "event_type")
    val eventType: String? = null,
    @ColumnInfo(name = "data")
    val data: String,
    @ColumnInfo(name = "event_id")
    val eventId: String? = null,
    @ColumnInfo(name = "retry_ms")
    val retryMs: Long? = null,
    @ColumnInfo(name = "byte_count")
    val byteCount: Long,
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
)
