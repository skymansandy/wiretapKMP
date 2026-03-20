# Installation

## Ktor (Android, iOS, JVM)

### Gradle (KMP / Android)

Add the dependencies to your module's `build.gradle.kts`:

```kotlin
// Debug — full inspection
debugImplementation("dev.skymansandy:wiretap-core:$wiretapVersion")
debugImplementation("dev.skymansandy:wiretap-ktor:$wiretapVersion")

// Release — no-op stubs (zero overhead)
releaseImplementation("dev.skymansandy:wiretap-ktor-noop:$wiretapVersion")
```

For **KMP shared modules**, use `commonMain` source set:

```kotlin
sourceSets {
    commonMain {
        dependencies {
            implementation("dev.skymansandy:wiretap-core:$wiretapVersion")
            implementation("dev.skymansandy:wiretap-ktor:$wiretapVersion")
        }
    }
}
```

### iOS (SPM)

WiretapKMP publishes iOS frameworks via Swift Package Manager (KMMBridge):

1. Add the SPM package dependency pointing to the WiretapKMP repository
2. Link `WiretapKit` (core) and `WiretapKtor` (or `WiretapURLSession`) frameworks

---

## OkHttp (Android, JVM)

```kotlin
// Debug
debugImplementation("dev.skymansandy:wiretap-core:$wiretapVersion")
debugImplementation("dev.skymansandy:wiretap-okhttp:$wiretapVersion")

// Release
releaseImplementation("dev.skymansandy:wiretap-okhttp-noop:$wiretapVersion")
```

---

## URLSession (iOS via SPM)

For native Swift/UIKit/SwiftUI projects:

1. Add the SPM package dependency
2. Link `WiretapURLSession` framework (includes `WiretapKit` core)

For release builds, swap to `wiretap-urlsession-noop` which exports the same `WiretapURLSession` framework with pass-through behavior.

---

## Requirements

| Platform | Minimum Version |
|----------|----------------|
| Android  | API 24 (Android 7.0) |
| iOS      | arm64 + Simulator arm64 |
| JVM      | Java 8+ |

### Key Dependencies

| Dependency | Version |
|-----------|---------|
| Kotlin    | 2.3.10  |
| Ktor      | 3.0.0   |
| OkHttp    | 4.12.0  |
| SQLDelight | 2.0.2  |
| Koin      | 4.0.2   |
| Compose Multiplatform | 1.10.2 |
