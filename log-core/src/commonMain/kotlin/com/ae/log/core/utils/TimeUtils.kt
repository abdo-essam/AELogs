package com.ae.log.core.utils

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

/**
 * Shared time-related utilities for AELog.
 */
public object TimeUtils {
    /**
     * Formats a millisecond timestamp to HH:mm:ss.
     */
    @OptIn(kotlin.time.ExperimentalTime::class)
    public fun formatTimestamp(timestamp: Long): String =
        runCatching {
            val instant = Instant.fromEpochMilliseconds(timestamp)
            val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            buildString {
                append(localDateTime.hour.toString().padStart(2, '0'))
                append(":")
                append(localDateTime.minute.toString().padStart(2, '0'))
                append(":")
                append(localDateTime.second.toString().padStart(2, '0'))
            }
        }.getOrDefault("")
}
