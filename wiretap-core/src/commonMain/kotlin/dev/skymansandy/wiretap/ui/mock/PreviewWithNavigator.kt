package dev.skymansandy.wiretap.ui.mock

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import dev.skymansandy.wiretap.navigation.api.WiretapNavigator
import dev.skymansandy.wiretap.navigation.compose.LocalWiretapNavigator

@Composable
internal fun PreviewWithNavigator(content: @Composable () -> Unit) {
    MaterialTheme {
        CompositionLocalProvider(LocalWiretapNavigator provides WiretapNavigator.NoOp) {
            content()
        }
    }
}
