package dev.skymansandy.spektorsample.core.model

data class KurlRequest(
    val url: String,
    val method: String,
    val headers: Map<String, String> = emptyMap(),
    val queryParams: Map<String, String> = emptyMap(),
    val body: String? = null
)