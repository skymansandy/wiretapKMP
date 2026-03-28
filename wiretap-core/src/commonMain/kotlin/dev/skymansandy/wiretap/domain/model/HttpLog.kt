package dev.skymansandy.wiretap.domain.model

import androidx.compose.ui.graphics.Color
import dev.skymansandy.wiretap.domain.model.HttpLog.Companion.RESPONSE_CODE_IN_PROGRESS
import dev.skymansandy.wiretap.ui.theme.WiretapColors

/**
 * Represents a logged HTTP request/response pair.
 *
 * Created when a request is intercepted (with [responseCode] = [RESPONSE_CODE_IN_PROGRESS]),
 * then updated when the response arrives. Check [isInProgress] to see if the response is pending.
 *
 * @property source Indicates whether this was a real network response, a mock, or a throttled request.
 * @property matchedRuleId The ID of the [WiretapRule] that matched this request, if any.
 */
data class HttpLog(
    val id: Long = 0,
    val url: String,
    val method: String,
    val requestHeaders: Map<String, String> = emptyMap(),
    val requestBody: String? = null,
    val responseCode: Int = RESPONSE_CODE_IN_PROGRESS,
    val responseHeaders: Map<String, String> = emptyMap(),
    val responseBody: String? = null,
    val responseBodySize: Long = 0,
    val durationMs: Long = 0,
    val durationNs: Long = 0,
    val source: ResponseSource = ResponseSource.Network,
    val timestamp: Long,
    val matchedRuleId: Long? = null,
    val protocol: String? = null,
    val remoteAddress: String? = null,
    val tlsProtocol: String? = null,
    val cipherSuite: String? = null,
    val certificateCn: String? = null,
    val issuerCn: String? = null,
    val certificateExpiry: String? = null,
    val timingPhases: List<TimingPhase> = emptyList(),
) {
    val isInProgress: Boolean = responseCode == RESPONSE_CODE_IN_PROGRESS

    val statusText: String = when {
        isInProgress -> "..."
        responseCode > 0 -> responseCode.toString()
        responseCode == -1 -> "!!!"
        else -> "ERR"
    }

    val statusColor: Color = when {
        isInProgress -> WiretapColors.StatusBlue
        responseCode in 200..299 -> Color.White
        responseCode in 300..399 -> WiretapColors.StatusBlue
        responseCode in 400..499 -> WiretapColors.StatusAmber
        responseCode >= 500 -> WiretapColors.StatusRed
        else -> WiretapColors.StatusGray
    }

    companion object {
        const val RESPONSE_CODE_IN_PROGRESS = -2
    }
}
