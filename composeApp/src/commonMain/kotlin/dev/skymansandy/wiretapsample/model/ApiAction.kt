package dev.skymansandy.wiretapsample.model

import io.ktor.client.HttpClient

internal data class ApiAction(
    val label: String,
    val category: ActionCategory,
    val action: suspend (HttpClient, (String) -> Unit) -> Unit,
)
