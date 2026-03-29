/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.data.db.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import dev.skymansandy.wiretap.data.db.room.entity.RuleEntity
import kotlinx.coroutines.flow.Flow

@Suppress("LongParameterList")
@Dao
internal interface RulesDao {

    @Insert
    suspend fun insert(entity: RuleEntity)

    @Query(
        """
        UPDATE RuleEntity SET
            method = :method,
            url_matcher_type = :urlMatcherType,
            url_pattern = :urlPattern,
            header_matchers = :headerMatchers,
            body_matcher_type = :bodyMatcherType,
            body_pattern = :bodyPattern,
            `action` = :action,
            mock_response_code = :mockResponseCode,
            mock_response_body = :mockResponseBody,
            mock_response_headers = :mockResponseHeaders,
            throttle_delay_ms = :throttleDelayMs,
            throttle_delay_max_ms = :throttleDelayMaxMs,
            enabled = :enabled
        WHERE id = :id
        """,
    )
    suspend fun update(
        method: String,
        urlMatcherType: String?,
        urlPattern: String?,
        headerMatchers: String?,
        bodyMatcherType: String?,
        bodyPattern: String?,
        action: String,
        mockResponseCode: Long?,
        mockResponseBody: String?,
        mockResponseHeaders: String?,
        throttleDelayMs: Long?,
        throttleDelayMaxMs: Long?,
        enabled: Long,
        id: Long,
    )

    @Query("SELECT * FROM RuleEntity ORDER BY created_at DESC")
    fun getAll(): Flow<List<RuleEntity>>

    @Query("SELECT * FROM RuleEntity WHERE id = :id")
    suspend fun getById(id: Long): RuleEntity?

    @Query("SELECT * FROM RuleEntity WHERE id = :id")
    fun flowById(id: Long): Flow<RuleEntity?>

    @Query("SELECT * FROM RuleEntity WHERE enabled = 1 ORDER BY created_at DESC")
    suspend fun getEnabledRules(): List<RuleEntity>

    @Query("UPDATE RuleEntity SET enabled = :enabled WHERE id = :id")
    suspend fun updateEnabled(enabled: Long, id: Long)

    @Query(
        """
        SELECT * FROM RuleEntity
        WHERE (url_pattern LIKE '%' || :query || '%'
           OR body_pattern LIKE '%' || :query || '%'
           OR header_matchers LIKE '%' || :query || '%'
           OR method LIKE '%' || :query || '%'
           OR `action` LIKE '%' || :query || '%')
        ORDER BY created_at DESC
        """,
    )
    fun search(query: String): Flow<List<RuleEntity>>

    @Query("DELETE FROM RuleEntity WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM RuleEntity")
    suspend fun deleteAll()
}
