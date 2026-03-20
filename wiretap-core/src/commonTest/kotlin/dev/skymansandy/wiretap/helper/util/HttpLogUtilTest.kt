package dev.skymansandy.wiretap.helper.util

import dev.skymansandy.wiretap.httpLogEntry
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.string.shouldStartWith
import kotlin.test.Test

class HttpLogUtilTest {

    // region buildShareText

    @Test
    fun `buildShareText includes method and response code`() {
        val entry = httpLogEntry(method = "POST", responseCode = 201)

        val text = buildShareText(entry)

        text shouldContain "POST 201"
    }

    @Test
    fun `buildShareText includes url`() {
        val entry = httpLogEntry(url = "https://api.example.com/users")

        val text = buildShareText(entry)

        text shouldContain "https://api.example.com/users"
    }

    @Test
    fun `buildShareText includes duration and source`() {
        val entry = httpLogEntry(durationMs = 150)

        val text = buildShareText(entry)

        text shouldContain "Duration: 150ms"
        text shouldContain "Source: Network"
    }

    @Test
    fun `buildShareText includes optional connection fields when present`() {
        val entry = httpLogEntry(
            protocol = "HTTP/2",
            remoteAddress = "1.2.3.4",
            tlsProtocol = "TLSv1.3",
            cipherSuite = "AES_256_GCM",
            certificateCn = "*.example.com",
            issuerCn = "DigiCert",
            certificateExpiry = "2027-01-01",
        )

        val text = buildShareText(entry)

        text shouldContain "HTTP Version: HTTP/2"
        text shouldContain "Remote Address: 1.2.3.4"
        text shouldContain "TLS Protocol: TLSv1.3"
        text shouldContain "Cipher Suite: AES_256_GCM"
        text shouldContain "Certificate CN: *.example.com"
        text shouldContain "Issuer CN: DigiCert"
        text shouldContain "Valid Until: 2027-01-01"
    }

    @Test
    fun `buildShareText omits optional fields when null`() {
        val entry = httpLogEntry(protocol = null, remoteAddress = null)

        val text = buildShareText(entry)

        text shouldNotContain "HTTP Version:"
        text shouldNotContain "Remote Address:"
    }

    @Test
    fun `buildShareText includes request headers`() {
        val entry = httpLogEntry(requestHeaders = mapOf("Authorization" to "Bearer token"))

        val text = buildShareText(entry)

        text shouldContain "Authorization: Bearer token"
    }

    @Test
    fun `buildShareText shows none for empty headers`() {
        val entry = httpLogEntry(requestHeaders = emptyMap())

        val text = buildShareText(entry)

        text shouldContain "--- Request Headers ---"
        text shouldContain "(none)"
    }

    @Test
    fun `buildShareText includes request and response body`() {
        val entry = httpLogEntry(
            requestBody = """{"name":"John"}""",
            responseBody = """{"id":1}""",
        )

        val text = buildShareText(entry)

        text shouldContain """{"name":"John"}"""
        text shouldContain """{"id":1}"""
    }

    @Test
    fun `buildShareText shows none for null bodies`() {
        val entry = httpLogEntry(requestBody = null, responseBody = null)

        val text = buildShareText(entry)

        text shouldContain "--- Request Body ---"
        text shouldContain "--- Response Body ---"
    }

    // endregion

    // region buildCurlCommand

    @Test
    fun `buildCurlCommand includes method and url`() {
        val entry = httpLogEntry(method = "GET", url = "https://api.example.com/users")

        val curl = buildCurlCommand(entry)

        curl shouldStartWith "curl -X GET 'https://api.example.com/users'"
    }

    @Test
    fun `buildCurlCommand includes headers`() {
        val entry = httpLogEntry(
            requestHeaders = mapOf(
                "Content-Type" to "application/json",
                "Accept" to "application/json",
            ),
        )

        val curl = buildCurlCommand(entry)

        curl shouldContain "-H 'Content-Type: application/json'"
        curl shouldContain "-H 'Accept: application/json'"
    }

    @Test
    fun `buildCurlCommand includes body for POST`() {
        val entry = httpLogEntry(
            method = "POST",
            requestBody = """{"name":"John"}""",
        )

        val curl = buildCurlCommand(entry)

        curl shouldContain """--data-raw '{"name":"John"}'"""
    }

    @Test
    fun `buildCurlCommand omits body when null`() {
        val entry = httpLogEntry(method = "GET", requestBody = null)

        val curl = buildCurlCommand(entry)

        curl shouldNotContain "--data-raw"
    }

    @Test
    fun `buildCurlCommand omits body when empty`() {
        val entry = httpLogEntry(method = "GET", requestBody = "")

        val curl = buildCurlCommand(entry)

        curl shouldNotContain "--data-raw"
    }

    // endregion
}
