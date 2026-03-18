package dev.skymansandy.wiretap.domain.model

enum class SocketStatus {
    CONNECTING,
    OPEN,
    CLOSING,
    CLOSED,
    FAILED,
}

enum class SocketMessageDirection {
    SENT,
    RECEIVED,
}

enum class SocketContentType {
    TEXT,
    BINARY,
}
