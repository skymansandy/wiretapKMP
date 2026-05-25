/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.okhttp.http

import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.matches
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import dev.skymansandy.wiretap.domain.model.HttpLog
import dev.skymansandy.wiretap.domain.model.ResponseSource
import dev.skymansandy.wiretap.domain.model.RuleAction
import dev.skymansandy.wiretap.domain.model.WiretapRule
import dev.skymansandy.wiretap.domain.model.config.http.HeaderAction
import dev.skymansandy.wiretap.domain.model.config.http.LogRetention
import dev.skymansandy.wiretap.domain.orchestrator.HttpLogManager
import dev.skymansandy.wiretap.domain.usecase.FindMatchingRuleUseCase
import dev.skymansandy.wiretap.okhttp.testing.installTestKoin
import dev.skymansandy.wiretap.okhttp.testing.teardownTestKoin
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class WiretapOkHttpInterceptorTest : DescribeSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    val httpLogManager = mock<HttpLogManager>(MockMode.autoUnit)
    val findMatchingRule = mock<FindMatchingRuleUseCase>(MockMode.autoUnit)

    lateinit var server: MockWebServer

    fun client(configure: (dev.skymansandy.wiretap.domain.model.config.http.WiretapHttpConfig.() -> Unit) = {}) =
        OkHttpClient.Builder()
            .addInterceptor(WiretapOkHttpInterceptor(configure))
            .build()

    beforeEach {
        installTestKoin(httpLogManager, findMatchingRule)
        everySuspend { httpLogManager.logHttpAndGetId(any()) } returns 42L
        everySuspend { findMatchingRule.invoke(any(), any(), any(), any()) } returns null
        server = MockWebServer()
        server.start()
    }

    afterEach {
        server.close()
        teardownTestKoin()
    }

    describe("intercept") {
        it("passes request through and logs request plus response") {
            server.enqueue(MockResponse.Builder().code(200).body("hello").build())

            val response = client().newCall(
                Request.Builder()
                    .url(server.url("/api/users"))
                    .build(),
            ).execute()

            response.code shouldBe 200
            response.body?.string() shouldBe "hello"

            verifySuspend { httpLogManager.logHttpAndGetId(any()) }
            verifySuspend {
                httpLogManager.updateHttp(
                    matches<HttpLog> {
                        it.id == 42L &&
                            it.responseCode == 200 &&
                            it.source == ResponseSource.Network &&
                            it.responseBody == "hello"
                    },
                )
            }
        }

        it("skips logging entirely when shouldLog returns false") {
            server.enqueue(MockResponse.Builder().code(200).body("ok").build())

            client { shouldLog = { _, _ -> false } }
                .newCall(Request.Builder().url(server.url("/skip")).build())
                .execute()

            verifySuspend(mode = dev.mokkery.verify.VerifyMode.exactly(0)) {
                httpLogManager.logHttpAndGetId(any())
            }
        }

        it("short-circuits before any logging when disabled") {
            server.enqueue(MockResponse.Builder().code(200).body("ok").build())

            client { enabled = false }
                .newCall(Request.Builder().url(server.url("/disabled")).build())
                .execute()

            verifySuspend(mode = dev.mokkery.verify.VerifyMode.exactly(0)) {
                httpLogManager.logHttpAndGetId(any())
            }
        }

        it("applies headerAction Mask to the logged headers") {
            server.enqueue(MockResponse.Builder().code(200).body("ok").build())

            client {
                headerAction = { key ->
                    if (key.equals("Authorization", ignoreCase = true)) HeaderAction.Mask() else HeaderAction.Keep
                }
            }
                .newCall(
                    Request.Builder()
                        .url(server.url("/secure"))
                        .header("Authorization", "Bearer abc")
                        .build(),
                )
                .execute()

            verifySuspend {
                httpLogManager.logHttpAndGetId(
                    matches<HttpLog> { it.requestHeaders["Authorization"] == "***" },
                )
            }
        }

        it("captures request body when present") {
            server.enqueue(MockResponse.Builder().code(200).body("ok").build())

            client()
                .newCall(
                    Request.Builder()
                        .url(server.url("/echo"))
                        .post("payload".toRequestBody())
                        .build(),
                )
                .execute()

            verifySuspend {
                httpLogManager.logHttpAndGetId(
                    matches<HttpLog> { it.requestBody == "payload" && it.method == "POST" },
                )
            }
        }

        it("passes WebSocket upgrade requests through without logging") {
            server.enqueue(
                MockResponse.Builder()
                    .code(101)
                    .addHeader("Upgrade", "websocket")
                    .addHeader("Connection", "Upgrade")
                    .build(),
            )

            client()
                .newCall(
                    Request.Builder()
                        .url(server.url("/ws"))
                        .header("Upgrade", "websocket")
                        .header("Connection", "Upgrade")
                        .build(),
                )
                .execute()

            verifySuspend(mode = dev.mokkery.verify.VerifyMode.exactly(0)) {
                httpLogManager.logHttpAndGetId(any())
            }
        }

        it("deletes the http log entry for text event-stream responses") {
            server.enqueue(
                MockResponse.Builder()
                    .code(200)
                    .addHeader("Content-Type", "text/event-stream")
                    .body("data: hello\n\n")
                    .build(),
            )

            client()
                .newCall(Request.Builder().url(server.url("/events")).build())
                .execute()

            verifySuspend { httpLogManager.logHttpAndGetId(any()) }
            verifySuspend { httpLogManager.deleteHttpLog(42L) }
        }

        it("short-circuits the network and serves the mock when a Mock rule matches") {
            val rule = WiretapRule(
                id = 7,
                method = "*",
                urlMatcher = dev.skymansandy.wiretap.domain.model.matchers.UrlMatcher.Contains("/mocked"),
                action = RuleAction.Mock(
                    responseCode = 418,
                    responseBody = "tea",
                    responseHeaders = mapOf("X-Mock" to "true"),
                ),
            )
            everySuspend { findMatchingRule.invoke(any(), any(), any(), any()) } returns rule

            val response = client()
                .newCall(Request.Builder().url(server.url("/mocked")).build())
                .execute()

            response.code shouldBe 418
            response.body?.string() shouldBe "tea"
            response.header("X-Mock") shouldBe "true"

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

        it("logs response code zero and rethrows on network IO error") {
            server.enqueue(
                MockResponse.Builder()
                    .onResponseStart(mockwebserver3.SocketEffect.CloseSocket())
                    .build(),
            )

            var thrown: Throwable? = null
            try {
                client()
                    .newCall(Request.Builder().url(server.url("/boom")).build())
                    .execute()
            } catch (e: Throwable) {
                thrown = e
            }

            (thrown != null) shouldBe true
            verifySuspend {
                httpLogManager.updateHttp(
                    matches<HttpLog> {
                        it.id == 42L && it.responseCode == 0 && it.source == ResponseSource.Network
                    },
                )
            }
        }
    }

    describe("LogRetention") {
        it("AppSession clears logs once on the first request") {
            server.enqueue(MockResponse.Builder().code(200).body("ok").build())
            server.enqueue(MockResponse.Builder().code(200).body("ok").build())

            val http = client { logRetention = LogRetention.AppSession }
            http.newCall(Request.Builder().url(server.url("/1")).build()).execute()
            http.newCall(Request.Builder().url(server.url("/2")).build()).execute()

            verifySuspend(mode = dev.mokkery.verify.VerifyMode.exactly(1)) {
                httpLogManager.clearHttpLogs()
            }
        }

        it("Days purges old logs once on the first request") {
            server.enqueue(MockResponse.Builder().code(200).body("ok").build())

            client { logRetention = LogRetention.Days(7) }
                .newCall(Request.Builder().url(server.url("/x")).build())
                .execute()

            verifySuspend {
                httpLogManager.purgeHttpLogsOlderThan(any())
            }
        }
    }
})
