package dev.skymansandy.wiretap.ui.common

import androidx.compose.runtime.Composable

@Composable
internal actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // No system back button on iOS
}
