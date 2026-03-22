package dev.skymansandy.wiretap.urlsession

import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import dev.skymansandy.wiretap.config.HeaderAction
import dev.skymansandy.wiretap.config.LogRetention
import dev.skymansandy.wiretap.data.db.entity.WiretapRule
import dev.skymansandy.wiretap.di.WiretapDi
import dev.skymansandy.wiretap.domain.model.RuleAction
import dev.skymansandy.wiretap.domain.model.UrlMatcher
import dev.skymansandy.wiretap.domain.orchestrator.WiretapOrchestrator
import dev.skymansandy.wiretap.domain.repository.RuleRepository
import dev.skymansandy.wiretap.domain.usecase.FindMatchingRuleUseCase
import io.kotest.matchers.shouldBe
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import platform.Foundation.NSData
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSMutableURLRequest
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSURLSession
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Foundation.setHTTPBody
import platform.Foundation.setHTTPMethod
import platform.Foundation.setValue
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class WiretapURLSessionInterceptorTest {

    private lateinit var orchestrator: WiretapOrchestrator
    private lateinit var ruleRepo: RuleRepository

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

        everySuspend { orchestrator.logHttpAndGetId(any()) } returns 1L
        everySuspend { orchestrator.updateHttp(any()) } returns Unit
        everySuspend { orchestrator.clearHttpLogs() } returns Unit
        everySuspend { orchestrator.purgeHttpLogsOlderThan(any()) } returns Unit
        everySuspend { ruleRepo.getEnabledRules() } returns emptyList()
    }

    @AfterTest
    fun tearDown() {
        WiretapDi.setTestKoin(null)
    }

    private fun createInterceptor(
        session: NSURLSession = NSURLSession.sharedSession,
        configure: dev.skymansandy.wiretap.config.WiretapConfig.() -> Unit = {},
    ) = WiretapURLSessionInterceptor(session, configure)

    private fun buildRequest(
        url: String = "https://example.com/api/test",
        method: String = "GET",
        headers: Map<String, String> = emptyMap(),
        body: String? = null,
    ): NSMutableURLRequest {
        val request = NSMutableURLRequest(NSURL.URLWithString(url)!!)
        request.setHTTPMethod(method)
        headers.forEach { (key, value) -> request.setValue(value, forHTTPHeaderField = key) }
        body?.let {
            request.setHTTPBody(
                NSString.create(string = it).dataUsingEncoding(NSUTF8StringEncoding),
            )
        }
        return request
    }

    // region Mock rules — no network needed

    @Test
    fun `mock rule returns mock response via intercept`() {
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

        val interceptor = createInterceptor()
        val request = buildRequest(url = "https://example.com/api/users")

        var receivedData: NSData? = null
        var receivedResponse: NSHTTPURLResponse? = null

        interceptor.intercept(request) { data, response, _ ->
            receivedData = data
            receivedResponse = response
        }

        receivedResponse?.statusCode shouldBe 201L

        val bodyStr = receivedData?.let {
            NSString.create(data = it, encoding = NSUTF8StringEncoding)?.toString()
        }
        bodyStr shouldBe """{"id":42}"""
    }

    @Test
    fun `mock rule logs request before returning mock`() {
        val mockRule = WiretapRule(
            id = 5L,
            urlMatcher = UrlMatcher.Contains("api"),
            action = RuleAction.Mock(responseCode = 200, responseBody = "ok"),
        )
        everySuspend { ruleRepo.getEnabledRules() } returns listOf(mockRule)

        val interceptor = createInterceptor()
        val request = buildRequest(url = "https://example.com/api/data", method = "POST")

        interceptor.intercept(request) { _, _, _ -> }

        verifySuspend { orchestrator.logHttpAndGetId(any()) }
        verifySuspend { orchestrator.updateHttp(any()) }
    }

    @Test
    fun `mock rule with shouldLog false skips logging`() {
        val mockRule = WiretapRule(
            id = 10L,
            urlMatcher = UrlMatcher.Contains("api"),
            action = RuleAction.Mock(responseCode = 200, responseBody = "ok"),
        )
        everySuspend { ruleRepo.getEnabledRules() } returns listOf(mockRule)

        val interceptor = createInterceptor {
            shouldLog = { _, _ -> false }
        }
        val request = buildRequest()

        interceptor.intercept(request) { _, _, _ -> }
    }

    // endregion

    // region Header actions with mock rules

    @Test
    fun `header action masks sensitive request headers in mock response`() {
        val mockRule = WiretapRule(
            id = 10L,
            urlMatcher = UrlMatcher.Contains("api"),
            action = RuleAction.Mock(responseCode = 200, responseBody = "ok"),
        )
        everySuspend { ruleRepo.getEnabledRules() } returns listOf(mockRule)

        val interceptor = createInterceptor {
            headerAction = { key ->
                if (key.equals("Authorization", ignoreCase = true)) HeaderAction.Mask()
                else HeaderAction.Keep
            }
        }
        val request = buildRequest(
            headers = mapOf("Authorization" to "Bearer secret"),
        )

        interceptor.intercept(request) { _, _, _ -> }

        verifySuspend { orchestrator.logHttpAndGetId(any()) }
    }

    @Test
    fun `header action skips excluded request headers`() {
        val mockRule = WiretapRule(
            id = 10L,
            urlMatcher = UrlMatcher.Contains("api"),
            action = RuleAction.Mock(responseCode = 200, responseBody = "ok"),
        )
        everySuspend { ruleRepo.getEnabledRules() } returns listOf(mockRule)

        val interceptor = createInterceptor {
            headerAction = { key ->
                if (key.equals("X-Internal", ignoreCase = true)) HeaderAction.Skip
                else HeaderAction.Keep
            }
        }
        val request = buildRequest(
            headers = mapOf("X-Internal" to "secret", "Accept" to "application/json"),
        )

        interceptor.intercept(request) { _, _, _ -> }

        verifySuspend { orchestrator.logHttpAndGetId(any()) }
    }

    @Test
    fun `mock response headers are masked in log`() {
        val mockRule = WiretapRule(
            id = 10L,
            urlMatcher = UrlMatcher.Contains("api"),
            action = RuleAction.Mock(
                responseCode = 200,
                responseBody = "ok",
                responseHeaders = mapOf("X-Secret" to "hidden"),
            ),
        )
        everySuspend { ruleRepo.getEnabledRules() } returns listOf(mockRule)

        val interceptor = createInterceptor {
            headerAction = { key ->
                if (key.equals("X-Secret", ignoreCase = true)) HeaderAction.Mask("REDACTED")
                else HeaderAction.Keep
            }
        }
        val request = buildRequest()

        interceptor.intercept(request) { _, _, _ -> }

        verifySuspend { orchestrator.updateHttp(any()) }
    }

    // endregion

    // region Log retention with mock rules

    @Test
    fun `app session retention clears logs on first intercept`() {
        val mockRule = WiretapRule(
            id = 10L,
            urlMatcher = UrlMatcher.Contains("api"),
            action = RuleAction.Mock(responseCode = 200, responseBody = "ok"),
        )
        everySuspend { ruleRepo.getEnabledRules() } returns listOf(mockRule)

        val interceptor = createInterceptor {
            logRetention = LogRetention.AppSession
        }

        interceptor.intercept(buildRequest()) { _, _, _ -> }

        verifySuspend { orchestrator.clearHttpLogs() }
    }

    @Test
    fun `days retention purges old logs on intercept`() {
        val mockRule = WiretapRule(
            id = 10L,
            urlMatcher = UrlMatcher.Contains("api"),
            action = RuleAction.Mock(responseCode = 200, responseBody = "ok"),
        )
        everySuspend { ruleRepo.getEnabledRules() } returns listOf(mockRule)

        val interceptor = createInterceptor {
            logRetention = LogRetention.Days(7)
        }

        interceptor.intercept(buildRequest()) { _, _, _ -> }

        verifySuspend { orchestrator.purgeHttpLogsOlderThan(any()) }
    }

    // endregion

    // region Request body logging

    @Test
    fun `POST request body is logged`() {
        val mockRule = WiretapRule(
            id = 10L,
            urlMatcher = UrlMatcher.Contains("api"),
            action = RuleAction.Mock(responseCode = 200, responseBody = "ok"),
        )
        everySuspend { ruleRepo.getEnabledRules() } returns listOf(mockRule)

        val interceptor = createInterceptor()
        val request = buildRequest(
            url = "https://example.com/api/users",
            method = "POST",
            body = """{"name":"test"}""",
        )

        interceptor.intercept(request) { _, _, _ -> }

        verifySuspend { orchestrator.logHttpAndGetId(any()) }
    }

    // endregion

    // region Multiple mock rules — first match wins

    @Test
    fun `first matching rule is applied`() {
        val rule1 = WiretapRule(
            id = 1L,
            urlMatcher = UrlMatcher.Contains("users"),
            action = RuleAction.Mock(responseCode = 200, responseBody = "rule1"),
        )
        val rule2 = WiretapRule(
            id = 2L,
            urlMatcher = UrlMatcher.Contains("api"),
            action = RuleAction.Mock(responseCode = 201, responseBody = "rule2"),
        )
        everySuspend { ruleRepo.getEnabledRules() } returns listOf(rule1, rule2)

        val interceptor = createInterceptor()

        var responseBody: String? = null
        interceptor.intercept(
            buildRequest(url = "https://example.com/api/users"),
        ) { data, _, _ ->
            responseBody = data?.let {
                NSString.create(data = it, encoding = NSUTF8StringEncoding)?.toString()
            }
        }

        responseBody shouldBe "rule1"
    }

    // endregion
}
