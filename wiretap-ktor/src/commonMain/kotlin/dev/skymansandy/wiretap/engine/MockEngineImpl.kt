package dev.skymansandy.wiretap.engine

import dev.skymansandy.wiretap.data.db.entity.WiretapRule
import dev.skymansandy.wiretap.domain.model.ResponseSource
import dev.skymansandy.wiretap.domain.model.RuleAction
import dev.skymansandy.wiretap.domain.model.WiretapResponse
import io.ktor.client.request.HttpRequestBuilder

internal class MockEngineImpl : MockEngine {

    override suspend fun execute(request: HttpRequestBuilder, rule: WiretapRule): WiretapResponse {
        val mock = rule.action as RuleAction.Mock
        return WiretapResponse(
            statusCode = mock.responseCode,
            headers = mock.responseHeaders ?: emptyMap(),
            body = mock.responseBody,
            source = ResponseSource.Mock,
            durationMs = 0,
        )
    }
}
