/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.launcher

import kotlin.math.sqrt

/**
 * Pure shake-detection state machine: feed in `(x, y, z, nowMs)` samples
 * from an accelerometer; emits `true` when a shake gesture is detected.
 *
 * Stateful — keep a single instance per sensor listener.
 */
internal class ShakeAccelerationFilter(
    private val thresholdAcceleration: Float = DEFAULT_THRESHOLD,
    private val cooldownMs: Long = DEFAULT_COOLDOWN_MS,
    initialGravity: Float = EARTH_GRAVITY,
) {

    private var activeAcceleration: Float = INITIAL_ACTIVE_ACCELERATION
    private var currentAcceleration: Float = initialGravity
    private var lastAcceleration: Float = initialGravity
    private var lastShakeTimestamp: Long = 0L

    fun onSample(x: Float, y: Float, z: Float, nowMs: Long): Boolean {
        lastAcceleration = currentAcceleration
        currentAcceleration = sqrt(x * x + y * y + z * z)
        val delta = currentAcceleration - lastAcceleration
        activeAcceleration = activeAcceleration * SMOOTHING_DECAY + delta

        val isShake = activeAcceleration > thresholdAcceleration &&
            nowMs - lastShakeTimestamp > cooldownMs
        if (isShake) {
            lastShakeTimestamp = nowMs
        }
        return isShake
    }

    companion object {

        const val DEFAULT_THRESHOLD: Float = 20f
        const val DEFAULT_COOLDOWN_MS: Long = 2000L
        const val EARTH_GRAVITY: Float = 9.80665f
        const val SMOOTHING_DECAY: Float = 0.9f
        const val INITIAL_ACTIVE_ACCELERATION: Float = 10f
    }
}
