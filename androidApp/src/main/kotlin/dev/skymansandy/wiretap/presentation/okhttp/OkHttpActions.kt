package dev.skymansandy.wiretap.presentation.okhttp

import dev.skymansandy.wiretap.okhttp.WiretapOkHttpInterceptor
import dev.skymansandy.wiretapsample.model.ActionCategory
import dev.skymansandy.wiretapsample.model.HttpMethod
import dev.skymansandy.wiretapsample.model.HttpTestCase
import dev.skymansandy.wiretapsample.model.httpTestCases
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.util.concurrent.TimeUnit

internal fun createOkHttpClient(): OkHttpClient =
    OkHttpClient.Builder()
        .addInterceptor(WiretapOkHttpInterceptor())
        .build()

internal data class OkHttpApiAction(
    val label: String,
    val category: ActionCategory,
    val action: (OkHttpClient, (String) -> Unit) -> Unit,
)

private fun Response.format(): String =
    buildString {
        appendLine("HTTP $code")
        appendLine(headers)
        appendLine()
        append(body.string())
    }

internal val okHttpActions: List<OkHttpApiAction> = httpTestCases.map { case ->
    OkHttpApiAction(case.label, case.category) { client, onStatus ->
        onStatus("${case.statusPrefix} ...")
        when (case) {
            is HttpTestCase.Request -> {
                val request = Request.Builder().url(case.url).apply {
                    when (case.method) {
                        HttpMethod.GET -> get()
                        HttpMethod.POST -> post(
                            case.body!!.toRequestBody(case.contentType!!.toMediaType()),
                        )
                    }
                }.build()
                onStatus(client.newCall(request).execute().format())
            }

            is HttpTestCase.Timeout -> {
                val timeoutClient = client.newBuilder()
                    .callTimeout(case.timeoutMs, TimeUnit.MILLISECONDS)
                    .readTimeout(case.timeoutMs, TimeUnit.MILLISECONDS)
                    .build()
                val request = Request.Builder()
                    .url(case.url)
                    .build()
                timeoutClient.newCall(request).execute()
                onStatus("Unexpected success")
            }

            is HttpTestCase.Cancel -> {
                val request = Request.Builder()
                    .url(case.url)
                    .build()
                val call = client.newCall(request)
                val cancelThread = Thread {
                    Thread.sleep(case.cancelAfterMs)
                    call.cancel()
                }.apply { isDaemon = true }
                cancelThread.start()
                try {
                    call.execute()
                } catch (_: Exception) {
                    // expected cancellation
                }
                onStatus("Request cancelled!")
            }
        }
    }
}
