/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.okhttp.http

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import okhttp3.Request

class WiretapOkHttpInterceptorNoopTest : DescribeSpec({
    describe("noop interceptor") {
        it("passes the request through unchanged") {
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
                server.takeRequest().url.toString() shouldContain "/ping"
            } finally {
                server.close()
            }
        }

        it("accepts a configure lambda without failing") {
            WiretapOkHttpInterceptor {
                enabled = true
                maxContentLength = 1024
            }
        }
    }
})
