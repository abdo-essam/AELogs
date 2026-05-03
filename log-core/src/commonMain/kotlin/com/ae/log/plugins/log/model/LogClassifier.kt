package com.ae.log.plugins.log.model

import kotlinx.atomicfu.update
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

private val PRETTY_JSON =
    Json {
        prettyPrint = true
        prettyPrintIndent = "  "
    }

private val HTTP_METHODS = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS")

private val STATUS_CODE_PATTERN by lazy { Regex("(?:HTTP/\\d\\.\\d\\s+|status[:\\s]+)(\\d{3})") }
private val URL_PATTERN by lazy { Regex("https?://[^\\s\"'\\]\\)>]+") }
private val HEADER_PATTERNS by lazy { listOf(Regex("^[A-Za-z][A-Za-z0-9-]*:\\s*.+$")) }

private val EXCLUDED_LINES =
    listOf(
        "COMMON HEADERS",
        "CONTENT HEADERS",
        "BODY START",
        "BODY END",
        "REQUEST",
        "RESPONSE",
        "HEADERS",
        "-->",
        "<--",
    )

private val IMPORTANT_HEADERS = listOf("Content-Type", "Authorization")

/**
 * Pre-computes classification and extraction fields for a log entry.
 * Should be called once during insertion to avoid redundant regex matching.
 */
public fun classify(
    severity: LogSeverity,
    tag: String,
    message: String,
    registry: LogTagRegistry,
): Triple<LogKind, HttpFields?, String?> {
    val method = HTTP_METHODS.firstOrNull { message.contains(it) }
    val statusCode =
        runCatching {
            STATUS_CODE_PATTERN
                .find(message)
                ?.groupValues
                ?.get(1)
                ?.toIntOrNull()
                ?.takeIf { it in 100..599 }
        }.getOrNull()
    val url = runCatching { URL_PATTERN.find(message)?.value }.getOrNull()

    val isResponse =
        message.contains("<--") ||
            message.contains("RESPONSE", ignoreCase = true) ||
            (statusCode != null && !message.contains("-->"))

    val isRequest =
        !isResponse &&
            (message.contains("-->") || message.contains("REQUEST", ignoreCase = true) || method != null)

    val isAnalytics = registry.isRegistered(tag)
    val analyticsLabel = if (isAnalytics) registry.getLabel(tag) else null

    val isNetwork =
        !isAnalytics &&
            (
                tag.contains("HTTP", ignoreCase = true) ||
                    tag.contains("Network", ignoreCase = true) ||
                    tag.contains("API", ignoreCase = true) ||
                    tag.contains("ktor", ignoreCase = true) ||
                    isRequest ||
                    isResponse ||
                    url != null ||
                    method != null
            )

    val kind =
        when {
            isAnalytics -> LogKind.ANALYTICS
            isNetwork -> LogKind.NETWORK
            else -> LogKind.LOG
        }

    val httpFields =
        if (isNetwork || isRequest || isResponse) {
            HttpFields(method, statusCode, url, isResponse)
        } else {
            null
        }

    return Triple(kind, httpFields, analyticsLabel)
}

// --- Type Classification ---
public val LogEntry.isResponse: Boolean
    get() = httpFields?.isResponse ?: false

public val LogEntry.isRequest: Boolean
    get() =
        httpFields != null && !isResponse && (httpFields.method != null || message.contains("-->") || message.contains("REQUEST", ignoreCase = true))

public val LogEntry.isNetworkLog: Boolean
    get() = kind == LogKind.NETWORK

public val LogEntry.isError: Boolean
    get() = severity == LogSeverity.ERROR || severity == LogSeverity.ASSERT

public val LogEntry.isAnalytics: Boolean
    get() = kind == LogKind.ANALYTICS

// --- HTTP Parsing ---
public val LogEntry.httpMethod: String?
    get() = httpFields?.method

public val LogEntry.httpStatusCode: Int?
    get() = httpFields?.statusCode

public val LogEntry.url: String?
    get() = httpFields?.url

public val LogEntry.endpoint: String?
    get() =
        url
            ?.runCatching {
                val path = substringAfter("://").substringAfter("/").substringBefore("?")
                if (path.isNotEmpty()) "/$path" else null
            }?.getOrNull()

// --- Display Helpers ---
public val LogEntry.displayTag: String
    get() =
        when (kind) {
            LogKind.ANALYTICS -> analyticsLabel?.uppercase() ?: tag.uppercase()
            LogKind.NETWORK -> if (isResponse) "RESPONSE" else if (isRequest) "REQUEST" else "NETWORK"
            else -> if (isError) "ERROR" else "LOG"
        }

public val LogEntry.bodyOnly: String
    get() =
        message
            .lineSequence()
            .filterNot { line ->
                EXCLUDED_LINES.any { excluded ->
                    line.trim().startsWith(excluded, ignoreCase = true)
                }
            }.filterNot { line ->
                line.contains(":") &&
                    HEADER_PATTERNS.any { it.matches(line.trim()) } &&
                    !IMPORTANT_HEADERS.any { line.trim().startsWith(it, ignoreCase = true) }
            }.joinToString("\n")
            .trim()
            .ifEmpty { message }

public val LogEntry.jsonBody: String?
    get() = extractJson(message)?.let { formatJson(it) }

// --- JSON Extraction ---
private fun extractJson(text: String): String? {
    val arrayStart = text.indexOf('[')
    val objectStart = text.indexOf('{')
    return when {
        arrayStart == -1 && objectStart == -1 -> null
        arrayStart == -1 -> extractJsonBlock(text, objectStart, '{', '}')
        objectStart == -1 -> extractJsonBlock(text, arrayStart, '[', ']')
        arrayStart < objectStart -> extractJsonBlock(text, arrayStart, '[', ']')
        else -> extractJsonBlock(text, objectStart, '{', '}')
    }
}

private fun extractJsonBlock(
    text: String,
    startIndex: Int,
    open: Char,
    close: Char,
): String? {
    var count = 0
    for (i in startIndex until text.length) {
        when (text[i]) {
            open -> count++
            close -> {
                count--
                if (count == 0) return text.substring(startIndex, i + 1)
            }
        }
    }
    return null
}

@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
private fun formatJson(json: String): String =
    runCatching {
        val jsonElement = PRETTY_JSON.parseToJsonElement(json)
        PRETTY_JSON.encodeToString(JsonElement.serializer(), jsonElement)
    }.getOrDefault(json)
