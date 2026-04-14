# Quick Start

Add AEDevLens to your project and initialize it in just a few lines.

## Basic Setup

```kotlin
// In your App composable (commonMain)
val devLens = rememberAEDevLens {
    install(LogsPlugin())
}

AEDevLensOverlay(controller = devLens) {
    // Your app content
    MyApp()
}
```

## Log Something

```kotlin
val devLens = AEDevLens.default

devLens.log(severity = LogSeverity.INFO, tag = "MyScreen", message = "Button clicked")
devLens.log(severity = LogSeverity.DEBUG, tag = "Network", message = "GET /api/users")
```

## Show / Hide the Overlay

The overlay can be triggered by:
- **Shake gesture** (Android) — enabled by default
- **Programmatically** via `AEDevLensController.show()` / `hide()`
