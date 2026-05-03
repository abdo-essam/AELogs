# Getting Started

## Requirements

| Platform | Minimum |
|----------|---------|
| Android  | API 24 (7.0) |
| iOS      | 15.0 |
| Kotlin   | 2.3.0 |
| Compose Multiplatform | 1.9.3 |

## Installation

=== "Version Catalog (Recommended)"

    ```toml title="gradle/libs.versions.toml"
    [versions]
    aelog = "1.0.0"

    [libraries]
    aelog-core      = { module = "com.ae.log:log-core", version.ref = "aelog" }
    aelog-network   = { module = "com.ae.log:log-network", version.ref = "aelog" }
    aelog-analytics = { module = "com.ae.log:log-analytics", version.ref = "aelog" }
    ```

    ```kotlin title="shared/build.gradle.kts"
    kotlin {
        sourceSets {
            commonMain.dependencies {
                implementation(libs.aelog.core)
                implementation(libs.aelog.network)
                implementation(libs.aelog.analytics)
            }
        }
    }
    ```

=== "Direct Dependency"

    ```kotlin title="shared/build.gradle.kts"
    kotlin {
        sourceSets {
            commonMain.dependencies {
                implementation("com.ae.log:log-core:1.0.0")
                implementation("com.ae.log:log-network:1.0.0")
                implementation("com.ae.log:log-analytics:1.0.0")
            }
        }
    }
    ```

## Verify Installation

```kotlin
import com.ae.log.AELog

fun main() {
    // Simply check if AELog is accessible
    AELog.isEnabled = true
    println("AELog is accessible and enabled: ${AELog.isEnabled}")
}
```

!!! success "Expected output"
    ```text
    AELog is accessible and enabled: true
    ```

## Next Steps
- [Quick Start Guide](quick-start.md) — Integrate into your app in 5 minutes
- [Configuration](configuration.md) — Customize behavior and appearance
- [Custom Plugins](plugins-guide.md) — Build your own debug panels
