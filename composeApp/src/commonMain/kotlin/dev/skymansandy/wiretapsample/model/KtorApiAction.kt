package dev.skymansandy.wiretapsample.model

import io.ktor.client.HttpClient

data class KtorApiAction(
    val label: String,
    val category: ActionCategory,
    val action: suspend (HttpClient, (String) -> Unit) -> Unit,
)
