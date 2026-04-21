/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.data.db.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.skymansandy.wiretap.data.db.room.entity.SseEventEntity
import dev.skymansandy.wiretap.data.db.room.entity.SseLogEntity
import kotlinx.coroutines.flow.Flow

@Suppress("LongParameterList")
@Dao
internal interface SseLogsDao {

    @Insert
    suspend fun insertSseLog(entity: SseLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSseLogWithId(entity: SseLogEntity)

    @Query(
        """
        UPDATE SseLogEntity SET
            status = :status,
            failure_message = :failureMessage,
            closed_at = :closedAt,
            last_event_id = :lastEventId,
            retry_ms = :retryMs
        WHERE id = :id
        """,
    )
    suspend fun updateSseLog(
        status: String,
        failureMessage: String?,
        closedAt: Long?,
        lastEventId: String?,
        retryMs: Long?,
        id: Long,
    )

    @Query("SELECT * FROM SseLogEntity WHERE id = :id")
    suspend fun getSseLogById(id: Long): SseLogEntity?

    @Query("SELECT * FROM SseLogEntity ORDER BY timestamp DESC")
    fun getAllSseLogs(): Flow<List<SseLogEntity>>

    @Query("DELETE FROM SseLogEntity")
    suspend fun deleteAllSseLogs()

    @Query("DELETE FROM SseLogEntity WHERE status IN ('Closed', 'Failed')")
    suspend fun deleteClosedSseLogs()

    @Query("UPDATE SseLogEntity SET status = 'Closed' WHERE status NOT IN ('Closed', 'Failed')")
    suspend fun closeStaleSseLogs()

    @Query(
        """
        SELECT * FROM SseLogEntity
        WHERE (url LIKE '%' || :query || '%'
           OR status LIKE '%' || :query || '%')
        AND (:afterId IS NULL OR id < :afterId)
        ORDER BY id DESC
        LIMIT :limit
        """,
    )
    suspend fun getSseLogsPage(query: String, afterId: Long?, limit: Long): List<SseLogEntity>

    @Query("UPDATE SseLogEntity SET event_count = event_count + 1 WHERE id = :id")
    suspend fun incrementSseEventCount(id: Long)

    @Insert
    suspend fun insertSseEvent(entity: SseEventEntity)

    @Query("SELECT * FROM SseEventEntity WHERE connection_id = :connectionId ORDER BY timestamp ASC")
    fun getSseEventsByConnectionId(connectionId: Long): Flow<List<SseEventEntity>>

    @Query("DELETE FROM SseEventEntity WHERE connection_id IN (SELECT id FROM SseLogEntity WHERE status IN ('Closed', 'Failed'))")
    suspend fun deleteClosedSseEvents()

    @Query("DELETE FROM SseEventEntity")
    suspend fun deleteAllSseEvents()
}
