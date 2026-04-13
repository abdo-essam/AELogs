# Configuration

Configure AEDevLens behaviour via `AEDevLensConfig`.

## Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `enabled` | `Boolean` | `true` | Enable or disable the overlay entirely |
| `maxLogEntries` | `Int` | `500` | Max number of log entries to keep in memory |
| `shakeToOpen` | `Boolean` | `true` | Android: shake gesture opens the overlay |
| `triggerThreshold` | `Float` | `2.5f` | Shake sensitivity (higher = less sensitive) |

## Example

```kotlin
AEDevLens.configure {
    enabled = BuildConfig.DEBUG
    maxLogEntries = 1000
    shakeToOpen = true
}
```

## Disabling in Release

Always guard AEDevLens with a debug flag:

```kotlin
if (BuildConfig.DEBUG) {
    AEDevLens.configure { enabled = true }
}
```
