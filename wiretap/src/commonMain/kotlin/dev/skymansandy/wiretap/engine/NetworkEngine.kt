package dev.skymansandy.wiretap.engine

import dev.skymansandy.wiretap.model.WiretapResponse
import io.ktor.client.request.*

interface NetworkEngine {
    suspend fun execute(request: HttpRequestBuilder): WiretapResponse
}
