package dev.skymansandy.wiretapsample

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.skymansandy.wiretap.plugin.di.WiretapKtor

fun main() {
    WiretapKtor.initialize()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "WiretapSample",
        ) {
            App()
        }
    }
}
