package dev.skymansandy.wiretapsample.api

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.HTTP
import de.jensklingenberg.ktorfit.http.Headers
import io.ktor.client.statement.HttpResponse

/**
 * httpbingo.org — the Go rewrite of httpbin. Unlike httpbin.org, it echoes requests made with
 * custom verbs instead of rejecting them with 405.
 */
interface HttpBingoApi {

    /**
     * QUERY (RFC 10008) — a safe, idempotent method that carries its query in the request body.
     * Ktorfit has no verb annotation for it, so it goes through the custom-verb [HTTP] annotation.
     */
    @Headers("Content-Type: application/json")
    @HTTP(method = "QUERY", path = "anything", hasBody = true)
    suspend fun queryAnything(@Body body: String): HttpResponse
}
