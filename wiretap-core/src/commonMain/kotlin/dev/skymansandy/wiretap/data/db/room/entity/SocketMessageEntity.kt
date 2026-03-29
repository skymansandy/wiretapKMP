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
    tableName = "SocketMessageEntity",
    indices = [
        Index(
            value = ["socket_id"],
        ),
    ],
    foreignKeys = [
        ForeignKey(
            entity = SocketLogEntity::class,
            parentColumns = ["id"],
            childColumns = ["socket_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
internal data class SocketMessageEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "socket_id")
    val socketId: Long,
    @ColumnInfo(name = "direction")
    val direction: String,
    @ColumnInfo(name = "content_type")
    val contentType: String,
    @ColumnInfo(name = "content")
    val content: String,
    @ColumnInfo(name = "byte_count")
    val byteCount: Long,
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
)
