# Configuration

Configure AELogs structure via `AELogsSetup.init()` and behaviour via `AELogsUiConfig`.

## Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `enabled` | `Boolean` | `false` | Enable or disable the overlay entirely (passed into `AELogsProvider`) |
| `showFloatingButton` | `Boolean` | `true` | Show floating bug button overlay |
| `enableLongPress` | `Boolean` | `true` | Show panel on 3-finger long press |

## Example

```kotlin
// Data config
AELogsSetup.init(
    config = AELogsConfig(maxLogEntries = 1000)
)

// UI config
AELogsProvider(
    inspector = AELogs.default,
    enabled = BuildConfig.DEBUG,
    uiConfig = AELogsUiConfig(
        showFloatingButton = true,
        enableLongPress = true
    )
) {
    MyApp()
}
```
