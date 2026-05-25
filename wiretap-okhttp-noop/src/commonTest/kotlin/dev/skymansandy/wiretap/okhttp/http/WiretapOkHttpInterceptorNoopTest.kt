/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.okhttp.http

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.test.Test

class WiretapOkHttpInterceptorNoopTest {

    @Test
    fun `noop interceptor passes the request through unchanged`() {
        val server = MockWebServer()
        server.start()
        try {
            server.enqueue(MockResponse.Builder().code(200).body("pong").build())

            val client = OkHttpClient.Builder()
                .addInterceptor(WiretapOkHttpInterceptor())
                .build()

            val response = client.newCall(
                Request.Builder().url(server.url("/ping")).build(),
            ).execute()

            response.code shouldBe 200
            response.body?.string() shouldBe "pong"
            // No Koin context required for the no-op variant — its absence is the contract.
            server.takeRequest().url.toString() shouldContain "/ping"
        } finally {
            server.close()
        }
    }

    @Test
    fun `noop interceptor accepts a configure lambda without failing`() {
        // The no-op variant accepts the same DSL block as the real one, but does nothing with it.
        // Just verify it constructs cleanly.
        WiretapOkHttpInterceptor {
            enabled = true
            maxContentLength = 1024
        }
    }
}
