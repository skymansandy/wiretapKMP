# Multi-Module Setup

## The Problem

In apps with multiple Gradle modules (e.g., `app` + `feature`), using `debugImplementation` in the app module doesn't expose Wiretap classes to feature modules. Feature modules that create `HttpClient` or `OkHttpClient` instances can't reference `WiretapKtorHttpPlugin` or `WiretapOkHttpInterceptor`.

## The Solution

Wiretap provides lightweight **API modules** that feature modules can depend on via regular `implementation`. These modules define the public API surface (plugin vals, interceptor classes) but are no-op by default. When the full plugin module is on the classpath (debug builds), real behavior activates automatically via Koin-based delegation.

## Ktor Multi-Module

```kotlin
// feature/build.gradle.kts
dependencies {
    implementation("dev.skymansandy:wiretap-ktor-api:1.0.0-RC7")
}

// app/build.gradle.kts
dependencies {
    debugImplementation("dev.skymansandy:wiretap-ktor:1.0.0-RC7")
    releaseImplementation("dev.skymansandy:wiretap-ktor-noop:1.0.0-RC7")
}
```

Feature modules use `WiretapKtorHttpPlugin` and `WiretapKtorWebSocketPlugin` as normal:

```kotlin
// feature module
val client = HttpClient {
    install(WiretapKtorHttpPlugin)
}
```

In debug builds, `wiretap-ktor` registers real delegates via Koin — full logging and rules activate. In release builds, only `wiretap-ktor-noop` (which re-exports `wiretap-ktor-api`) is present — everything is a no-op.

## OkHttp Multi-Module

```kotlin
// feature/build.gradle.kts
dependencies {
    implementation("dev.skymansandy:wiretap-okhttp-api:1.0.0-RC7")
}

// app/build.gradle.kts
dependencies {
    debugImplementation("dev.skymansandy:wiretap-okhttp:1.0.0-RC7")
    releaseImplementation("dev.skymansandy:wiretap-okhttp-noop:1.0.0-RC7")
}
```

Feature modules use `WiretapOkHttpInterceptor` and `WiretapOkHttpWebSocketListener` as normal:

```kotlin
// feature module
val client = OkHttpClient.Builder()
    .addInterceptor(WiretapOkHttpInterceptor())
    .build()
```

## Platform-Specific Registration

### Android

No extra setup needed. The Ktor and OkHttp modules register their Koin delegates automatically via App Startup initializers.

### JVM Desktop

Call the initializer manually at app startup:

=== "Ktor"

    ```kotlin
    fun main() {
        WiretapKtor.initialize()
        // ... rest of app setup
    }
    ```

=== "OkHttp"

    ```kotlin
    fun main() {
        WiretapOkHttp.initialize()
        // ... rest of app setup
    }
    ```

## Single-Module Apps

Existing single-module consumers don't need to change anything. The `wiretap-ktor` and `wiretap-okhttp` modules still work exactly as before — they transitively include the API module and register delegates automatically.
