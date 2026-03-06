package dev.skymansandy.kurlclient

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.skymansandy.kurlclient.db.AppDatabase
import dev.skymansandy.kurlclient.db.createDatabaseDriver

fun main() {
    AppDatabase.init(createDatabaseDriver())
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "KurlClient",
        ) {
            App()
        }
    }
}