package com.ae.log.plugins.log.model

import androidx.compose.runtime.Immutable
import kotlin.time.Clock

/**
 * Represents a single log entry captured by AELog.
 */
@OptIn(kotlin.time.ExperimentalTime::class)
@Immutable
public data class LogEntry(
    val id: String =
        com.ae.log.core.utils.IdGenerator
            .next(),
    val severity: LogSeverity,
    val tag: String,
    val message: String,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds(),
    val kind: LogKind = LogKind.LOG,
    val httpFields: HttpFields? = null,
    val analyticsLabel: String? = null,
)

/**
 * Defines the high-level category of a log entry for filtering and display.
 */
public enum class LogKind {
    LOG,
    NETWORK,
    ANALYTICS,
}

/**
 * Extracted HTTP-specific metadata for network logs.
 */
public data class HttpFields(
    val method: String?,
    val statusCode: Int?,
    val url: String?,
    val isResponse: Boolean,
)
