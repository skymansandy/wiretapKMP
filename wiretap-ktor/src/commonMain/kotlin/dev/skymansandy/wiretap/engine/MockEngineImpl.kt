package dev.skymansandy.wiretap.engine

import dev.skymansandy.wiretap.model.ResponseSource
import dev.skymansandy.wiretap.model.WiretapResponse
import dev.skymansandy.wiretap.model.WiretapRule
import io.ktor.client.request.*

class MockEngineImpl : MockEngine {

    override suspend fun execute(request: HttpRequestBuilder, rule: WiretapRule): WiretapResponse {
        return WiretapResponse(
            statusCode = rule.mockResponseCode ?: 200,
            headers = rule.mockResponseHeaders ?: emptyMap(),
            body = rule.mockResponseBody,
            source = ResponseSource.MOCK,
            durationMs = 0,
        )
    }
}
