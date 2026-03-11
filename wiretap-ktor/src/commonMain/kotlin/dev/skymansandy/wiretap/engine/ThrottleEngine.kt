package dev.skymansandy.wiretap.engine

import dev.skymansandy.wiretap.model.WiretapResponse
import dev.skymansandy.wiretap.model.WiretapRule
import io.ktor.client.request.*

interface ThrottleEngine {
    suspend fun execute(
        request: HttpRequestBuilder,
        rule: WiretapRule,
        proceed: suspend () -> WiretapResponse,
    ): WiretapResponse
}
