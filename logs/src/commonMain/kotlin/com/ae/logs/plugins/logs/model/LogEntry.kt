package com.ae.logs.plugins.logs.model

import androidx.compose.runtime.Immutable
import kotlin.time.Clock

/**
 * Represents a single log entry captured by AELogs.
 *
 * This is a simple data holder. Parsing and classification logic
 * are provided via extension properties in LogClassifier.
 */
@Immutable
public data class LogEntry(
    val id: String =
        com.ae.logs.core.utils.IdGenerator
            .generateId(),
    val severity: LogSeverity,
    val tag: String,
    val message: String,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds(),
)
