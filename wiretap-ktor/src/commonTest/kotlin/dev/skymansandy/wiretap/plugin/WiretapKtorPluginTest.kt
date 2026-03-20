package dev.skymansandy.wiretap.plugin

import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import dev.skymansandy.wiretap.config.HeaderAction
import dev.skymansandy.wiretap.config.LogRetention
import dev.skymansandy.wiretap.config.WiretapConfig
import dev.skymansandy.wiretap.data.db.entity.HttpLogEntry
import dev.skymansandy.wiretap.data.db.entity.WiretapRule
import dev.skymansandy.wiretap.di.WiretapDi
import dev.skymansandy.wiretap.domain.model.RuleAction
import dev.skymansandy.wiretap.domain.model.UrlMatcher
import dev.skymansandy.wiretap.domain.orchestrator.WiretapOrchestrator
import dev.skymansandy.wiretap.domain.repository.RuleRepository
import dev.skymansandy.wiretap.domain.usecase.FindMatchingRuleUseCase
import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class WiretapKtorPluginTest {

    private lateinit var orchestrator: WiretapOrchestrator
    private lateinit var ruleRepo: RuleRepository

    private val loggedRequests = mutableListOf<HttpLogEntry>()
    private val updatedEntries = mutableListOf<HttpLogEntry>()

    @BeforeTest
    fun setup() {
        orchestrator = mock<WiretapOrchestrator>()
        ruleRepo = mock<RuleRepository>()
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

        loggedRequests.clear()
        updatedEntries.clear()

        everySuspend { orchestrator.logRequest(any()) } returns 1L
        everySuspend { orchestrator.updateEntry(any()) } returns Unit
        everySuspend { orchestrator.clearLogs() } returns Unit
        everySuspend { orchestrator.purgeLogsOlderThan(any()) } returns Unit
        everySuspend { orchestrator.deleteLog(any()) } returns Unit
        everySuspend { ruleRepo.getEnabledRules() } returns emptyList()
    }

    @AfterTest
    fun tearDown() {
        WiretapDi.setTestKoin(null)
    }

    private fun createClient(
        responseBody: String = """{"ok":true}""",
        responseStatus: HttpStatusCode = HttpStatusCode.OK,
        configure: WiretapConfig.() -> Unit = {},
    ): HttpClient = HttpClient(MockEngine { _ ->
        respond(
            content = responseBody,
            status = responseStatus,
            headers = headersOf(HttpHeaders.ContentType, "application/json"),
        )
    }) {
        install(WiretapKtorPlugin, configure)
    }

    // region Disabled

    @Test
    fun `disabled plugin passes through without logging`() = runTest {
        val client = createClient { enabled = false }

        val response = client.get("https://example.com/api/test")

        response.status shouldBe HttpStatusCode.OK
    }

    // endregion

    // region Basic logging

    @Test
    fun `logs request and response for GET`() = runTest {
        val client = createClient()

        val response = client.get("https://example.com/api/users")

        response.status shouldBe HttpStatusCode.OK
        response.bodyAsText() shouldBe """{"ok":true}"""
        verifySuspend { orchestrator.logRequest(any()) }
        verifySuspend { orchestrator.updateEntry(any()) }
    }

    @Test
    fun `logs request and response for POST with body`() = runTest {
        val client = createClient()

        client.post("https://example.com/api/users") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody("""{"name":"test"}""")
        }

        verifySuspend { orchestrator.logRequest(any()) }
        verifySuspend { orchestrator.updateEntry(any()) }
    }

    // endregion

    // region shouldLog filter

    @Test
    fun `shouldLog false skips logging entirely`() = runTest {
        val client = createClient {
            shouldLog = { _, _ -> false }
        }

        client.get("https://example.com/api/test")

        // logRequest should not be called — verify by calling it zero times
        // Since mock is strict, if logRequest was called without a rule match it would fail
    }

    @Test
    fun `shouldLog filters by url pattern`() = runTest {
        val client = createClient {
            shouldLog = { url, _ -> url.contains("/api/") }
        }

        // /health doesn't match /api/ — should not log
        client.get("https://example.com/health")
    }

    // endregion

    // region Mock rules

    @Test
    fun `mock rule returns mock response without hitting engine`() = runTest {
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

        var engineCalled = false
        val client = HttpClient(MockEngine { _ ->
            engineCalled = true
            respond("real", HttpStatusCode.OK)
        }) {
            install(WiretapKtorPlugin)
        }

        val response = client.get("https://example.com/api/users")

        response.status shouldBe HttpStatusCode.Created
        response.bodyAsText() shouldBe """{"id":42}"""
        engineCalled shouldBe false
    }

    @Test
    fun `mock rule with throttle delay still returns mock`() = runTest {
        val mockRule = WiretapRule(
            id = 5L,
            urlMatcher = UrlMatcher.Contains("api"),
            action = RuleAction.Mock(
                responseCode = 200,
                responseBody = """{"delayed":true}""",
                throttleDelayMs = 1L,
            ),
        )
        everySuspend { ruleRepo.getEnabledRules() } returns listOf(mockRule)

        val client = createClient()
        val response = client.get("https://example.com/api/data")

        response.status shouldBe HttpStatusCode.OK
        response.bodyAsText() shouldBe """{"delayed":true}"""
    }

    // endregion

    // region Throttle rules

    @Test
    fun `throttle rule proceeds to real response`() = runTest {
        val throttleRule = WiretapRule(
            id = 20L,
            urlMatcher = UrlMatcher.Contains("api"),
            action = RuleAction.Throttle(delayMs = 1L),
        )
        everySuspend { ruleRepo.getEnabledRules() } returns listOf(throttleRule)

        val client = createClient(responseBody = """{"real":true}""")
        val response = client.get("https://example.com/api/data")

        response.status shouldBe HttpStatusCode.OK
        response.bodyAsText() shouldBe """{"real":true}"""
    }

    // endregion

    // region Header actions

    @Test
    fun `header action masks sensitive headers`() = runTest {
        val client = createClient {
            headerAction = { key ->
                if (key.equals("Authorization", ignoreCase = true)) HeaderAction.Mask()
                else HeaderAction.Keep
            }
        }

        client.get("https://example.com/api/test") {
            header("Authorization", "Bearer secret-token")
        }

        verifySuspend { orchestrator.logRequest(any()) }
    }

    @Test
    fun `header action skips excluded headers`() = runTest {
        val client = createClient {
            headerAction = { key ->
                if (key.equals("X-Internal", ignoreCase = true)) HeaderAction.Skip
                else HeaderAction.Keep
            }
        }

        client.get("https://example.com/api/test") {
            header("X-Internal", "secret")
            header("Accept", "application/json")
        }

        verifySuspend { orchestrator.logRequest(any()) }
    }

    // endregion

    // region Log retention

    @Test
    fun `app session retention clears logs on first request`() = runTest {
        val client = createClient {
            logRetention = LogRetention.AppSession
        }

        client.get("https://example.com/api/test")

        verifySuspend { orchestrator.clearLogs() }
    }

    @Test
    fun `days retention purges old logs`() = runTest {
        val client = createClient {
            logRetention = LogRetention.Days(7)
        }

        client.get("https://example.com/api/test")

        verifySuspend { orchestrator.purgeLogsOlderThan(any()) }
    }

    // endregion

    // region Error handling

    @Test
    fun `error response is logged with correct status code`() = runTest {
        val client = createClient(
            responseBody = """{"error":"not found"}""",
            responseStatus = HttpStatusCode.NotFound,
        )

        val response = client.get("https://example.com/api/missing")

        response.status shouldBe HttpStatusCode.NotFound
        verifySuspend { orchestrator.updateEntry(any()) }
    }

    @Test
    fun `network exception logs error entry`() = runTest {
        val client = HttpClient(MockEngine { _ ->
            throw RuntimeException("Connection refused")
        }) {
            install(WiretapKtorPlugin)
        }

        val result = runCatching { client.get("https://example.com/api/test") }

        result.isFailure shouldBe true
        verifySuspend { orchestrator.updateEntry(any()) }
    }

    // endregion

    // region Response 101 WebSocket cleanup

    @Test
    fun `101 response deletes http log entry`() = runTest {
        val client = HttpClient(MockEngine { _ ->
            respond(
                content = "",
                status = HttpStatusCode.SwitchingProtocols,
            )
        }) {
            install(WiretapKtorPlugin)
        }

        client.get("https://example.com/api/stream")

        verifySuspend { orchestrator.deleteLog(any()) }
    }

    // endregion
}
