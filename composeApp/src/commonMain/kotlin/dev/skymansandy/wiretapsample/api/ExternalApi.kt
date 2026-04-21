package dev.skymansandy.wiretapsample.api

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Url
import io.ktor.client.statement.HttpResponse

interface ExternalApi {

    @GET
    suspend fun getByUrl(@Url url: String): HttpResponse
}
