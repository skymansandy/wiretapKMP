/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.plugin.sse

import dev.skymansandy.wiretap.helper.markers.ExperimentalWiretapSseApi
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalWiretapSseApi::class)
class WiretapKtorSsePluginNoopTest : DescribeSpec({
    describe("noop SSE plugin") {
        it("passes the request through unchanged") {
            runTest {
                val engine = MockEngine { respond("hi", HttpStatusCode.OK) }
                val client = HttpClient(engine) { install(WiretapKtorSsePlugin) }

                val response = client.get("https://example.com/events")

                response.status shouldBe HttpStatusCode.OK
                response.bodyAsText() shouldBe "hi"
            }
        }

        it("install accepts the same DSL block as the real one") {
            runTest {
                val engine = MockEngine { respond("ok", HttpStatusCode.OK) }
                val client = HttpClient(engine) {
                    install(WiretapKtorSsePlugin) {
                        enabled = true
                    }
                }
                client.get("https://example.com").status shouldBe HttpStatusCode.OK
            }
        }
    }
})
