package dev.skymansandy.wiretap.presentation.okhttp

import dev.skymansandy.wiretap.okhttp.WiretapOkHttpInterceptor
import dev.skymansandy.wiretapsample.model.ActionCategory
import dev.skymansandy.wiretapsample.model.HttpMethod
import dev.skymansandy.wiretapsample.model.HttpTestCase
import dev.skymansandy.wiretapsample.model.httpTestCases
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.milliseconds

internal fun createOkHttpClient(): OkHttpClient =
    OkHttpClient.Builder()
        .addInterceptor(WiretapOkHttpInterceptor())
        .build()

internal data class OkHttpApiAction(
    val label: String,
    val category: ActionCategory,
    val action: suspend (OkHttpClient, (String) -> Unit) -> Unit,
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
                coroutineScope {
                    launch {
                        delay(case.cancelAfterMs)
                        call.cancel()
                    }
                    launch(Dispatchers.IO) {
                        try {
                            call.execute()
                        } catch (_: Exception) {
                            // expected cancellation
                        }
                    }
                }
                onStatus("Request cancelled!")
            }

            is HttpTestCase.Burst -> {
                coroutineScope {
                    for (i in 1..case.count) {
                        launch(Dispatchers.IO) {
                            val request = Request.Builder()
                                .url("${case.url}$i")
                                .build()
                            try {
                                val response = client.newCall(request).execute()
                                onStatus("Burst $i/${case.count}: HTTP ${response.code}")
                            } catch (_: Exception) {
                                // ignored
                            }
                        }
                        if (i < case.count) delay(case.intervalMs)
                    }
                }
            }

            is HttpTestCase.RapidCancel -> {
                coroutineScope {
                    var previousCall: okhttp3.Call? = null
                    for (i in 1..case.count) {
                        delay(10.milliseconds)
                        previousCall?.cancel()
                        val request = Request.Builder()
                            .url("${case.url}$i")
                            .build()
                        val call = client.newCall(request)
                        previousCall = call
                        launch(Dispatchers.IO) {
                            try {
                                val response = call.execute()
                                onStatus("Request $i/${case.count}: HTTP ${response.code}")
                            } catch (_: Exception) {
                                // expected cancellation
                            }
                        }
                    }
                }
            }
        }
    }
}
