/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.launcher

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class ShakeAccelerationFilterTest : DescribeSpec({

    // Timestamps must be > cooldownMs above lastShakeTimestamp=0 for the first
    // shake to register, mirroring how System.currentTimeMillis() is always
    // large in practice. Use a base in the seconds range.
    val t0 = 10_000L

    describe("idle gravity") {
        it("does not trigger when samples sit at Earth gravity") {
            val filter = ShakeAccelerationFilter()
            val gravity = ShakeAccelerationFilter.EARTH_GRAVITY

            repeat(50) { i ->
                filter.onSample(x = 0f, y = 0f, z = gravity, nowMs = t0 + i * 20L) shouldBe false
            }
        }
    }

    describe("strong jolt") {
        it("triggers on a single large delta past the threshold") {
            val filter = ShakeAccelerationFilter()
            filter.onSample(0f, 0f, 9.8f, nowMs = t0)
            filter.onSample(0f, 0f, 9.8f, nowMs = t0 + 20)

            val triggered = filter.onSample(50f, 50f, 50f, nowMs = t0 + 40)

            triggered shouldBe true
        }
    }

    describe("cooldown") {
        it("debounces a second shake that lands inside the cooldown window") {
            val filter = ShakeAccelerationFilter(cooldownMs = 2000L)
            filter.onSample(0f, 0f, 9.8f, nowMs = t0)
            filter.onSample(0f, 0f, 9.8f, nowMs = t0 + 20)
            filter.onSample(50f, 50f, 50f, nowMs = t0 + 40) shouldBe true

            // Same magnitude jolt 100ms later — well inside the 2000ms cooldown.
            val second = filter.onSample(50f, 50f, 50f, nowMs = t0 + 140)

            second shouldBe false
        }

        it("re-triggers once the cooldown elapses") {
            val filter = ShakeAccelerationFilter(cooldownMs = 100L)
            filter.onSample(0f, 0f, 9.8f, nowMs = t0)
            filter.onSample(0f, 0f, 9.8f, nowMs = t0 + 20)
            filter.onSample(50f, 50f, 50f, nowMs = t0 + 40) shouldBe true

            // Settle, then jolt again after the cooldown window.
            filter.onSample(0f, 0f, 9.8f, nowMs = t0 + 200)
            val second = filter.onSample(80f, 80f, 80f, nowMs = t0 + 300)

            second shouldBe true
        }
    }

    describe("sub-threshold motion") {
        it("ignores gentle wiggles") {
            val filter = ShakeAccelerationFilter()
            repeat(20) { i ->
                val v = 9.8f + if (i % 2 == 0) 0.5f else -0.5f
                filter.onSample(v, v, v, nowMs = t0 + i * 20L) shouldBe false
            }
        }
    }

    describe("custom threshold") {
        it("respects a lower threshold for easier triggering") {
            val filter = ShakeAccelerationFilter(thresholdAcceleration = 10f)
            filter.onSample(0f, 0f, 9.8f, nowMs = t0)
            filter.onSample(0f, 0f, 9.8f, nowMs = t0 + 20)

            // Same jolt that would be sub-threshold with the default 20f would
            // cross a custom 10f threshold.
            val triggered = filter.onSample(15f, 15f, 15f, nowMs = t0 + 40)

            triggered shouldBe true
        }
    }
})
