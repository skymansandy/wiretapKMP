package dev.skymansandy.wiretap.engine

import dev.skymansandy.wiretap.domain.model.WiretapResponse
import io.ktor.client.request.HttpRequestBuilder

internal interface RuleEngine {

    suspend fun evaluate(
        request: HttpRequestBuilder,
        proceed: suspend () -> WiretapResponse,
    ): WiretapResponse
}
