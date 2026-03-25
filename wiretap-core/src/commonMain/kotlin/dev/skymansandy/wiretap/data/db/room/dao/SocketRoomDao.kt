package dev.skymansandy.wiretap.data.db.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.skymansandy.wiretap.data.db.room.entity.SocketLogEntity
import dev.skymansandy.wiretap.data.db.room.entity.SocketMessageEntity
import kotlinx.coroutines.flow.Flow

@Suppress("LongParameterList")
@Dao
internal interface SocketRoomDao {

    @Insert
    suspend fun insertSocketLog(entity: SocketLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSocketLogWithId(entity: SocketLogEntity)

    @Query(
        """
        UPDATE SocketLogEntity SET
            status = :status,
            close_code = :closeCode,
            close_reason = :closeReason,
            failure_message = :failureMessage,
            closed_at = :closedAt,
            protocol = :protocol,
            remote_address = :remoteAddress
        WHERE id = :id
        """,
    )
    suspend fun updateSocketLog(
        status: String,
        closeCode: Long?,
        closeReason: String?,
        failureMessage: String?,
        closedAt: Long?,
        protocol: String?,
        remoteAddress: String?,
        id: Long,
    )

    @Query("SELECT * FROM SocketLogEntity WHERE id = :id")
    suspend fun getSocketLogById(id: Long): SocketLogEntity?

    @Query("SELECT * FROM SocketLogEntity ORDER BY timestamp DESC")
    fun getAllSocketLogs(): Flow<List<SocketLogEntity>>

    @Query("DELETE FROM SocketLogEntity")
    suspend fun deleteAllSocketLogs()

    @Query("DELETE FROM SocketLogEntity WHERE status IN ('Closed', 'Failed')")
    suspend fun deleteClosedSocketLogs()

    @Query("UPDATE SocketLogEntity SET status = 'Closed' WHERE status NOT IN ('Closed', 'Failed')")
    suspend fun closeStaleSocketLogs()

    @Query(
        """
        SELECT * FROM SocketLogEntity
        WHERE (url LIKE '%' || :query || '%'
           OR status LIKE '%' || :query || '%')
        AND (:afterId IS NULL OR id < :afterId)
        ORDER BY id DESC
        LIMIT :limit
        """,
    )
    suspend fun getSocketLogsPage(query: String, afterId: Long?, limit: Long): List<SocketLogEntity>

    @Query("UPDATE SocketLogEntity SET message_count = message_count + 1 WHERE id = :id")
    suspend fun incrementSocketMessageCount(id: Long)

    @Insert
    suspend fun insertSocketMessage(entity: SocketMessageEntity)

    @Query("SELECT * FROM SocketMessageEntity WHERE socket_id = :socketId ORDER BY timestamp ASC")
    fun getSocketMessagesBySocketId(socketId: Long): Flow<List<SocketMessageEntity>>

    @Query("DELETE FROM SocketMessageEntity WHERE socket_id IN (SELECT id FROM SocketLogEntity WHERE status IN ('Closed', 'Failed'))")
    suspend fun deleteClosedSocketMessages()

    @Query("DELETE FROM SocketMessageEntity")
    suspend fun deleteAllSocketMessages()
}
