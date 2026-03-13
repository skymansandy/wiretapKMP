package dev.skymansandy.wiretap.engine

import dev.skymansandy.wiretap.data.db.entity.WiretapRule
import dev.skymansandy.wiretap.domain.model.WiretapResponse
import io.ktor.client.request.*

interface ThrottleEngine {
    suspend fun execute(
        request: HttpRequestBuilder,
        rule: WiretapRule,
        proceed: suspend () -> WiretapResponse,
    ): WiretapResponse
}
