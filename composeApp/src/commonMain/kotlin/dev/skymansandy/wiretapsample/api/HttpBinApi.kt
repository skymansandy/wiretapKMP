package dev.skymansandy.wiretapsample.api

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Header
import de.jensklingenberg.ktorfit.http.HeaderMap
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import io.ktor.client.statement.HttpResponse

interface HttpBinApi {

    @GET("get")
    suspend fun get(): HttpResponse

    @GET("headers")
    suspend fun getHeaders(@HeaderMap headers: Map<String, String>): HttpResponse

    @Headers("Content-Type: application/json")
    @POST("anything")
    suspend fun postAnything(
        @Body body: String,
        @HeaderMap headers: Map<String, String>,
    ): HttpResponse

    @GET("status/{code}")
    suspend fun getStatus(@Path("code") code: Int): HttpResponse

    @GET("redirect/{count}")
    suspend fun redirect(@Path("count") count: Int): HttpResponse

    @GET("delay/{seconds}")
    suspend fun delay(@Path("seconds") seconds: Int): HttpResponse
}
