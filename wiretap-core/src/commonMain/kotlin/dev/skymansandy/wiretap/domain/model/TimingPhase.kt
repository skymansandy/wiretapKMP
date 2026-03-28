package dev.skymansandy.wiretap.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class TimingPhase(
    val name: String,
    val startMs: Double,
    val durationMs: Double,
)
