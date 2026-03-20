package dev.skymansandy.wiretap.engine

import dev.skymansandy.wiretap.data.db.entity.WiretapRule
import dev.skymansandy.wiretap.domain.model.ResponseSource
import dev.skymansandy.wiretap.domain.model.RuleAction
import dev.skymansandy.wiretap.domain.model.WiretapResponse
import io.ktor.client.request.HttpRequestBuilder
import kotlinx.coroutines.delay

internal class ThrottleEngineImpl : ThrottleEngine {

    override suspend fun execute(
        request: HttpRequestBuilder,
        rule: WiretapRule,
        proceed: suspend () -> WiretapResponse,
    ): WiretapResponse {

        val throttle = rule.action as RuleAction.Throttle
        val minDelay = throttle.delayMs
        val maxDelay = throttle.delayMaxMs ?: minDelay
        val delayMs = if (maxDelay > minDelay) (minDelay..maxDelay).random() else minDelay
        if (delayMs > 0) {
            delay(delayMs)
        }
        val response = proceed()
        return response.copy(source = ResponseSource.Throttle)
    }
}
