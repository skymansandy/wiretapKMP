/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.util

import dev.skymansandy.wiretap.domain.model.HttpLog
import dev.skymansandy.wiretap.domain.model.ResponseSource
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain

class HttpLogUtilTest : DescribeSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    describe("buildShareText") {
        it("starts with method status and url") {
            val text = buildShareText(log(method = "POST", responseCode = 201))

            val lines = text.lines()
            lines[0] shouldBe "POST 201"
            lines[1] shouldBe "https://example.com/api/users"
        }

        it("reports duration and source") {
            val text = buildShareText(
                log(durationMs = 250, source = ResponseSource.Mock),
            )

            text shouldContain "Duration: 250ms | Source: Mock"
        }

        it("omits optional network metadata when null") {
            val text = buildShareText(log())

            text shouldNotContain "HTTP Version:"
            text shouldNotContain "Remote Address:"
            text shouldNotContain "TLS Protocol:"
            text shouldNotContain "Cipher Suite:"
            text shouldNotContain "Certificate CN:"
            text shouldNotContain "Issuer CN:"
            text shouldNotContain "Valid Until:"
        }

        it("emits all metadata lines when populated") {
            val text = buildShareText(
                log(
                    protocol = "HTTP/2",
                    remoteAddress = "1.2.3.4:443",
                    tlsProtocol = "TLSv1.3",
                    cipherSuite = "TLS_AES_128_GCM_SHA256",
                    certificateCn = "example.com",
                    issuerCn = "Let's Encrypt",
                    certificateExpiry = "2027-01-01",
                ),
            )

            text shouldContain "HTTP Version: HTTP/2"
            text shouldContain "Remote Address: 1.2.3.4:443"
            text shouldContain "TLS Protocol: TLSv1.3"
            text shouldContain "Cipher Suite: TLS_AES_128_GCM_SHA256"
            text shouldContain "Certificate CN: example.com"
            text shouldContain "Issuer CN: Let's Encrypt"
            text shouldContain "Valid Until: 2027-01-01"
        }

        it("prints none placeholder for absent request headers and body") {
            val text = buildShareText(log(requestHeaders = emptyMap(), requestBody = null))

            text shouldContain "--- Request Headers ---\n(none)"
            text shouldContain "--- Request Body ---\n(none)"
        }

        it("lists headers as key colon value lines") {
            val text = buildShareText(
                log(
                    requestHeaders = mapOf("Authorization" to "Bearer abc"),
                    responseHeaders = mapOf("Content-Type" to "application/json"),
                ),
            )

            text shouldContain "Authorization: Bearer abc"
            text shouldContain "Content-Type: application/json"
        }

        it("embeds the response body verbatim at the end") {
            val text = buildShareText(log(responseBody = """{"ok":true}"""))

            text.endsWith("""{"ok":true}""") shouldBe true
        }
    }

    describe("buildCurlCommand") {
        it("emits method and url") {
            val cmd = buildCurlCommand(log(method = "GET"))

            cmd shouldContain "curl -X GET 'https://example.com/api/users'"
        }

        it("emits -H flag per header") {
            val cmd = buildCurlCommand(
                log(requestHeaders = mapOf("Authorization" to "Bearer abc", "Accept" to "application/json")),
            )

            cmd shouldContain "-H 'Authorization: Bearer abc'"
            cmd shouldContain "-H 'Accept: application/json'"
        }

        it("emits --data-raw when request body is present") {
            val cmd = buildCurlCommand(log(method = "POST", requestBody = """{"a":1}"""))

            cmd shouldContain """--data-raw '{"a":1}'"""
        }

        it("omits --data-raw for empty body") {
            val cmd = buildCurlCommand(log(method = "POST", requestBody = ""))

            cmd shouldNotContain "--data-raw"
        }

        it("omits --data-raw for null body") {
            val cmd = buildCurlCommand(log(method = "GET", requestBody = null))

            cmd shouldNotContain "--data-raw"
        }
    }
})

@Suppress("LongParameterList")
private fun log(
    url: String = "https://example.com/api/users",
    method: String = "GET",
    requestHeaders: Map<String, String> = emptyMap(),
    requestBody: String? = null,
    responseCode: Int = 200,
    responseHeaders: Map<String, String> = emptyMap(),
    responseBody: String? = null,
    durationMs: Long = 0,
    source: ResponseSource = ResponseSource.Network,
    protocol: String? = null,
    remoteAddress: String? = null,
    tlsProtocol: String? = null,
    cipherSuite: String? = null,
    certificateCn: String? = null,
    issuerCn: String? = null,
    certificateExpiry: String? = null,
) = HttpLog(
    url = url,
    method = method,
    requestHeaders = requestHeaders,
    requestBody = requestBody,
    responseCode = responseCode,
    responseHeaders = responseHeaders,
    responseBody = responseBody,
    durationMs = durationMs,
    source = source,
    timestamp = 0L,
    protocol = protocol,
    remoteAddress = remoteAddress,
    tlsProtocol = tlsProtocol,
    cipherSuite = cipherSuite,
    certificateCn = certificateCn,
    issuerCn = issuerCn,
    certificateExpiry = certificateExpiry,
)
