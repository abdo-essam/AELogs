# Kermit Integration

[Kermit](https://github.com/touchlab/Kermit) is a popular KMP logging library by Touchlab. You can route Kermit logs into AEDevLens by creating a custom `LogWriter`.

## Setup

```kotlin
class DevLensLogWriter : LogWriter() {
    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        val level = when (severity) {
            Severity.Verbose -> LogLevel.VERBOSE
            Severity.Debug   -> LogLevel.DEBUG
            Severity.Info    -> LogLevel.INFO
            Severity.Warn    -> LogLevel.WARN
            Severity.Error   -> LogLevel.ERROR
            Severity.Assert  -> LogLevel.ERROR
        }
        AEDevLens.log(tag = tag, message = message, level = level, throwable = throwable)
    }
}
```

## Usage

```kotlin
// In your DI / app init
Logger.addLogWriter(DevLensLogWriter())
```

Now every Kermit log call will automatically appear in the AEDevLens log viewer.
