# Shake to Launch

Wiretap includes a built-in launcher that opens the inspector UI with a simple gesture. Call `enableWiretapLauncher()` once during app startup to activate it.

## Platform Triggers

| Platform | Trigger | Details |
|----------|---------|---------|
| **Android** | Shake gesture | Uses the accelerometer sensor via `SensorManager`. Registers as a `DefaultLifecycleObserver` — listens only while the app is in the foreground. |
| **iOS** | Shake gesture | Uses the `wiretap-shake` module which overrides `UIWindow.motionEnded` to detect `.motionShake` events. |
| **JVM Desktop** | ++ctrl+shift+d++ | Registers a global `KeyEventDispatcher` for the `Ctrl+Shift+D` shortcut. |

## Setup

=== "Android"

    ```kotlin
    class MyApplication : Application() {
        override fun onCreate() {
            super.onCreate()
            enableWiretapLauncher()
        }
    }
    ```

    No permissions are required. The accelerometer sensor is used for shake detection, and the listener is automatically paused/resumed with the app lifecycle.

=== "iOS (KMP)"

    ```kotlin
    fun MainViewController(): UIViewController {
        enableWiretapLauncher()
        // ...
    }
    ```

=== "iOS (Swift)"

    ```swift
    import WiretapKit

    @main
    struct MyApp: App {
        init() {
            WiretapLauncherKt.enableWiretapLauncher()
        }
    }
    ```

=== "JVM Desktop"

    ```kotlin
    fun main() = application {
        enableWiretapLauncher()
        // ...
    }
    ```

## How It Works

### Android

`enableWiretapLauncher()` adds a `ShakeGestureListener` as a `ProcessLifecycleOwner` observer. The listener monitors the accelerometer for sudden acceleration changes exceeding a threshold. When detected, it calls `startWiretap()` which launches `WiretapConsoleActivity`.

### iOS

`enableWiretapLauncher()` calls `ShakeDetector.enable()` from the `wiretap-shake` module. This bridges to a Swift `WiretapShakeDetector` class (compiled via swiftklib) that overrides `UIWindow.motionEnded(_:with:)`. When a shake motion is detected, the callback presents the Wiretap Compose UI as a full-screen modal `UIViewController`.

### JVM Desktop

`enableWiretapLauncher()` registers a `KeyEventDispatcher` on the `KeyboardFocusManager`. Pressing `Ctrl+Shift+D` calls `startWiretap()`, which opens a Swing `JFrame` containing the Wiretap Compose UI panel.

## Manual Launch

You can also launch the inspector programmatically at any time:

```kotlin
startWiretap()
```

This is useful if you want to trigger the inspector from a debug menu or button instead of (or in addition to) the shake gesture.
