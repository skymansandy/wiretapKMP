package dev.skymansandy.kurlclient

import androidx.compose.ui.window.ComposeUIViewController
import dev.skymansandy.kurlclient.db.AppDatabase
import dev.skymansandy.kurlclient.db.createDatabaseDriver
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    AppDatabase.init(createDatabaseDriver())
    return ComposeUIViewController { App() }
}