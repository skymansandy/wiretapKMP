package dev.skymansandy.wiretap.helper.notification

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.window.ComposeUIViewController
import dev.skymansandy.wiretap.ui.WiretapScreen
import platform.UIKit.UIApplication
import platform.UIKit.UIModalPresentationFullScreen
import platform.UIKit.UIViewController

fun WiretapViewController(): UIViewController {
    return ComposeUIViewController {
        MaterialTheme {
            WiretapScreen(onBack = {
                UIApplication.sharedApplication.keyWindow?.rootViewController
                    ?.dismissViewControllerAnimated(true, completion = null)
            })
        }
    }
}

actual fun startWiretap() {
    val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController ?: return
    val wiretapVc = WiretapViewController()
    wiretapVc.setModalPresentationStyle(UIModalPresentationFullScreen)
    rootViewController.presentViewController(wiretapVc, animated = true, completion = null)
}
