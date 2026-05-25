/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.data.mappers

import dev.skymansandy.wiretap.data.db.room.entity.HttpLogEntity
import dev.skymansandy.wiretap.data.db.room.entity.HttpLogListProjection
import dev.skymansandy.wiretap.domain.model.HttpLog
import dev.skymansandy.wiretap.domain.model.ResponseSource
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class HttpLogEntityMapperTest {

    @Test
    fun `entity to domain copies every scalar field`() {
        val entity = HttpLogEntity(
            id = 42,
            url = "https://example.com/api",
            method = "POST",
            requestHeaders = "Authorization: Bearer abc",
            requestBody = "request",
            responseCode = 201L,
            responseHeaders = "Content-Type: application/json",
            responseBody = "response",
            durationMs = 123L,
            source = "Mock",
            timestamp = 1_700_000_000_000L,
            matchedRuleId = 7L,
            protocol = "HTTP/2",
            remoteAddress = "1.2.3.4:443",
            tlsProtocol = "TLSv1.3",
            cipherSuite = "TLS_AES_128_GCM_SHA256",
            certificateCn = "example.com",
            issuerCn = "Let's Encrypt",
            certificateExpiry = "2027-01-01",
        )

        val domain = entity.toDomain()

        domain.id shouldBe 42
        domain.url shouldBe "https://example.com/api"
        domain.method shouldBe "POST"
        domain.requestHeaders shouldBe mapOf("Authorization" to "Bearer abc")
        domain.requestBody shouldBe "request"
        domain.responseCode shouldBe 201
        domain.responseHeaders shouldBe mapOf("Content-Type" to "application/json")
        domain.responseBody shouldBe "response"
        domain.responseBodySize shouldBe "response".length.toLong()
        domain.durationMs shouldBe 123L
        domain.source shouldBe ResponseSource.Mock
        domain.timestamp shouldBe 1_700_000_000_000L
        domain.matchedRuleId shouldBe 7L
        domain.protocol shouldBe "HTTP/2"
        domain.remoteAddress shouldBe "1.2.3.4:443"
        domain.tlsProtocol shouldBe "TLSv1.3"
        domain.cipherSuite shouldBe "TLS_AES_128_GCM_SHA256"
        domain.certificateCn shouldBe "example.com"
        domain.issuerCn shouldBe "Let's Encrypt"
        domain.certificateExpiry shouldBe "2027-01-01"
    }

    @Test
    fun `entity with null response body produces zero response body size`() {
        val entity = baseEntity(responseBody = null)

        entity.toDomain().responseBodySize shouldBe 0L
    }

    @Test
    fun `domain to entity copies every scalar field`() {
        val domain = HttpLog(
            id = 42,
            url = "https://example.com/api",
            method = "POST",
            requestHeaders = mapOf("Authorization" to "Bearer abc"),
            requestBody = "request",
            responseCode = 201,
            responseHeaders = mapOf("Content-Type" to "application/json"),
            responseBody = "response",
            durationMs = 123L,
            source = ResponseSource.Mock,
            timestamp = 1_700_000_000_000L,
            matchedRuleId = 7L,
            protocol = "HTTP/2",
            remoteAddress = "1.2.3.4:443",
            tlsProtocol = "TLSv1.3",
            cipherSuite = "TLS_AES_128_GCM_SHA256",
            certificateCn = "example.com",
            issuerCn = "Let's Encrypt",
            certificateExpiry = "2027-01-01",
        )

        val entity = domain.toRoomEntity()

        entity.id shouldBe 42L
        entity.url shouldBe "https://example.com/api"
        entity.method shouldBe "POST"
        entity.requestHeaders shouldBe "Authorization: Bearer abc"
        entity.requestBody shouldBe "request"
        entity.responseCode shouldBe 201L
        entity.responseHeaders shouldBe "Content-Type: application/json"
        entity.responseBody shouldBe "response"
        entity.durationMs shouldBe 123L
        entity.source shouldBe "Mock"
        entity.timestamp shouldBe 1_700_000_000_000L
        entity.matchedRuleId shouldBe 7L
        entity.protocol shouldBe "HTTP/2"
    }

    @Test
    fun `domain to entity to domain round-trip preserves data`() {
        val original = HttpLog(
            id = 1,
            url = "https://example.com",
            method = "GET",
            requestHeaders = mapOf("Accept" to "application/json"),
            requestBody = "body",
            responseCode = 200,
            responseHeaders = mapOf("X-Trace" to "abc"),
            responseBody = "{}",
            durationMs = 50,
            source = ResponseSource.Network,
            timestamp = 1_700_000_000_000L,
        )

        val roundTripped = original.toRoomEntity().toDomain()

        roundTripped.url shouldBe original.url
        roundTripped.method shouldBe original.method
        roundTripped.requestHeaders shouldBe original.requestHeaders
        roundTripped.requestBody shouldBe original.requestBody
        roundTripped.responseCode shouldBe original.responseCode
        roundTripped.responseHeaders shouldBe original.responseHeaders
        roundTripped.responseBody shouldBe original.responseBody
        roundTripped.durationMs shouldBe original.durationMs
        roundTripped.source shouldBe original.source
        roundTripped.timestamp shouldBe original.timestamp
    }

    @Test
    fun `every ResponseSource value round-trips through the entity`() {
        ResponseSource.entries.forEach { source ->
            val entity = baseEntity().copy(source = source.name)
            entity.toDomain().source shouldBe source
        }
    }

    @Test
    fun `projection to domain copies metadata and computes response body size from column`() {
        val projection = HttpLogListProjection(
            id = 1,
            url = "https://example.com",
            method = "GET",
            requestHeaders = "Accept: */*",
            responseCode = 200,
            responseHeaders = "Content-Type: text/plain",
            responseBodySize = 4096L,
            durationMs = 12,
            source = "Throttle",
            timestamp = 1L,
            matchedRuleId = 9L,
            protocol = "HTTP/1.1",
            remoteAddress = null,
            tlsProtocol = null,
            cipherSuite = null,
            certificateCn = null,
            issuerCn = null,
            certificateExpiry = null,
        )

        val domain = projection.toDomain()

        domain.responseBodySize shouldBe 4096L
        domain.source shouldBe ResponseSource.Throttle
        domain.matchedRuleId shouldBe 9L
        domain.requestHeaders shouldBe mapOf("Accept" to "*/*")
    }

    private fun baseEntity(
        responseBody: String? = "response",
    ) = HttpLogEntity(
        id = 1,
        url = "https://example.com",
        method = "GET",
        requestHeaders = "",
        requestBody = null,
        responseCode = 200,
        responseHeaders = "",
        responseBody = responseBody,
        durationMs = 0,
        source = "Network",
        timestamp = 0L,
    )
}
