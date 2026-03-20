package dev.skymansandy.wiretapsample.model

internal data class WsLogEntry(
    val type: WsMsgType,
    val text: String,
) {
    enum class WsMsgType {
        Sent,
        Recv,
        Sys,
    }
}