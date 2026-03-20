package dev.skymansandy.wiretapsample.ui.theme

import androidx.compose.ui.graphics.Color
import dev.skymansandy.wiretapsample.model.ActionCategory

val ColorSuccess = Color(0xFF2E7D32)
val ColorRedirect = Color(0xFF1565C0)
val ColorClientError = Color(0xFFE65100)
val ColorServerError = Color(0xFFC62828)
val ColorTimeout = Color(0xFF616161)
val ColorCancel = Color(0xFF616161)
val ColorWsSent = Color(0xFF4527A0)

val actionColor = mapOf(
    ActionCategory.Success to ColorSuccess,
    ActionCategory.Redirect to ColorRedirect,
    ActionCategory.ClientError to ColorClientError,
    ActionCategory.ServerError to ColorServerError,
    ActionCategory.Timeout to ColorTimeout,
    ActionCategory.Cancel to ColorCancel,
)
