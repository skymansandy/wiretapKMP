package dev.skymansandy.wiretap.ui.mock

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import dev.skymansandy.wiretap.navigation.LocalWiretapNavigator
import dev.skymansandy.wiretap.navigation.WiretapNavigator

@Composable
internal fun PreviewWithNavigator(content: @Composable () -> Unit) {
    MaterialTheme {
        CompositionLocalProvider(LocalWiretapNavigator provides WiretapNavigator.Companion.NoOp) {
            content()
        }
    }
}
