package dev.skymansandy.wiretap.okhttp

import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import dev.skymansandy.wiretap.config.HeaderAction
import dev.skymansandy.wiretap.config.LogRetention
import dev.skymansandy.wiretap.config.WiretapConfig
import dev.skymansandy.wiretap.data.db.entity.WiretapRule
import dev.skymansandy.wiretap.di.WiretapDi
import dev.skymansandy.wiretap.domain.model.RuleAction
import dev.skymansandy.wiretap.domain.model.UrlMatcher
import dev.skymansandy.wiretap.domain.orchestrator.WiretapOrchestrator
import dev.skymansandy.wiretap.domain.repository.RuleRepository
import dev.skymansandy.wiretap.domain.usecase.FindMatchingRuleUseCase
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class WiretapOkHttpInterceptorTest {

    private lateinit var orchestrator: WiretapOrchestrator
    private lateinit var ruleRepo: RuleRepository

    private lateinit var server: MockWebServer

    @BeforeTest
    fun setup() {
        orchestrator = mock()
        ruleRepo = mock()
        val findMatchingRule = FindMatchingRuleUseCase(ruleRepo)

        WiretapDi.setTestKoin(
            koinApplication {
                modules(
                    module {
                        single<WiretapOrchestrator> { orchestrator }
                        single { findMatchingRule }
                    },
                )
            }.koin,
        )

        everySuspend { orchestrator.logRequest(any()) } returns 1L
        everySuspend { orchestrator.updateEntry(any()) } returns Unit
        everySuspend { orchestrator.clearLogs() } returns Unit
        everySuspend { orchestrator.purgeLogsOlderThan(any()) } returns Unit
        everySuspend { orchestrator.deleteLog(any()) } returns Unit
        everySuspend { ruleRepo.getEnabledRules() } returns emptyList()

        server = MockWebServer()
        server.start()
    }

    @AfterTest
    fun tearDown() {
        server.close()
        WiretapDi.setTestKoin(null)
    }

    private fun createClient(
        configure: WiretapConfig.() -> Unit = {},
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(WiretapOkHttpInterceptor(configure))
        .build()

    private fun mockResponse(
        code: Int = 200,
        body: String = "",
        headers: Map<String, String> = emptyMap(),
    ): MockResponse {
        val headerBuilder = Headers.Builder()
        headers.forEach { (k, v) -> headerBuilder.add(k, v) }
        return MockResponse(code, headerBuilder.build(), body)
    }

    // region Disabled

    @Test
    fun `disabled interceptor passes through without logging`() {
        server.enqueue(mockResponse(body = """{"ok":true}"""))
        val client = createClient { enabled = false }

        val response = client.newCall(
            Request.Builder().url(server.url("/api/test")).build(),
        ).execute()

        response.code shouldBe 200
    }

    // endregion

    // region Basic logging

    @Test
    fun `logs request and response for GET`() {
        server.enqueue(
            mockResponse(
                body = """{"users":[]}""",
                headers = mapOf("Content-Type" to "application/json"),
            ),
        )
        val client = createClient()

        val response = client.newCall(
            Request.Builder().url(server.url("/api/users")).build(),
        ).execute()

        response.code shouldBe 200
        verifySuspend { orchestrator.logRequest(any()) }
        verifySuspend { orchestrator.updateEntry(any()) }
    }

    @Test
    fun `logs request and response for POST with body`() {
        server.enqueue(mockResponse(code = 201, body = """{"id":1}"""))
        val client = createClient()
        val jsonBody = """{"name":"test"}""".toRequestBody("application/json".toMediaType())

        client.newCall(
            Request.Builder()
                .url(server.url("/api/users"))
                .post(jsonBody)
                .build(),
        ).execute()

        verifySuspend { orchestrator.logRequest(any()) }
        verifySuspend { orchestrator.updateEntry(any()) }
    }

    // endregion

    // region shouldLog filter

    @Test
    fun `shouldLog false skips logging`() {
        server.enqueue(mockResponse(body = "ok"))
        val client = createClient {
            shouldLog = { _, _ -> false }
        }

        client.newCall(
            Request.Builder().url(server.url("/api/test")).build(),
        ).execute()
    }

    @Test
    fun `shouldLog filters by url`() {
        server.enqueue(mockResponse(body = "ok"))
        val client = createClient {
            shouldLog = { url, _ -> url.contains("/api/") }
        }

        client.newCall(
            Request.Builder().url(server.url("/health")).build(),
        ).execute()
    }

    // endregion

    // region Mock rules

    @Test
    fun `mock rule returns mock response without network call`() {
        val mockRule = WiretapRule(
            id = 10L,
            urlMatcher = UrlMatcher.Contains("users"),
            action = RuleAction.Mock(
                responseCode = 201,
                responseBody = """{"id":42}""",
                responseHeaders = mapOf("X-Mock" to "true"),
            ),
        )
        everySuspend { ruleRepo.getEnabledRules() } returns listOf(mockRule)
        val client = createClient()

        val response = client.newCall(
            Request.Builder().url(server.url("/api/users")).build(),
        ).execute()

        response.code shouldBe 201
        response.body?.string() shouldContain """"id":42"""
        server.requestCount shouldBe 0
    }

    @Test
    fun `mock rule respects shouldLog false`() {
        val mockRule = WiretapRule(
            id = 10L,
            urlMatcher = UrlMatcher.Contains("users"),
            action = RuleAction.Mock(
                responseCode = 200,
                responseBody = """{"mock":true}""",
            ),
        )
        everySuspend { ruleRepo.getEnabledRules() } returns listOf(mockRule)
        val client = createClient {
            shouldLog = { _, _ -> false }
        }

        val response = client.newCall(
            Request.Builder().url(server.url("/api/users")).build(),
        ).execute()

        response.code shouldBe 200
    }

    // endregion

    // region Throttle rules

    @Test
    fun `throttle rule proceeds to real response`() {
        server.enqueue(mockResponse(body = """{"real":true}"""))
        val throttleRule = WiretapRule(
            id = 20L,
            urlMatcher = UrlMatcher.Contains("api"),
            action = RuleAction.Throttle(delayMs = 1L),
        )
        everySuspend { ruleRepo.getEnabledRules() } returns listOf(throttleRule)
        val client = createClient()

        val response = client.newCall(
            Request.Builder().url(server.url("/api/data")).build(),
        ).execute()

        response.code shouldBe 200
        response.body?.string() shouldBe """{"real":true}"""
        verifySuspend { orchestrator.updateEntry(any()) }
    }

    // endregion

    // region Header actions

    @Test
    fun `header action masks sensitive headers`() {
        server.enqueue(mockResponse(body = "ok"))
        val client = createClient {
            headerAction = { key ->
                if (key.equals("Authorization", ignoreCase = true)) HeaderAction.Mask()
                else HeaderAction.Keep
            }
        }

        client.newCall(
            Request.Builder()
                .url(server.url("/api/test"))
                .header("Authorization", "Bearer secret-token")
                .build(),
        ).execute()

        verifySuspend { orchestrator.logRequest(any()) }
    }

    @Test
    fun `header action skips excluded headers`() {
        server.enqueue(mockResponse(body = "ok"))
        val client = createClient {
            headerAction = { key ->
                if (key.equals("X-Internal", ignoreCase = true)) HeaderAction.Skip
                else HeaderAction.Keep
            }
        }

        client.newCall(
            Request.Builder()
                .url(server.url("/api/test"))
                .header("X-Internal", "secret")
                .header("Accept", "application/json")
                .build(),
        ).execute()

        verifySuspend { orchestrator.logRequest(any()) }
    }

    @Test
    fun `header action masks response headers`() {
        server.enqueue(
            mockResponse(
                body = "ok",
                headers = mapOf("X-Secret" to "hidden-value"),
            ),
        )
        val client = createClient {
            headerAction = { key ->
                if (key.equals("X-Secret", ignoreCase = true)) HeaderAction.Mask("REDACTED")
                else HeaderAction.Keep
            }
        }

        client.newCall(
            Request.Builder().url(server.url("/api/test")).build(),
        ).execute()

        verifySuspend { orchestrator.updateEntry(any()) }
    }

    // endregion

    // region Log retention

    @Test
    fun `app session retention clears logs on first request`() {
        server.enqueue(mockResponse(body = "ok"))
        val client = createClient {
            logRetention = LogRetention.AppSession
        }

        client.newCall(
            Request.Builder().url(server.url("/api/test")).build(),
        ).execute()

        verifySuspend { orchestrator.clearLogs() }
    }

    @Test
    fun `days retention purges old logs`() {
        server.enqueue(mockResponse(body = "ok"))
        val client = createClient {
            logRetention = LogRetention.Days(7)
        }

        client.newCall(
            Request.Builder().url(server.url("/api/test")).build(),
        ).execute()

        verifySuspend { orchestrator.purgeLogsOlderThan(any()) }
    }

    // endregion

    // region Error handling

    @Test
    fun `error response is logged with correct status code`() {
        server.enqueue(mockResponse(code = 404, body = """{"error":"not found"}"""))
        val client = createClient()

        val response = client.newCall(
            Request.Builder().url(server.url("/api/missing")).build(),
        ).execute()

        response.code shouldBe 404
        verifySuspend { orchestrator.updateEntry(any()) }
    }

    @Test
    fun `network exception logs error entry with code 0`() {
        server.close()
        val client = createClient()

        val result = runCatching {
            client.newCall(
                Request.Builder().url("http://localhost:1/api/test").build(),
            ).execute()
        }

        result.isFailure shouldBe true
        verifySuspend { orchestrator.updateEntry(any()) }
    }

    // endregion

    // region WebSocket upgrade skip

    @Test
    fun `websocket upgrade request is skipped`() {
        server.enqueue(mockResponse(body = "ok"))
        val client = createClient()

        client.newCall(
            Request.Builder()
                .url(server.url("/ws"))
                .header("Upgrade", "websocket")
                .build(),
        ).execute()
    }

    // endregion

    // region TLS info

    @Test
    fun `non-tls response has null tls fields`() {
        server.enqueue(mockResponse(body = "ok"))
        val client = createClient()

        client.newCall(
            Request.Builder().url(server.url("/api/test")).build(),
        ).execute()

        verifySuspend { orchestrator.updateEntry(any()) }
    }

    // endregion
}
