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
import io.kotest.matchers.shouldBe
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class WiretapOkHttpInterceptorTest {

    private val httpLogManager = mock<HttpLogManager>(MockMode.autoUnit)
    // FindMatchingRuleUseCase is a final class — construct a real one over a mocked repo
    // and steer behavior via the repo's getEnabledRules() return.
    private val ruleRepository = mock<dev.skymansandy.wiretap.domain.repository.RuleRepository>(MockMode.autoUnit)
    private val findMatchingRule = FindMatchingRuleUseCase(ruleRepository)

    private lateinit var server: MockWebServer

    @BeforeTest
    fun setUp() {
        installTestKoin(httpLogManager, findMatchingRule)
        // saveAndGetId stub used by every logged request.
        everySuspend { httpLogManager.logHttpAndGetId(any()) } returns 42L
        // No matching rule by default.
        everySuspend { ruleRepository.getEnabledRules() } returns emptyList()
        server = MockWebServer()
        server.start()
    }

    @AfterTest
    fun tearDown() {
        server.close()
        teardownTestKoin()
    }

    private fun client(configure: (dev.skymansandy.wiretap.domain.model.config.http.WiretapHttpConfig.() -> Unit) = {}) =
        OkHttpClient.Builder()
            .addInterceptor(WiretapOkHttpInterceptor(configure))
            .build()

    @Test
    fun `passes request through and logs request plus response`() {
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

    @Test
    fun `shouldLog false skips logging entirely`() {
        server.enqueue(MockResponse.Builder().code(200).body("ok").build())

        client { shouldLog = { _, _ -> false } }
            .newCall(Request.Builder().url(server.url("/skip")).build())
            .execute()

        verifySuspend(mode = dev.mokkery.verify.VerifyMode.exactly(0)) {
            httpLogManager.logHttpAndGetId(any())
        }
    }

    @Test
    fun `disabled config short-circuits before any logging`() {
        server.enqueue(MockResponse.Builder().code(200).body("ok").build())

        client { enabled = false }
            .newCall(Request.Builder().url(server.url("/disabled")).build())
            .execute()

        verifySuspend(mode = dev.mokkery.verify.VerifyMode.exactly(0)) {
            httpLogManager.logHttpAndGetId(any())
        }
    }

    @Test
    fun `headerAction Mask is applied to the logged headers`() {
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

    @Test
    fun `request body is captured when present`() {
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

    @Test
    fun `WebSocket upgrade requests are passed through without logging`() {
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

    @Test
    fun `text event-stream responses delete the http log entry`() {
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

    @Test
    fun `matching Mock rule short-circuits the network and serves the mock`() {
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
        everySuspend { ruleRepository.getEnabledRules() } returns listOf(rule)

        // Do NOT enqueue any MockWebServer response — if the interceptor calls through,
        // the test will fail with a NoEnqueuedResponse / SocketException.

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

    @Test
    fun `LogRetention AppSession clears logs once on the first request`() {
        server.enqueue(MockResponse.Builder().code(200).body("ok").build())
        server.enqueue(MockResponse.Builder().code(200).body("ok").build())

        val http = client { logRetention = LogRetention.AppSession }
        http.newCall(Request.Builder().url(server.url("/1")).build()).execute()
        http.newCall(Request.Builder().url(server.url("/2")).build()).execute()

        verifySuspend(mode = dev.mokkery.verify.VerifyMode.exactly(1)) {
            httpLogManager.clearHttpLogs()
        }
    }

    @Test
    fun `LogRetention Days purges old logs once on the first request`() {
        server.enqueue(MockResponse.Builder().code(200).body("ok").build())

        client { logRetention = LogRetention.Days(7) }
            .newCall(Request.Builder().url(server.url("/x")).build())
            .execute()

        verifySuspend {
            httpLogManager.purgeHttpLogsOlderThan(any())
        }
    }

    @Test
    fun `network IO error logs response code zero and rethrows`() {
        // Close the socket before the response is sent to simulate an IO failure.
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
