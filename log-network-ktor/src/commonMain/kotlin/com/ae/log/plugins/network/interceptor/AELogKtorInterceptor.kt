@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.ae.log.plugins.network.interceptor

import com.ae.log.AELog
import com.ae.log.plugins.network.NetworkPlugin
import com.ae.log.plugins.network.model.NetworkMethod
import io.ktor.client.plugins.api.ClientPlugin
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.statement.bodyAsText
import io.ktor.http.content.ByteArrayContent
import io.ktor.http.content.TextContent
import io.ktor.util.AttributeKey
import kotlinx.io.readByteArray

/**
 * Ktor [io.ktor.client.HttpClient] plugin that automatically records every
 * request/response pair into the [NetworkPlugin] viewer — zero boilerplate,
 * no ID management.
 *
 * ## Setup
 *
 * Add the dependency:
 * ```kotlin
 * // build.gradle.kts
 * implementation("io.github.abdo-essam:log-network-ktor:<version>")
 * ```
 *
 * Install the plugin — one line:
 * ```kotlin
 * val client = HttpClient(CIO) {
 *     install(AELogKtorInterceptor)
 * }
 * ```
 *
 * Every subsequent call is captured automatically.
 *
 * ## What is captured
 *
 * | Field | Source |
 * |-------|--------|
 * | URL | `request.url` |
 * | Method | `request.method` |
 * | Request headers | `request.headers` |
 * | Status code | `response.status.value` |
 * | Response headers | `response.headers` |
 * | Duration | monotonic clock delta (request → response) |
 *
 * Request and response **bodies** are captured automatically up to the configured limits.
 * Response bodies are captured using a non-destructive observer that does not consume
 * the primary response stream.
 *
 * ## Silent no-op
 *
 * If [NetworkPlugin] is not installed or [AELog.init] has not been called,
 * every hook returns immediately — the real HTTP client is never affected.
 */

public class AELogKtorConfig {
    public var redactHeaders: Set<String> =
        setOf(
            "Authorization",
            "Cookie",
            "Set-Cookie",
            "Proxy-Authorization",
            "X-Api-Key",
        )
    public var maxRequestBodyBytes: Long = 250_000L
    public var maxResponseBodyBytes: Long = 250_000L
    public var excludeUrls: List<Regex> = emptyList()
}

public val AELogKtorInterceptor: ClientPlugin<AELogKtorConfig> =
    createClientPlugin("AELogKtor", ::AELogKtorConfig) {
        val redactHeaders = pluginConfig.redactHeaders
        val excludeUrls = pluginConfig.excludeUrls
        val maxRequestBodyBytes = pluginConfig.maxRequestBodyBytes
        val maxResponseBodyBytes = pluginConfig.maxResponseBodyBytes

        val networkRequestIdKey = AttributeKey<String>("AELogNetworkRequestId")

        fun Map<String, String>.redact(): Map<String, String> {
            if (redactHeaders.isEmpty()) return this
            return mapValues { (key, value) ->
                if (redactHeaders.any { it.equals(key, ignoreCase = true) }) "***" else value
            }
        }

        fun shouldCaptureBody(contentType: String?): Boolean {
            if (contentType == null) return false
            return contentType.startsWith("text/", ignoreCase = true) ||
                contentType.contains("json", ignoreCase = true) ||
                contentType.contains("xml", ignoreCase = true) ||
                contentType.contains("form-urlencoded", ignoreCase = true)
        }

        on(Send) { request ->
            if (!AELog.isEnabled) return@on proceed(request)
            val recorder = AELog.plugin<NetworkPlugin>()?.recorder ?: return@on proceed(request)

            val urlString = request.url.buildString()
            if (excludeUrls.any { it.matches(urlString) }) return@on proceed(request)

            val id = recorder.newId()
            val startMark =
                kotlin.time.TimeSource.Monotonic
                    .markNow()

            val contentType = request.headers["Content-Type"]
            val reqBody =
                if (shouldCaptureBody(contentType)) {
                    val fullBody =
                        when (val content = request.body) {
                            is TextContent -> content.text
                            is ByteArrayContent -> content.bytes().decodeToString()
                            is String -> content
                            else -> null
                        }

                    if (fullBody == null) {
                        "<streamed or unknown body>"
                    } else if (fullBody.length > maxRequestBodyBytes) {
                        fullBody.take(maxRequestBodyBytes.toInt()) + "\n… [truncated]"
                    } else {
                        fullBody
                    }
                } else {
                    val len = request.headers["Content-Length"]
                    if (len != null) "<binary or unsupported, $len bytes>" else "<binary or unsupported>"
                }

            recorder.request(
                id = id,
                url = request.url.buildString(),
                method = NetworkMethod.fromString(request.method.value),
                rawMethod = request.method.value,
                headers =
                    request.headers
                        .entries()
                        .associate { entry -> entry.key to entry.value.joinToString(", ") }
                        .redact(),
                body = reqBody,
            )

                request.attributes.put(networkRequestIdKey, id)

                val call =
                    try {
                        proceed(request)
                    } catch (t: Throwable) {
                        recorder.error(id = id, message = t.message ?: t::class.simpleName ?: "Unknown error")
                        throw t
                    }

                val durationMs = startMark.elapsedNow().inWholeMilliseconds

                // Capture response headers and status immediately
                val headers =
                    call.response.headers
                        .entries()
                        .associate { entry -> entry.key to entry.value.joinToString(", ") }
                        .redact()

                recorder.response(
                    id = id,
                    statusCode = call.response.status.value,
                    headers = headers,
                    body = null, // Will be updated by the onResponse hook if possible
                    durationMs = durationMs,
                )

                return@on call
            }

        onResponse { response ->
            if (!AELog.isEnabled) return@onResponse
            val id = response.call.attributes.getOrNull(networkRequestIdKey) ?: return@onResponse
            val recorder = AELog.plugin<NetworkPlugin>()?.recorder ?: return@onResponse

            if (shouldCaptureBody(response.headers["Content-Type"])) {
                runCatching {
                    // Users should install DoubleReceive plugin to avoid consuming the stream.
                    val bodyString =
                        response.bodyAsText().let {
                            if (it.length.toLong() > maxResponseBodyBytes) {
                                it.take(maxResponseBodyBytes.toInt()) + "\n… [truncated]"
                            } else {
                                it
                            }
                        }

                    recorder.response(
                        id = id,
                        statusCode = response.status.value,
                        headers =
                            response.headers.entries()
                                .associate { entry -> entry.key to entry.value.joinToString(", ") }
                                .redact(),
                        body = bodyString,
                        durationMs = -1, // Already recorded in on(Send)
                    )
                }
            }
        }
    }
