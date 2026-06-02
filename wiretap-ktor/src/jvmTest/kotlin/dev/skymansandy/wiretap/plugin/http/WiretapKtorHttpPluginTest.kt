/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.plugin.http

import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.matches
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.exactly
import dev.mokkery.verifySuspend
import dev.skymansandy.wiretap.domain.model.HttpLog
import dev.skymansandy.wiretap.domain.model.ResponseSource
import dev.skymansandy.wiretap.domain.model.RuleAction
import dev.skymansandy.wiretap.domain.model.WiretapRule
import dev.skymansandy.wiretap.domain.model.config.http.HeaderAction
import dev.skymansandy.wiretap.domain.model.matchers.UrlMatcher
import dev.skymansandy.wiretap.domain.orchestrator.HttpLogManager
import dev.skymansandy.wiretap.domain.usecase.FindMatchingRuleUseCase
import dev.skymansandy.wiretap.ktor.testing.EmbeddedTestServer
import dev.skymansandy.wiretap.ktor.testing.installTestKoin
import dev.skymansandy.wiretap.ktor.testing.teardownTestKoin
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlin.time.Duration.Companion.seconds

class WiretapKtorHttpPluginTest : DescribeSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    val httpLogManager = mock<HttpLogManager>(MockMode.autoUnit)
    val findMatchingRule = mock<FindMatchingRuleUseCase>(MockMode.autoUnit)

    lateinit var server: EmbeddedTestServer
    lateinit var client: HttpClient

    fun newClient(
        configure: dev.skymansandy.wiretap.domain.model.config.http.WiretapHttpConfig.() -> Unit = {},
    ): HttpClient = HttpClient(Java) {
        install(WiretapKtorHttpPlugin, configure)
    }

    beforeEach {
        installTestKoin(httpLogManager, findMatchingRule)
        everySuspend { httpLogManager.logHttpAndGetId(any()) } returns 42L
        everySuspend { findMatchingRule.invoke(any(), any(), any(), any()) } returns null
        server = EmbeddedTestServer {
            get("/echo") { call.respondText("hello-get") }
            post("/echo") {
                val body = call.receiveText()
                call.respondText("echoed:$body")
            }
            get("/boom") {
                call.respond(HttpStatusCode.InternalServerError, "boom!")
            }
        }
        client = newClient()
    }

    afterEach {
        runBlocking { client.close() }
        server.stop()
        teardownTestKoin()
    }

    describe("end-to-end roundtrip") {
        it("logs the request and response of a successful GET") {
            runTest(timeout = 10.seconds) {
                val response = client.get("http://127.0.0.1:${server.port}/echo")

                response.status.value shouldBe 200
                response.bodyAsText() shouldBe "hello-get"
                eventually(5.seconds) {
                    verifySuspend {
                        httpLogManager.logHttpAndGetId(
                            matches<HttpLog> {
                                it.method == "GET" && it.url.endsWith("/echo")
                            },
                        )
                    }
                    verifySuspend {
                        httpLogManager.updateHttp(
                            matches<HttpLog> {
                                it.id == 42L &&
                                    it.responseCode == 200 &&
                                    it.responseBody == "hello-get" &&
                                    it.source == ResponseSource.Network
                            },
                        )
                    }
                }
            }
        }

        it("captures the request body on POST") {
            runTest(timeout = 10.seconds) {
                client.post("http://127.0.0.1:${server.port}/echo") {
                    setBody("ping")
                }

                eventually(5.seconds) {
                    verifySuspend {
                        httpLogManager.logHttpAndGetId(
                            matches<HttpLog> {
                                it.method == "POST" && it.requestBody == "ping"
                            },
                        )
                    }
                }
            }
        }
    }

    describe("plugin disabled") {
        it("does not log anything when enabled = false") {
            runTest(timeout = 10.seconds) {
                runBlocking { client.close() }
                client = newClient { enabled = false }

                val response = client.get("http://127.0.0.1:${server.port}/echo")

                response.status.value shouldBe 200
                verifySuspend(mode = exactly(0)) { httpLogManager.logHttpAndGetId(any()) }
                verifySuspend(mode = exactly(0)) { httpLogManager.updateHttp(any()) }
            }
        }
    }

    describe("shouldLog filter") {
        it("skips the DB write entirely when shouldLog returns false") {
            runTest(timeout = 10.seconds) {
                runBlocking { client.close() }
                client = newClient { shouldLog = { _, _ -> false } }

                client.get("http://127.0.0.1:${server.port}/echo")

                verifySuspend(mode = exactly(0)) { httpLogManager.logHttpAndGetId(any()) }
            }
        }
    }

    describe("header masking") {
        it("masks Authorization in the logged request headers") {
            runTest(timeout = 10.seconds) {
                runBlocking { client.close() }
                client = newClient {
                    headerAction = { key ->
                        if (key.equals("Authorization", ignoreCase = true)) HeaderAction.Mask()
                        else HeaderAction.Keep
                    }
                }

                client.get("http://127.0.0.1:${server.port}/echo") {
                    headers.append(HttpHeaders.Authorization, "Bearer abc")
                }

                eventually(5.seconds) {
                    verifySuspend {
                        httpLogManager.logHttpAndGetId(
                            matches<HttpLog> { it.requestHeaders["Authorization"] == "***" },
                        )
                    }
                }
            }
        }
    }

    describe("body truncation") {
        it("truncates the logged request body to maxContentLength") {
            runTest(timeout = 10.seconds) {
                runBlocking { client.close() }
                client = newClient { maxContentLength = 4 }

                client.post("http://127.0.0.1:${server.port}/echo") {
                    setBody("1234567890")
                }

                eventually(5.seconds) {
                    verifySuspend {
                        httpLogManager.logHttpAndGetId(
                            matches<HttpLog> {
                                val body = it.requestBody
                                body != null && body.startsWith("1234") && body.contains("truncated")
                            },
                        )
                    }
                }
            }
        }
    }

    describe("mock rule") {
        it("short-circuits the network and serves the mock body") {
            runTest(timeout = 10.seconds) {
                val rule = WiretapRule(
                    id = 7,
                    method = "*",
                    urlMatcher = UrlMatcher.Contains("/echo"),
                    action = RuleAction.Mock(
                        responseCode = 418,
                        responseBody = "tea",
                        responseHeaders = mapOf("X-Mock" to "true"),
                    ),
                )
                everySuspend { findMatchingRule.invoke(any(), any(), any(), any()) } returns rule

                val response = client.get("http://127.0.0.1:${server.port}/echo")

                response.status.value shouldBe 418
                response.bodyAsText() shouldBe "tea"
                eventually(5.seconds) {
                    verifySuspend {
                        httpLogManager.updateHttp(
                            matches<HttpLog> {
                                it.responseCode == 418 &&
                                    it.source == ResponseSource.Mock &&
                                    it.matchedRuleId == 7L
                            },
                        )
                    }
                }
            }
        }
    }

    describe("throttle rule") {
        it("delays the request but still proceeds to the network") {
            runTest(timeout = 10.seconds) {
                val rule = WiretapRule(
                    id = 9,
                    method = "*",
                    urlMatcher = UrlMatcher.Contains("/echo"),
                    action = RuleAction.Throttle(delayMs = 10L),
                )
                everySuspend { findMatchingRule.invoke(any(), any(), any(), any()) } returns rule

                val response = client.get("http://127.0.0.1:${server.port}/echo")

                response.status.value shouldBe 200
                eventually(5.seconds) {
                    verifySuspend {
                        httpLogManager.updateHttp(
                            matches<HttpLog> {
                                it.responseCode == 200 && it.source == ResponseSource.Throttle
                            },
                        )
                    }
                }
            }
        }
    }

    describe("failure response") {
        it("logs the 5xx status without throwing") {
            runTest(timeout = 10.seconds) {
                val response = client.get("http://127.0.0.1:${server.port}/boom")

                response.status.value shouldBe 500
                eventually(5.seconds) {
                    verifySuspend {
                        httpLogManager.updateHttp(matches<HttpLog> { it.responseCode == 500 })
                    }
                }
            }
        }
    }
})
