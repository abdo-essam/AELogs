@file:OptIn(kotlin.time.ExperimentalTime::class)
@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package com.ae.log.plugins.network.interceptor

import com.ae.log.AELog
import com.ae.log.plugins.network.NetworkPlugin
import com.ae.log.plugins.network.model.NetworkMethod
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.content.TextContent
import io.ktor.util.*
import kotlin.time.Clock

/**
 * Ktor Client interceptor that automatically records HTTP traffic to AELog.
 * 
 * Works with Ktor 2.x and 3.x.
 */
public class AELogKtorInterceptor internal constructor() {

    public companion object Plugin : HttpClientPlugin<Unit, AELogKtorInterceptor> {
        override val key: AttributeKey<AELogKtorInterceptor> = AttributeKey("AELogKtorInterceptor")

        private val RequestIdKey = AttributeKey<String>("AELogRequestId")
        private val StartTimeKey = AttributeKey<Long>("AELogStartTime")

        override fun prepare(block: Unit.() -> Unit): AELogKtorInterceptor = AELogKtorInterceptor()

        override fun install(plugin: AELogKtorInterceptor, scope: HttpClient) {
            val clock = Clock.System

            // Intercept Outgoing Requests
            scope.requestPipeline.intercept(HttpRequestPipeline.State) {
                if (!AELog.isEnabled) return@intercept
                
                val recorder = AELog.getPlugin<NetworkPlugin>()?.recorder ?: return@intercept
                val id = recorder.newId()
                
                context.attributes.put(RequestIdKey, id)
                context.attributes.put(StartTimeKey, clock.now().toEpochMilliseconds())

                val method = NetworkMethod.valueOf(context.method.value.uppercase()) ?: NetworkMethod.GET
                val headersMap = context.headers.build().entries().associate { it.key to it.value.joinToString(", ") }
                
                val bodyString = when (val body = context.body) {
                    is TextContent -> body.text
                    else -> null
                }

                recorder.startRequest(
                    id = id,
                    url = context.url.buildString(),
                    method = method,
                    headers = headersMap,
                    body = bodyString
                )
            }

            // Intercept Incoming Responses
            scope.receivePipeline.intercept(HttpReceivePipeline.State) { response ->
                if (!AELog.isEnabled) return@intercept
                
                val recorder = AELog.getPlugin<NetworkPlugin>()?.recorder ?: return@intercept
                val call = response.call
                val id = call.attributes.getOrNull(RequestIdKey) ?: return@intercept
                val start = call.attributes.getOrNull(StartTimeKey) ?: clock.now().toEpochMilliseconds()
                val duration = clock.now().toEpochMilliseconds() - start

                val headersMap = response.headers.entries().associate { it.key to it.value.joinToString(", ") }
                
                recorder.logResponse(
                    id = id,
                    statusCode = response.status.value,
                    headers = headersMap,
                    durationMs = duration
                )
            }
        }
    }
}

/**
 * Accessor for the Ktor Interceptor.
 */
public val KtorInterceptor: AELogKtorInterceptor.Plugin = AELogKtorInterceptor.Plugin
