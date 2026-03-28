package dev.skymansandy.wiretapsample.ui.theme

import androidx.compose.ui.graphics.Color
import dev.skymansandy.wiretapsample.model.ActionCategory

internal val ColorSuccess = Color(0xFF66BB6A)
internal val ColorRedirect = Color(0xFF42A5F5)
internal val ColorClientError = Color(0xFFFFA726)
internal val ColorServerError = Color(0xFFEF5350)
internal val ColorTimeout = Color(0xFF9E9E9E)
internal val ColorCancel = Color(0xFF9E9E9E)
internal val ColorBatch = Color(0xFF26A69A)
internal val ColorWsSent = Color(0xFF7E57C2)

val actionColor = mapOf(
    ActionCategory.Success to ColorSuccess,
    ActionCategory.Redirect to ColorRedirect,
    ActionCategory.ClientError to ColorClientError,
    ActionCategory.ServerError to ColorServerError,
    ActionCategory.Timeout to ColorTimeout,
    ActionCategory.Cancel to ColorCancel,
    ActionCategory.Batch to ColorBatch,
)
