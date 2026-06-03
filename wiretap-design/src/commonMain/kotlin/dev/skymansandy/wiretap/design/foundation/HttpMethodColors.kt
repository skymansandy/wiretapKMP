/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.design.foundation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import dev.skymansandy.wiretap.design.theme.WiretapDesign

enum class HttpMethod { GET, POST, PUT, PATCH, DELETE, ANY, WS, SSE }

@Composable
@ReadOnlyComposable
fun HttpMethod.color(): Color {
    val c = WiretapDesign.colors
    return when (this) {
        HttpMethod.GET -> c.methodGet
        HttpMethod.POST -> c.methodPost
        HttpMethod.PUT -> c.methodPut
        HttpMethod.PATCH -> c.methodPatch
        HttpMethod.DELETE -> c.methodDelete
        HttpMethod.ANY -> c.methodAny
        HttpMethod.WS -> c.methodWs
        HttpMethod.SSE -> c.methodSse
    }
}

fun HttpMethod.label(): String = name

fun httpMethodOrNull(raw: String): HttpMethod? =
    runCatching { HttpMethod.valueOf(raw.trim().uppercase()) }.getOrNull()
