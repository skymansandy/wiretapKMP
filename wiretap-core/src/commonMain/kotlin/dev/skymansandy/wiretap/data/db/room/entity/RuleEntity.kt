package dev.skymansandy.wiretap.data.db.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "RuleEntity",
    indices = [
        Index(
            value = ["enabled"],
            name = "idx_rule_enabled",
        ),
    ],
)
internal data class RuleEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "method", defaultValue = "*")
    val method: String = "*",
    @ColumnInfo(name = "url_matcher_type")
    val urlMatcherType: String? = null,
    @ColumnInfo(name = "url_pattern")
    val urlPattern: String? = null,
    @ColumnInfo(name = "header_matchers")
    val headerMatchers: String? = null,
    @ColumnInfo(name = "body_matcher_type")
    val bodyMatcherType: String? = null,
    @ColumnInfo(name = "body_pattern")
    val bodyPattern: String? = null,
    @ColumnInfo(name = "action")
    val action: String,
    @ColumnInfo(name = "mock_response_code")
    val mockResponseCode: Long? = null,
    @ColumnInfo(name = "mock_response_body")
    val mockResponseBody: String? = null,
    @ColumnInfo(name = "mock_response_headers")
    val mockResponseHeaders: String? = null,
    @ColumnInfo(name = "throttle_delay_ms")
    val throttleDelayMs: Long? = null,
    @ColumnInfo(name = "throttle_delay_max_ms")
    val throttleDelayMaxMs: Long? = null,
    @ColumnInfo(name = "enabled", defaultValue = "1")
    val enabled: Long = 1,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
)
