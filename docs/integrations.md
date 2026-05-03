# Logging Integrations

AELog works with **any** logging library. Just forward logs to the static `AELog.log()` method.

## Kermit

```kotlin
import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import com.ae.log.AELog
import com.ae.log.plugins.log.model.LogSeverity

class AELogKermitWriter : LogWriter() {
    override fun log(
        severity: Severity,
        message: String,
        tag: String,
        throwable: Throwable?
    ) {
        AELog.log(
            severity = severity.toAELogLogSeverity(),
            tag = tag,
            message = message,
            throwable = throwable
        )
    }
}

private fun Severity.toAELogLogSeverity(): LogSeverity = when (this) {
    Severity.Verbose -> LogSeverity.VERBOSE
    Severity.Debug -> LogSeverity.DEBUG
    Severity.Info -> LogSeverity.INFO
    Severity.Warn -> LogSeverity.WARN
    Severity.Error -> LogSeverity.ERROR
    Severity.Assert -> LogSeverity.ASSERT
}

// Setup
Logger.addLogWriter(AELogKermitWriter())
```

## Napier

```kotlin
import io.github.aakira.napier.Antilog
import io.github.aakira.napier.LogLevel
import com.ae.log.AELog
import com.ae.log.plugins.log.model.LogSeverity

class AELogNapierAntilog : Antilog() {
    override fun performLog(
        priority: LogLevel,
        tag: String?,
        throwable: Throwable?,
        message: String?
    ) {
        AELog.log(
            severity = priority.toAELogLogSeverity(),
            tag = tag ?: "Napier",
            message = message ?: "",
            throwable = throwable
        )
    }
}

private fun LogLevel.toAELogLogSeverity(): LogSeverity = when (this) {
    LogLevel.VERBOSE -> LogSeverity.VERBOSE
    LogLevel.DEBUG -> LogSeverity.DEBUG
    LogLevel.INFO -> LogSeverity.INFO
    LogLevel.WARNING -> LogSeverity.WARN
    LogLevel.ERROR -> LogSeverity.ERROR
    LogLevel.ASSERT -> LogSeverity.ASSERT
}

// Setup
Napier.base(AELogNapierAntilog())
```

## Timber (Android)

```kotlin
import timber.log.Timber
import com.ae.log.AELog
import com.ae.log.plugins.log.model.LogSeverity

class AELogTimberTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        AELog.log(
            severity = priority.toAELogLogSeverity(),
            tag = tag ?: "Timber",
            message = message,
            throwable = t
        )
    }
}

private fun Int.toAELogLogSeverity(): LogSeverity = when (this) {
    android.util.Log.VERBOSE -> LogSeverity.VERBOSE
    android.util.Log.DEBUG -> LogSeverity.DEBUG
    android.util.Log.INFO -> LogSeverity.INFO
    android.util.Log.WARN -> LogSeverity.WARN
    android.util.Log.ERROR -> LogSeverity.ERROR
    android.util.Log.ASSERT -> LogSeverity.ASSERT
    else -> LogSeverity.DEBUG
}

// Setup
Timber.plant(AELogTimberTree())
```

## Ktor Client Logging

```kotlin
import io.ktor.client.*
import io.ktor.client.plugins.logging.*
import com.ae.log.AELog

val client = HttpClient {
    install(Logging) {
        logger = object : Logger {
            override fun log(message: String) {
                // Forward to AELog
                AELog.d("HTTP", message)
            }
        }
        level = LogLevel.ALL
    }
}
```

## Direct Usage (No Library)

```kotlin
// Use the static shorthands
AELog.i("MyApp", "App started")
AELog.e("Auth", "Login failed", error)

// Or the generic log method
AELog.log(LogSeverity.INFO, "MyApp", "Direct log call")
```
