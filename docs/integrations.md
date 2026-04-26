# Logging Integrations

AELogs works with **any** logging library. Just forward logs to `AELogs.default.log()`.

## Kermit

```kotlin
import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import com.ae.logs.AELogs
import com.ae.logs.plugins.logs.model.LogSeverity

class AELogsKermitWriter(
    private val inspector: AELogs = AELogs.default
) : LogWriter() {
    override fun log(
        severity: Severity,
        message: String,
        tag: String,
        throwable: Throwable?
    ) {
        inspector.log(
            severity = severity.toAELogsLogSeverity(),
            tag = tag,
            message = buildString {
                append(message)
                throwable?.let { append("\n${it.stackTraceToString()}") }
            }
        )
    }
}

private fun Severity.toAELogsLogSeverity(): LogSeverity = when (this) {
    Severity.Verbose -> LogSeverity.VERBOSE
    Severity.Debug -> LogSeverity.DEBUG
    Severity.Info -> LogSeverity.INFO
    Severity.Warn -> LogSeverity.WARN
    Severity.Error -> LogSeverity.ERROR
    Severity.Assert -> LogSeverity.ASSERT
}

// Setup
Logger.addLogWriter(AELogsKermitWriter())
```

## Napier

```kotlin
import io.github.aakira.napier.Antilog
import io.github.aakira.napier.LogLevel
import com.ae.logs.AELogs
import com.ae.logs.plugins.logs.model.LogSeverity

class AELogsNapierAntilog(
    private val inspector: AELogs = AELogs.default
) : Antilog() {
    override fun performLog(
        priority: LogLevel,
        tag: String?,
        throwable: Throwable?,
        message: String?
    ) {
        inspector.log(
            severity = priority.toAELogsLogSeverity(),
            tag = tag ?: "Napier",
            message = buildString {
                message?.let { append(it) }
                throwable?.let { append("\n${it.stackTraceToString()}") }
            }
        )
    }
}

private fun LogLevel.toAELogsLogSeverity(): LogSeverity = when (this) {
    LogLevel.VERBOSE -> LogSeverity.VERBOSE
    LogLevel.DEBUG -> LogSeverity.DEBUG
    LogLevel.INFO -> LogSeverity.INFO
    LogLevel.WARNING -> LogSeverity.WARN
    LogLevel.ERROR -> LogSeverity.ERROR
    LogLevel.ASSERT -> LogSeverity.ASSERT
}

// Setup
Napier.base(AELogsNapierAntilog())
```

## Timber (Android)

```kotlin
import timber.log.Timber
import com.ae.logs.AELogs
import com.ae.logs.plugins.logs.model.LogSeverity

class AELogsTimberTree(
    private val inspector: AELogs = AELogs.default
) : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        inspector.log(
            severity = priority.toAELogsLogSeverity(),
            tag = tag ?: "Timber",
            message = buildString {
                append(message)
                t?.let { append("\n${it.stackTraceToString()}") }
            }
        )
    }
}

private fun Int.toAELogsLogSeverity(): LogSeverity = when (this) {
    android.util.Log.VERBOSE -> LogSeverity.VERBOSE
    android.util.Log.DEBUG -> LogSeverity.DEBUG
    android.util.Log.INFO -> LogSeverity.INFO
    android.util.Log.WARN -> LogSeverity.WARN
    android.util.Log.ERROR -> LogSeverity.ERROR
    android.util.Log.ASSERT -> LogSeverity.ASSERT
    else -> LogSeverity.DEBUG
}

// Setup
Timber.plant(AELogsTimberTree())
```

## KotlinLogging / SLF4J

```kotlin
import org.slf4j.event.Level
import com.ae.logs.AELogs
import com.ae.logs.plugins.logs.model.LogSeverity
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase

class AELogsSlf4jAppender(
    private val inspector: AELogs = AELogs.default
) : AppenderBase<ILoggingEvent>() {
    override fun append(event: ILoggingEvent) {
        inspector.log(
            severity = event.level.toAELogsLogSeverity(),
            tag = event.loggerName.substringAfterLast('.'),
            message = event.formattedMessage
        )
    }
}

private fun Level.toAELogsLogSeverity(): LogSeverity = when (this) {
    Level.TRACE -> LogSeverity.VERBOSE
    Level.DEBUG -> LogSeverity.DEBUG
    Level.INFO -> LogSeverity.INFO
    Level.WARN -> LogSeverity.WARN
    Level.ERROR -> LogSeverity.ERROR
}
```

## Ktor Client Logging

```kotlin
import io.ktor.client.*
import io.ktor.client.plugins.logging.*
import com.ae.logs.AELogs
import com.ae.logs.plugins.logs.model.LogSeverity

val client = HttpClient {
    install(Logging) {
        logger = object : Logger {
            override fun log(message: String) {
                AELogs.default.log(
                    severity = LogSeverity.DEBUG,
                    tag = "HTTP",
                    message = message
                )
            }
        }
        level = LogLevel.ALL
    }
}
```

## Direct Usage (No Library)

```kotlin
// Just call log() directly — no bridge needed
val inspector = AELogs.default

inspector.log(LogSeverity.INFO, "MyApp", "App started")
inspector.log(LogSeverity.ERROR, "Auth", "Login failed: $error")
```
