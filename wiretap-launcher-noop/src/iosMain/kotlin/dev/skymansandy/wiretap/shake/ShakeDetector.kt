@file:Suppress("UnusedParameter")

/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.shake

/**
 * No-op stand-in for the real `ShakeDetector` that `wiretap-launcher` exports from
 * `:wiretap-shake`.
 *
 * Consumers who present the console themselves call this directly, so it has to exist
 * in the noop artifact too — otherwise swapping `wiretap-launcher` for
 * `wiretap-launcher-noop` per build variant forces a separate iOS source set just to
 * hide the call. The real implementation cannot simply be depended on here: it is a
 * swiftklib/cinterop wrapper around the Swift detector, which is precisely what this
 * artifact exists to leave out.
 */
object ShakeDetector {

    fun enable(onShake: () -> Unit) = Unit
}
