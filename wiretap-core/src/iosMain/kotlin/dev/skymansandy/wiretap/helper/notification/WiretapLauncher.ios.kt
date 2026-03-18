package dev.skymansandy.wiretap.helper.notification

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.window.ComposeUIViewController
import dev.skymansandy.wiretap.ui.WiretapScreen
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGPoint
import platform.UIKit.UIApplication
import platform.UIKit.UIEvent
import platform.UIKit.UIEventSubtype
import platform.UIKit.UIEventSubtypeMotionShake
import platform.UIKit.UIEventTypeMotion
import platform.UIKit.UIModalPresentationFullScreen
import platform.UIKit.UINavigationController
import platform.UIKit.UITabBarController
import platform.UIKit.UIView
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowLevelAlert
import platform.UIKit.UIWindowScene

private var wiretapViewControllerInstance: UIViewController? = null
private var shakeDetectorWindow: UIWindow? = null

fun WiretapViewController(): UIViewController {
    return ComposeUIViewController {
        MaterialTheme {
            WiretapScreen(onBack = {
                getTopMostViewController()?.dismissViewControllerAnimated(true, completion = null)
                wiretapViewControllerInstance = null
            })
        }
    }.also {
        wiretapViewControllerInstance = it
    }
}

actual fun startWiretap() {
    if (wiretapViewControllerInstance != null) return
    val topVc = getTopMostViewController() ?: return
    val wiretapVc = WiretapViewController()
    wiretapVc.setModalPresentationStyle(UIModalPresentationFullScreen)
    topVc.presentViewController(wiretapVc, animated = true, completion = null)
}

actual fun enableLaunchTool() {
    val windowScene = UIApplication.sharedApplication.connectedScenes
        .firstOrNull { it is UIWindowScene } as? UIWindowScene

    if (windowScene == null) {
        println("[Wiretap] enableLaunchTool: no window scene found, deferring...")
        platform.Foundation.NSNotificationCenter.defaultCenter.addObserverForName(
            name = "UISceneDidActivateNotification",
            `object` = null,
            queue = platform.Foundation.NSOperationQueue.mainQueue
        ) { notification ->
            val scene = notification?.`object`() as? UIWindowScene ?: return@addObserverForName
            setupShakeDetectorWindow(scene)
        }
        return
    }
    setupShakeDetectorWindow(windowScene)
}

private fun setupShakeDetectorWindow(windowScene: UIWindowScene) {
    if (shakeDetectorWindow != null) return
    println("[Wiretap] Setting up shake detector window")
    val window = ShakeDetectorWindow(windowScene).apply {
        rootViewController = UIViewController(nibName = null, bundle = null)
        windowLevel = UIWindowLevelAlert + 1
        backgroundColor = null
    }
    shakeDetectorWindow = window
    window.makeKeyAndVisible()
}

@OptIn(BetaInteropApi::class)
private class ShakeDetectorWindow @OverrideInit constructor(
    windowScene: UIWindowScene
) : UIWindow(windowScene) {

    override fun sendEvent(event: UIEvent) {
        if (event.type == UIEventTypeMotion) {
            println("[Wiretap] Motion event received! type=${event.type} subtype=${event.subtype}")
            println("[Wiretap] UIEventTypeMotion=$UIEventTypeMotion UIEventSubtypeMotionShake=$UIEventSubtypeMotionShake")
            if (event.subtype == UIEventSubtypeMotionShake) {
                println("[Wiretap] Shake detected!")
                startWiretap()
            }
        }
        super.sendEvent(event)
    }

    override fun motionEnded(motion: UIEventSubtype, withEvent: UIEvent?) {
        println("[Wiretap] motionEnded called! motion=$motion")
        super.motionEnded(motion, withEvent)
        if (motion == UIEventSubtypeMotionShake) {
            println("[Wiretap] Shake via motionEnded!")
            startWiretap()
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun hitTest(point: CValue<CGPoint>, withEvent: UIEvent?): UIView? {
        return null // Pass all touches through
    }
}

private fun getTopMostViewController(
    base: UIViewController? = UIApplication.sharedApplication.topWindow?.rootViewController
): UIViewController? {
    if (base == null) return null
    return when (base) {
        is UINavigationController -> getTopMostViewController(base.visibleViewController)
        is UITabBarController -> base.selectedViewController?.let { getTopMostViewController(it) }
        else -> {
            if (base.presentedViewController != null) {
                getTopMostViewController(base.presentedViewController)
            } else {
                base
            }
        }
    }
}

private val UIApplication.topWindow: UIWindow?
    get() {
        return connectedScenes
            .asSequence()
            .mapNotNull { it as? UIWindowScene }
            .flatMap { it.windows.asSequence() }
            .filterIsInstance<UIWindow>()
            .filter { it !is ShakeDetectorWindow }
            .lastOrNull { it.isKeyWindow() }
    }
