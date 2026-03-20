package dev.skymansandy.wiretap.shake

import kotlinx.cinterop.ExperimentalForeignApi
import dev.skymansandy.wiretap.shake.WiretapShakeDetector as SwiftShakeDetector

object ShakeDetector {

    @OptIn(ExperimentalForeignApi::class)
    fun enable(onShake: () -> Unit) {
        SwiftShakeDetector().enableShakeDetectorWithCallback(onShake)
    }
}
