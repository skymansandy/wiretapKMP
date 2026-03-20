package dev.skymansandy.wiretapsample.model

internal enum class ActionCategory {
    Success,
    Redirect,
    ClientError,
    ServerError,
    Timeout,
    Cancel,
}
