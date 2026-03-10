package dev.skymansandy.wiretap.engine

import dev.skymansandy.wiretap.model.WiretapResponse
import io.ktor.client.request.*

interface RuleEngine {
    suspend fun evaluate(request: HttpRequestBuilder, proceed: suspend () -> WiretapResponse): WiretapResponse
}
