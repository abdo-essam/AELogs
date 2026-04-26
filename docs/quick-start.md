# Quick Start

Add AELogs to your project and initialize it in just a few lines.

## Basic Setup

Call this early in your app lifecycle:

```kotlin
AELogsSetup.init(
    plugins = listOf(
        LogsPlugin(),
        NetworkPlugin(),
        AnalyticsPlugin()
    )
)
```

Wrap your root compose app:

```kotlin
// In your App composable (commonMain)
AELogsProvider(
    inspector = AELogs.default, 
    enabled = true // Tie this to your build variant (e.g. debug=true, release=false)
) {
    // Your app content
    MyApp()
}
```

## Log Something

Use the global static APIs corresponding to your installed plugins:

```kotlin
// 1. Logs
AELogs.i("MyScreen", "Button clicked")
AELogs.e("Database", "Failed to load configs", exception)

// 2. Network
NetworkApi.logRequest("GET", "https://api.example.com", headers = emptyMap())

// 3. Analytics
AnalyticsApi.logEvent("user_tapped_purchase", properties = mapOf("val" to "2.99"))
```

## Show / Hide the Overlay

The overlay can be triggered by:
- **Floating Button** — enabled by default
- **Long-Press Gesture** — enabled by default
- **Programmatically** via `LocalAELogsController.current.show()` / `hide()`
