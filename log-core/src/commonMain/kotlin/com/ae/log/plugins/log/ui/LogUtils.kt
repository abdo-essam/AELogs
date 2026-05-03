package com.ae.log.plugins.log.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.ae.log.plugins.log.model.*
import com.ae.log.plugins.log.model.LogEntry
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

/**
 * Utility functions for log formatting and coloring.
 */
internal object LogUtils {
    fun formatTimestamp(timestamp: Long): String = com.ae.log.core.utils.TimeUtils.formatTimestamp(timestamp)

    fun formatLogForCopy(log: LogEntry): String =
        buildString {
            appendLine("[${formatTimestamp(log.timestamp)}] [${log.displayTag}]")
            appendLine("Tag: ${log.tag}")
            log.httpMethod?.let { appendLine("Method: $it") }
            log.url?.let { appendLine("URL: $it") }
            log.httpStatusCode?.let { appendLine("Status: $it") }
            log.jsonBody?.let {
                appendLine("Body:")
                appendLine(it)
            } ?: run {
                appendLine("Message:")
                appendLine(log.bodyOnly)
            }
        }

    fun formatAllLogsForCopy(logs: List<LogEntry>): String =
        logs.joinToString("\n\n") { log ->
            buildString {
                appendLine("=".repeat(50))
                append(formatLogForCopy(log))
            }
        }

    fun getMethodColor(method: String): Color = com.ae.log.ui.theme.NetworkColors.getMethodColor(method)

    fun getStatusCodeColor(statusCode: Int): Color = com.ae.log.ui.theme.NetworkColors.getStatusCodeColor(statusCode)

    @Composable
    fun getLogTypeColor(log: LogEntry): Pair<Color, Color> {
        val colors = androidx.compose.material3.MaterialTheme.colorScheme
        val isDark = isSystemInDarkTheme()
        val mainColor =
            when {
                log.isAnalytics -> if (isDark) Color(0xFFFFB74D) else Color(0xFFFF6D00)
                log.isError -> colors.error
                log.isResponse -> if (isDark) Color(0xFF81C784) else Color(0xFF4CAF50)
                log.isRequest -> if (isDark) Color(0xFF64B5F6) else Color(0xFF2196F3)
                log.isNetworkLog -> if (isDark) Color(0xFFBA68C8) else Color(0xFF9C27B0)
                else -> colors.onSurfaceVariant
            }
        val bgColor =
            when {
                log.isAnalytics -> if (isDark) Color(0xFF4E342E) else Color(0xFFFFF3E0)
                log.isError -> colors.errorContainer
                log.isResponse -> if (isDark) Color(0xFF1B5E20) else Color(0xFFE8F5E9)
                log.isRequest -> if (isDark) Color(0xFF0D47A1) else Color(0xFFE3F2FD)
                log.isNetworkLog -> if (isDark) Color(0xFF4A148C) else Color(0xFFF3E5F5)
                else -> colors.surfaceVariant
            }
        return mainColor to bgColor
    }

    fun getBadgeLabel(log: LogEntry, registry: LogTagRegistry? = null): String =
        when {
            log.isAnalytics ->
                registry?.getLabel(log.tag) ?: log.tag.take(3).uppercase()
            log.isError -> "ERR"
            log.isResponse -> "RES"
            log.isRequest -> "REQ"
            log.isNetworkLog -> "NET"
            else -> "LOG"
        }
}
