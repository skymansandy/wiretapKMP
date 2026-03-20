# URLSession — Setup

**Platforms:** iOS (iosArm64 + iosSimulatorArm64)

## Dependencies

### Swift Package Manager

Add the WiretapKMP SPM package dependency, then link the `WiretapURLSession` framework.

For release builds, swap to `wiretap-urlsession-noop` which exports the same `WiretapURLSession` framework with pass-through behavior.

### XcodeGen Example

```yaml
packages:
  WiretapURLSession:
    path: path/to/WiretapKMP

targets:
  MyApp:
    dependencies:
      - package: WiretapURLSession
    preBuildScripts:
      - name: "Compile Kotlin Frameworks"
        script: |
          cd "$SRCROOT/path/to/WiretapKMP"
          ./gradlew :wiretap-urlsession:spmDevBuild \
            -PspmBuildTargets=ios_simulator_arm64
```

!!! warning "Linker Flag Required"
    Add `-lsqlite3` to **Other Linker Flags** in your Xcode target's Build Settings. WiretapKMP uses SQLite for log storage, and the iOS framework requires this system library to be linked.

## Create the Interceptor

```swift
import WiretapURLSession

let interceptor = WiretapURLSessionInterceptor(session: .shared) { config in
    config.enabled = true
    config.shouldLog = { url, method in
        KotlinBoolean(value: url.contains("/api/"))
    }
    config.headerAction = { key in
        if key.caseInsensitiveCompare("Authorization") == .orderedSame {
            return HeaderAction.Mask(mask: "***")
        }
        if key.caseInsensitiveCompare("Cookie") == .orderedSame {
            return HeaderAction.Skip.shared
        }
        return HeaderAction.Keep.shared
    }
    config.logRetention = LogRetention.Days(days: 7)
}
```

## Enable the Inspector UI

```swift
@main
struct MyApp: App {
    init() {
        WiretapLauncher_iosKt.enableLaunchTool()
    }

    var body: some Scene {
        WindowGroup { ContentView() }
    }
}
```

## Configuration Reference

| Property | Swift Type | Default | Description |
|----------|-----------|---------|-------------|
| `enabled` | `Bool` | `true` | Master switch — `false` disables all logging |
| `shouldLog` | `(String, String) -> KotlinBoolean` | logs all | Filter which requests to capture |
| `headerAction` | `(String) -> HeaderAction` | `Keep` | Control how headers are logged |
| `logRetention` | `LogRetention` | `Forever` | How long to keep log entries |

### `enabled`

```swift
config.enabled = false  // Disable entirely — requests pass through
```

### `shouldLog`

```swift
config.shouldLog = { url, method in
    KotlinBoolean(value: url.contains("/api/"))
}
```

!!! note "Swift Type Bridging"
    `shouldLog` returns `KotlinBoolean` instead of Swift `Bool` due to Kotlin/Native interop. Use `KotlinBoolean(value: true/false)`.

### `headerAction`

```swift
config.headerAction = { key in
    if key.caseInsensitiveCompare("Authorization") == .orderedSame {
        return HeaderAction.Mask(mask: "***")
    }
    if key.caseInsensitiveCompare("Cookie") == .orderedSame {
        return HeaderAction.Skip.shared
    }
    return HeaderAction.Keep.shared
}
```

!!! note "Swift Singletons"
    `Keep` and `Skip` are Kotlin objects — access them via `.shared` in Swift. `Mask` is a data class and is constructed normally.

### `logRetention`

```swift
config.logRetention = LogRetention.Forever.shared
config.logRetention = LogRetention.AppSession.shared
config.logRetention = LogRetention.Days(days: 7)
```
