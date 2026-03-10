package dev.skymansandy.wiretap.model

data class WiretapResponse(
    val statusCode: Int,
    val headers: Map<String, String> = emptyMap(),
    val body: String? = null,
    val source: ResponseSource,
    val durationMs: Long = 0,
)
