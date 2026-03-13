package dev.skymansandy.wiretap.config

data class WiretapConfig(
    val enabled: Boolean = true,
    val loggingEnabled: Boolean = true,
    val maxLogEntries: Int = 500,
)
