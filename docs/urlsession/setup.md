# URLSession — Setup

**Platforms:** iOS (iosArm64 + iosSimulatorArm64)

## Dependencies

### Swift Package Manager

Add the WiretapKMP SPM package via its GitHub repository URL, then link the `WiretapURLSession` framework.

=== "Xcode"

    1. **File → Add Package Dependencies…**
    2. Enter the repository URL:
       ```
       https://github.com/skymansandy/wiretapKMP.git
       ```
    3. Set the version rule (e.g. **Up to Next Major** from `1.0.0-RC2`)
    4. Add the `WiretapURLSession` library to your target

=== "Package.swift"

    ```swift
    dependencies: [
        .package(
            url: "https://github.com/skymansandy/wiretapKMP.git",
            from: "1.0.0-RC2"
        )
    ]
    ```
    Then add the dependency to your target:
    ```swift
    .target(
        name: "MyApp",
        dependencies: [
            .product(name: "WiretapURLSession", package: "wiretapKMP")
        ]
    )
    ```

=== "XcodeGen"

    ```yaml
    packages:
      WiretapURLSession:
        url: https://github.com/skymansandy/wiretapKMP.git
        from: 1.0.0-RC2

    targets:
      MyApp:
        dependencies:
          - package: WiretapURLSession
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
