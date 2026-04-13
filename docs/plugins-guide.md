# Plugins Overview

AEDevLens uses a plugin architecture. Each plugin adds a tab to the DevLens overlay.

## Built-in Plugins

| Plugin | Type | Description |
|--------|------|-------------|
| `LogsPlugin` | `UIPlugin` | Real-time log viewer with level/tag filtering |

## Plugin Types

### UIPlugin
Renders a tab with header and content areas. Ideal for inspecting data visually.

### DataPlugin
Background data collector with no UI. Feeds data to a `UIPlugin`.

## Installing Plugins

```kotlin
val devLens = rememberAEDevLens {
    install(LogsPlugin())
    install(MyCustomPlugin())
}
```

## Creating Custom Plugins

See the [Custom Plugins](custom-plugins.md) guide for a full walkthrough.
