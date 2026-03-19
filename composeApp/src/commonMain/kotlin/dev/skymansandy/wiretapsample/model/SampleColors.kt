package dev.skymansandy.wiretapsample.model

import androidx.compose.ui.graphics.Color

internal val ColorSuccess = Color(0xFF4CAF50)
internal val ColorRedirect = Color(0xFF42A5F5)
internal val ColorClientError = Color(0xFFFFA726)
internal val ColorServerError = Color(0xFFEF5350)
internal val ColorEdgeCase = Color(0xFF9E9E9E)

internal val actionColor = mapOf(
    ActionCategory.SUCCESS to ColorSuccess,
    ActionCategory.REDIRECT to ColorRedirect,
    ActionCategory.CLIENT_ERROR to ColorClientError,
    ActionCategory.SERVER_ERROR to ColorServerError,
    ActionCategory.EDGE_CASE to ColorEdgeCase,
)
