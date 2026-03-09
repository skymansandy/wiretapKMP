package dev.skymansandy.wiretapsample

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.skymansandy.wiretapsample.di.initKoin

fun main() {
    initKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "WiretapSample",
        ) {
            App()
        }
    }
}
