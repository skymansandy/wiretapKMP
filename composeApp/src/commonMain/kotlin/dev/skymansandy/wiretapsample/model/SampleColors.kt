package dev.skymansandy.wiretapsample.model

import androidx.compose.ui.graphics.Color

internal val ColorSuccess = Color(0xFF4CAF50)
internal val ColorRedirect = Color(0xFF42A5F5)
internal val ColorClientError = Color(0xFFFFA726)
internal val ColorServerError = Color(0xFFEF5350)
internal val ColorTimeout = Color(0xFF9E9E9E)
internal val ColorCancel = Color(0xFF9E9E9E)

internal val actionColor = mapOf(
    ActionCategory.Success to ColorSuccess,
    ActionCategory.Redirect to ColorRedirect,
    ActionCategory.ClientError to ColorClientError,
    ActionCategory.ServerError to ColorServerError,
    ActionCategory.Timeout to ColorTimeout,
    ActionCategory.Cancel to ColorCancel,
)
