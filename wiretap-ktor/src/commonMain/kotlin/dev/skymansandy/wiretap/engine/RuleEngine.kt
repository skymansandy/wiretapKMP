package dev.skymansandy.wiretap.engine

import dev.skymansandy.wiretap.domain.model.RuleAction
import dev.skymansandy.wiretap.domain.model.WiretapResponse
import dev.skymansandy.wiretap.domain.repository.RuleRepository
import io.ktor.client.request.*

internal interface RuleEngine {
    suspend fun evaluate(request: HttpRequestBuilder, proceed: suspend () -> WiretapResponse): WiretapResponse
}

internal class RuleEngineImpl(
    private val ruleRepository: RuleRepository,
    private val mockEngine: MockEngine,
    private val throttleEngine: ThrottleEngine,
) : RuleEngine {

    override suspend fun evaluate(
        request: HttpRequestBuilder,
        proceed: suspend () -> WiretapResponse,
    ): WiretapResponse {
        val url = request.url.buildString()
        val method = request.method.value

        val matchingRule = ruleRepository.findMatchingRule(url, method)
            ?: return proceed()

        return when (matchingRule.action) {
            RuleAction.MOCK -> mockEngine.execute(request, matchingRule)
            RuleAction.THROTTLE -> throttleEngine.execute(request, matchingRule, proceed)
        }
    }
}
