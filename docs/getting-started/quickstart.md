# Quick Start

## 1. Initialize Koin

Wiretap uses Koin for dependency injection. Initialize it once at app startup:

=== "Android"

    ```kotlin
    class MyApplication : Application() {
        override fun onCreate() {
            super.onCreate()
            // WiretapInitializer runs automatically via AndroidX App Startup
            // No manual init needed — context is captured automatically
        }
    }
    ```

    !!! note
        On Android, `WiretapInitializer` (AndroidX App Startup) automatically captures the application context, creates a notification channel, and adds a launcher shortcut. No manual setup required.

=== "iOS (Swift)"

    ```swift
    @main
    struct MyApp: App {
        init() {
            // Enable the Wiretap overlay launcher
            WiretapLauncher_iosKt.enableLaunchTool()
        }
    }
    ```

=== "JVM Desktop"

    ```kotlin
    fun main() = application {
        // Koin is initialized lazily on first plugin use
        Window(onCloseRequest = ::exitApplication) {
            App()
        }
    }
    ```

## 2. Install the Plugin

=== "Ktor"

    ```kotlin
    val client = HttpClient {
        install(WiretapKtorPlugin) {
            // All config is optional — defaults log everything
            logRetention = LogRetention.Days(7)
        }
    }
    ```

=== "OkHttp"

    ```kotlin
    val client = OkHttpClient.Builder()
        .addInterceptor(WiretapOkHttpInterceptor {
            logRetention = LogRetention.Days(7)
        })
        .build()
    ```

=== "URLSession (Swift)"

    ```swift
    let interceptor = WiretapURLSessionInterceptor(session: .shared) {
        $0.logRetention = LogRetention.Days(days: 7)
    }
    ```

## 3. Make Requests

Use your HTTP client as usual — Wiretap captures everything automatically:

=== "Ktor"

    ```kotlin
    val response = client.get("https://api.example.com/users")
    println(response.bodyAsText())
    ```

=== "OkHttp"

    ```kotlin
    val request = Request.Builder()
        .url("https://api.example.com/users")
        .build()
    val response = client.newCall(request).execute()
    ```

=== "URLSession (Swift)"

    ```swift
    // Option 1: intercept() — fire-and-forget with mock/throttle rule support
    interceptor.intercept(request: request) { data, response, error in
        // handle response
    }

    // Option 2: dataTask() — returns task for cancel support (logging only)
    let task = interceptor.dataTask(request: request) { data, response, error in
        // handle response
    }
    task.resume()
    ```

## 4. View the Inspector

=== "Android"

    - **Shake your device** to open the Wiretap console (if `enableWiretapLauncher()` was called)
    - Use the **launcher shortcut** added automatically by `WiretapInitializer`
    - Or call `startWiretap()` programmatically

=== "iOS"

    ```swift
    // Call from a button or gesture
    WiretapLauncher_iosKt.startWiretap()
    ```

=== "JVM Desktop"

    ```kotlin
    // Embed the Wiretap Compose UI in your window
    WiretapScreen(
        onBack = { /* handle back */ },
        orchestrator = WiretapDi.orchestrator,
        ruleRepository = WiretapDi.ruleRepository,
        findConflictingRules = WiretapDi.findConflictingRules,
    )
    ```

## What's Next?

- [Configure header masking and filtering](../configuration/config.md)
- [Set up mock and throttle rules](../guides/rules.md)
- [Add WebSocket logging](../guides/websockets.md)
