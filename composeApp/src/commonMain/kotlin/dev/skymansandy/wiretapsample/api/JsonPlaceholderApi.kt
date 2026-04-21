package dev.skymansandy.wiretapsample.api

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import dev.skymansandy.wiretapsample.model.Post
import io.ktor.client.statement.HttpResponse

interface JsonPlaceholderApi {

    @GET("posts/{id}")
    suspend fun getPost(@Path("id") id: Int): Post

    @GET("posts/{id}")
    suspend fun getPostRaw(@Path("id") id: Int): HttpResponse

    @GET("posts/{id}/comments")
    suspend fun getComments(@Path("id") id: Int): HttpResponse

    @Headers("Content-Type: application/json")
    @POST("posts")
    suspend fun createPost(@Body body: String): HttpResponse
}
