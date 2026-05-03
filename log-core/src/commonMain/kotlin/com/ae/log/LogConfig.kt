package com.ae.log

import com.ae.log.plugins.log.model.LogSeverity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Configuration for a [AELog] instance.
 *
 * Contains only core (non-UI) settings. UI-specific config lives in
 * `UiConfig` in the `logs-ui` module.
 *
 * ```kotlin
 * AELog.create(LogConfig())
 * ```
 */
public data class LogConfig(
    val minSeverity: LogSeverity = LogSeverity.VERBOSE,
    val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    val platformLogSink: PlatformLogSink = PlatformLogSink.Default,
    val errorHandler: (Throwable) -> Unit = {
        platformLogSink.log(LogSeverity.ERROR, "AELog", "Plugin error", it)
    },
)
