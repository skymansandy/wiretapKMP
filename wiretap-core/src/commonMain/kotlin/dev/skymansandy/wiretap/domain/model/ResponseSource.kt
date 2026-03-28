package dev.skymansandy.wiretap.domain.model

enum class ResponseSource(val label: String) {

    Network("Network"),
    Mock("Mock"),
    Throttle("Throttle"),
    MockAndThrottle("Mock + Throttle"),
}
