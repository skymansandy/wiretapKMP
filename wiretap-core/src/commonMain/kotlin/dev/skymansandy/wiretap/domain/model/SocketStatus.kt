package dev.skymansandy.wiretap.domain.model

enum class SocketStatus {
    Connecting,
    Open,
    Closing,
    Closed,
    Failed,
}

enum class SocketMessageType {
    Sent,
    Received,
}

enum class SocketContentType {
    Text,
    Binary,
}
