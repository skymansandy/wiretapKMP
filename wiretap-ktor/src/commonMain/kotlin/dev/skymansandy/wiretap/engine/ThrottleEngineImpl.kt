package dev.skymansandy.wiretap.engine

import dev.skymansandy.wiretap.data.db.entity.WiretapRule
import dev.skymansandy.wiretap.domain.model.ResponseSource
import dev.skymansandy.wiretap.domain.model.WiretapResponse
import io.ktor.client.request.*
import kotlinx.coroutines.delay

class ThrottleEngineImpl : ThrottleEngine {

    override suspend fun execute(
        request: HttpRequestBuilder,
        rule: WiretapRule,
        proceed: suspend () -> WiretapResponse,
    ): WiretapResponse {
        val delayMs = rule.throttleDelayMs ?: 0L
        if (delayMs > 0) {
            delay(delayMs)
        }
        val response = proceed()
        return response.copy(source = ResponseSource.THROTTLE)
    }
}
