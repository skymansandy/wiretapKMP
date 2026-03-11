package dev.skymansandy.wiretapsample

import androidx.compose.ui.window.ComposeUIViewController
import dev.skymansandy.wiretapsample.di.initKoin
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    initKoin()
    return ComposeUIViewController { App() }
}
