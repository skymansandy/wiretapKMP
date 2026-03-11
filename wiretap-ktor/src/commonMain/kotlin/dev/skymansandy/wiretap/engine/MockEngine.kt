package dev.skymansandy.wiretap.engine

import dev.skymansandy.wiretap.model.WiretapResponse
import dev.skymansandy.wiretap.model.WiretapRule
import io.ktor.client.request.*

interface MockEngine {
    suspend fun execute(request: HttpRequestBuilder, rule: WiretapRule): WiretapResponse
}
