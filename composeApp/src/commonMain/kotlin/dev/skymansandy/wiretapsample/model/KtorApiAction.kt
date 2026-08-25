package dev.skymansandy.wiretapsample.model

import dev.skymansandy.wiretapsample.api.ExternalApi
import dev.skymansandy.wiretapsample.api.HttpBinApi
import dev.skymansandy.wiretapsample.api.HttpBingoApi
import dev.skymansandy.wiretapsample.api.JsonPlaceholderApi
import io.ktor.client.HttpClient

data class KtorfitApis(
    val jsonPlaceholder: JsonPlaceholderApi,
    val httpBin: HttpBinApi,
    val httpBinHttp: HttpBinApi,
    val httpBingo: HttpBingoApi,
    val external: ExternalApi,
    val client: HttpClient,
)

data class KtorApiAction(
    val label: String,
    val category: ActionCategory,
    val action: suspend (KtorfitApis, (String) -> Unit) -> Unit,
)
