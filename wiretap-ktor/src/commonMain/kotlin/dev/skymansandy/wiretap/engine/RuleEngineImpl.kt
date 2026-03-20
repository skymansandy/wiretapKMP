package dev.skymansandy.wiretap.engine

import dev.skymansandy.wiretap.domain.model.RuleAction
import dev.skymansandy.wiretap.domain.model.WiretapResponse
import dev.skymansandy.wiretap.domain.usecase.FindMatchingRuleUseCase
import io.ktor.client.request.HttpRequestBuilder

internal class RuleEngineImpl(
    private val findMatchingRule: FindMatchingRuleUseCase,
    private val mockEngine: MockEngine,
    private val throttleEngine: ThrottleEngine,
) : RuleEngine {

    override suspend fun evaluate(
        request: HttpRequestBuilder,
        proceed: suspend () -> WiretapResponse,
    ): WiretapResponse {
        val url = request.url.buildString()
        val method = request.method.value

        val matchingRule = findMatchingRule(url, method)
            ?: return proceed()

        return when (matchingRule.action) {
            is RuleAction.Mock -> mockEngine.execute(request, matchingRule)
            is RuleAction.Throttle -> throttleEngine.execute(request, matchingRule, proceed)
        }
    }
}
