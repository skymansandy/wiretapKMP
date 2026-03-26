package dev.skymansandy.wiretap.ui.navigation

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import dev.skymansandy.wiretap.ui.navigation.WiretapNavigator.Companion.NoOp

@Composable
internal fun PreviewWithNavigator(content: @Composable () -> Unit) {
    MaterialTheme {
        CompositionLocalProvider(LocalWiretapNavigator provides NoOp) {
            content()
        }
    }
}
