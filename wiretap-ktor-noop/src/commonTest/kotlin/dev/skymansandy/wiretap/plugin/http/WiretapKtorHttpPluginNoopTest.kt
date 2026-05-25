/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.plugin.http

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest

class WiretapKtorHttpPluginNoopTest : DescribeSpec({
    describe("noop plugin") {
        it("passes the request through unchanged") {
            runTest {
                val engine = MockEngine { respond("hi", HttpStatusCode.OK) }
                val client = HttpClient(engine) { install(WiretapKtorHttpPlugin) }

                val response = client.get("https://example.com/ping")

                response.status shouldBe HttpStatusCode.OK
                response.bodyAsText() shouldBe "hi"
            }
        }

        it("install accepts the same DSL block as the real one") {
            runTest {
                val engine = MockEngine { respond("ok", HttpStatusCode.OK) }
                val client = HttpClient(engine) {
                    install(WiretapKtorHttpPlugin) {
                        enabled = true
                        maxContentLength = 1024
                    }
                }
                client.get("https://example.com").status shouldBe HttpStatusCode.OK
            }
        }
    }
})
