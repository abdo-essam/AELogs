# AEDevLens

> 🔍 Extensible on-device developer tools SDK for Kotlin Multiplatform — a mini Flipper built for KMP teams.

[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.0-blue.svg)](https://kotlinlang.org)
[![Compose Multiplatform](https://img.shields.io/badge/Compose_Multiplatform-1.9.3-green.svg)](https://www.jetbrains.com/lpc/compose-multiplatform/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## Why "AEDevLens"?

The name carries a specific meaning:

- **AE** — stands for **Arab Engineers** / **Advanced Engineering** (your team identity, publishable under `com.ae.*`)
- **Dev** — short for **Developer** — this is purely a dev tool, invisible to end users
- **Lens** — a lens lets you **see clearly through something**. AEDevLens lets you see through your app at runtime: logs, network calls, analytics events, performance — all in one focused view

Together: **"A developer's lens into your running app"**

It follows the same metaphor as:
- [Flipper](https://fbflipper.com/) — Facebook's extensible dev tool
- [Telescope](https://github.com/mxmCherry/telescope) — look through your app
- [Lens](https://k8slens.dev/) — visibility into complex systems

It's short, technical, memorable, and reflects exactly what the tool does.

---

## Features

- 🔌 **Plugin Architecture** — Extensible with custom built-in or external plugins
- 📋 **Built-in Log Viewer** — Search, filter, copy, and inspect logs in real-time
- 📱 **Adaptive UI** — Bottom sheet on phones, dialog on tablets
- 🎯 **Zero Release Overhead** — `enabled = BuildConfig.DEBUG` disables everything
- 🌍 **Multiplatform** — Android + iOS support
- 🔒 **Plugin Isolation** — One plugin crash never affects other plugins
- ⚡ **High Performance** — SharedFlow + batching, no UI jank on fast log producers
- 🎨 **Themeable** — Full Material3 dark/light theme, customizable colors

---

## Quick Start (3 Steps)

### 1. Add dependency

```kotlin
// settings.gradle.kts
include(":aeinspector")   // or publish to GitHub Packages

// composeApp/build.gradle.kts
implementation(project(":aeinspector"))
```

### 2. Wrap your app

```kotlin
@Composable
fun App() {
    AEDevLensProvider(
        enabled = BuildConfig.DEBUG  // Zero overhead in release
    ) {
        MaterialTheme {
            MainNavigation()
        }
    }
}
```

### 3. That's it — tap the 🐛 button to open the inspector

---

## Custom Logging

You can log directly to AEDevLens from anywhere:

```kotlin
// Simple logging
AEDevLens.default.log(LogLevel.DEBUG, "CartFlow", "Item added: $itemId")
AEDevLens.default.log(LogLevel.ERROR, "Auth", "Token expired: ${error.message}")
AEDevLens.default.log(LogLevel.INFO, "Payment", "Transaction: ${"{"}"amount": 100, "currency": "EGP"{"}"}")
```

JSON bodies in log messages are automatically detected and **pretty-printed** in the inspector.

### Bridge with Kermit (optional)

If your app already uses Kermit, route all its logs automatically:

```kotlin
import co.touchlab.kermit.Logger

Logger.setLogWriters(
    AEDevLensLogWriter(),   // All Kermit logs → AEDevLens
    platformLogWriter()      // Also go to Logcat / console
)
```

> **Why is Kermit not internal?** AEDevLens does NOT force Kermit on you. Every app is different — some use Timber, some use custom log systems, some use `println`. The bridge is optional. If you use Kermit, add it in one line. If not, log directly via `AEDevLens.default.log()`.

---

## What Are Plugins?

AEDevLens is built on a **plugin architecture** — every feature is a self-contained, isolated module that you can install, remove, or replace.

Think of it exactly like browser extensions — you pick what you need.

```
AEDevLens Core (the browser)
    ├── LogsPlugin       → installed by default ✅
    ├── NetworkPlugin    → install if you use Ktor/OkHttp 🔌
    ├── DatabasePlugin   → install if you use SQLDelight/Room 🔌
    ├── PrefsPlugin      → install if you use DataStore/SharedPrefs 🔌
    └── CrashPlugin      → install for crash persistence 🔌 (coming)
```

### Plugin types:

| Type | When to use |
|------|------------|
| `UIPlugin` | Has a visible tab in the inspector (logs, network, etc.) |
| `DataPlugin` | Headless — collects data silently, no visible tab (crash recorder, perf sampler) |

### Installing plugins:

```kotlin
val devLens = AEDevLens.create()

// Only install what you need
devLens.install(LogsPlugin())          // Always useful
devLens.install(NetworkPlugin())       // HTTP inspector (if your app has network)
devLens.install(PrefInspectorPlugin()) // SharedPrefs viewer (if your app uses prefs)
```

### Apps with no network:

If your app has **no network** (offline tools, calculators, games), simply **don't install NetworkPlugin**. There is **zero network overhead** in the core library. The `LogsPlugin` works completely standalone:

```kotlin
// Minimal setup — no network, no prefs, just logs
AEDevLens.create().apply {
    // LogsPlugin is installed automatically — nothing else needed
}
```

---

## Making a Custom Plugin

### Custom UI Plugin (visible tab):

```kotlin
class PreferencesPlugin(
    private val dataStore: DataStore<Preferences>
) : UIPlugin {
    override val id = "prefs"
    override val name = "Prefs"
    override val icon = Icons.Default.Settings

    private val _entries = MutableStateFlow<Map<String, Any>>(emptyMap())
    override val badgeCount: StateFlow<Int?> = _entries
        .map { it.size }
        .stateIn(CoroutineScope(Dispatchers.Default), SharingStarted.Eagerly, null)

    override fun onAttach(inspector: AEDevLens) {
        // Start observing DataStore
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val entries by _entries.collectAsState()
        LazyColumn(modifier) {
            items(entries.entries.toList()) { (key, value) ->
                ListItem(
                    headlineContent = { Text(key) },
                    supportingContent = { Text(value.toString()) }
                )
            }
        }
    }

    override fun onClear() { _entries.value = emptyMap() }
}
```

### Custom Data Plugin (headless, no UI tab):

```kotlin
class PerformanceSamplerPlugin : DataPlugin {
    override val id = "perf_sampler"
    override val name = "Performance"

    private val samples = mutableListOf<FrameData>()

    override fun onOpen() {
        // Start collecting frame data when inspector opens
        startSampling()
    }

    override fun onClose() {
        // Stop heavy sampling when inspector is closed
        stopSampling()
    }

    override fun onClear() { samples.clear() }
}
```

### Registering your custom plugin:

```kotlin
AEDevLens.default.install(PreferencesPlugin(myDataStore))
AEDevLens.default.install(PerformanceSamplerPlugin())
```

---

## Plugin Lifecycle

```
install() → onAttach(inspectorInstance)
                ↓
           [Inspector opens] → onOpen()
                ↓
           [Inspector closes] → onClose()
                ↓
           [Repeat open/close as user uses it]
                ↓
           uninstall() → onDetach()
```

| Method | When | Typical Use |
|--------|------|-------------|
| `onAttach` | Plugin registered | Init coroutines, open DB connections |
| `onOpen` | Inspector UI appears | Start listeners, refresh data |
| `onClose` | Inspector UI hidden | Pause expensive sampling |
| `onDetach` | Plugin unregistered | Close connections, cancel jobs |
| `onClear` | User taps "Clear" | Delete collected data |

---

## Architecture Decisions

| Decision | Reason |
|----------|--------|
| **Instance-based** (`AEDevLens.create()`) instead of Singleton | Testable, no hidden globals, supports multiple isolated instances |
| **Plugin isolation** via try-catch boundaries | One crashing plugin never kills the inspector |
| **Kermit is optional** (`api`, not embedded) | Don't force Kermit on apps using other log systems |
| **Adaptive UI** (Sheet vs Dialog) | Bottom sheet on phones, dialog on tablets — proper UX |
| **SharedFlow + batching** per `LogStore` | Prevents UI jank when 100 logs/sec are produced |
| **`enabled = false`** → zero overhead | Safe to leave in code for release builds |

---

## Best Practices

| ✅ Do | ❌ Don't |
|-------|---------|
| `enabled = BuildConfig.DEBUG` | Ship inspector to production |
| Use `debugImplementation` | Log sensitive data (tokens, PII, passwords) |
| Keep plugins small and focused | Create monolithic plugins |
| Use `DataPlugin` for headless tasks | Force Compose on non-UI plugins |
| Install only plugins you need | Install everything "just in case" |

---

## Roadmap

- [ ] `NetworkPlugin` — Ktor/OkHttp HTTP inspector
- [ ] `DatabasePlugin` — SQLDelight table browser
- [ ] `PrefsPlugin` — DataStore / SharedPreferences viewer
- [ ] `CrashPlugin` — Crash persistence across restarts
- [ ] `PerformancePlugin` — FPS / memory sampler
- [ ] Shake-to-open (configurable gesture)
- [ ] GitHub Packages publishing CI/CD

---

## License

MIT License — see [LICENSE](LICENSE) for details.
