/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.design.foundation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import dev.skymansandy.wiretap.design.theme.WiretapDesign

enum class StatusClass { Informational, Success, Redirect, ClientError, ServerError }

fun statusClassOf(status: Int): StatusClass = when (status / 100) {
    1 -> StatusClass.Informational
    2 -> StatusClass.Success
    3 -> StatusClass.Redirect
    4 -> StatusClass.ClientError
    else -> StatusClass.ServerError
}

@Composable
@ReadOnlyComposable
fun StatusClass.color(): Color {
    val c = WiretapDesign.colors
    return when (this) {
        StatusClass.Informational -> c.status1xx
        StatusClass.Success -> c.status2xx
        StatusClass.Redirect -> c.status3xx
        StatusClass.ClientError -> c.status4xx
        StatusClass.ServerError -> c.status5xx
    }
}

fun statusReasonFor(status: Int): String = when (status) {
    100 -> "Continue"
    101 -> "Switching Protocols"
    200 -> "OK"
    201 -> "Created"
    202 -> "Accepted"
    204 -> "No Content"
    301 -> "Moved Permanently"
    302 -> "Found"
    304 -> "Not Modified"
    400 -> "Bad Request"
    401 -> "Unauthorized"
    403 -> "Forbidden"
    404 -> "Not Found"
    409 -> "Conflict"
    422 -> "Unprocessable"
    429 -> "Too Many"
    500 -> "Server Error"
    502 -> "Bad Gateway"
    503 -> "Unavailable"
    504 -> "Gateway Timeout"
    else -> ""
}
