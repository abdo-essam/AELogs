package com.ae.log.sample

import com.ae.log.AELog
import com.ae.log.plugins.analytics.AnalyticsPlugin
import com.ae.log.plugins.analytics.AnalyticsTracker
import com.ae.log.plugins.log.LogPlugin
import com.ae.log.plugins.network.NetworkPlugin
import com.ae.log.plugins.network.NetworkRecorder
import com.ae.log.plugins.network.interceptor.AELogKtorInterceptor
import io.ktor.client.HttpClient

/**
 * Singleton that holds plugin API references after [SampleApp.onCreate].
 *
 * Lives in commonMain so [App] (also commonMain) can access it without
 * using reified inline functions (which cause JVM target mismatches in KMP).
 *
 * APIs are `null` before init — all callers guard with `?.`.
 */
object SampleState {
    var networkApi: NetworkRecorder? = null
    var analyticsApi: AnalyticsTracker? = null

    /**
     * Real [HttpClient] created in [SampleApp] with [AELogKtorInterceptor] installed.
     * Every call made through this client is automatically captured by [NetworkPlugin].
     */
    var httpClient: HttpClient? = null
    private var isInitialized = false

    fun initialize() {
        if (isInitialized) return
        isInitialized = true

        AELog.init(
            LogPlugin(),
            NetworkPlugin(),
            AnalyticsPlugin(),
        )

        networkApi = AELog.plugin<NetworkPlugin>()?.recorder
        analyticsApi = AELog.plugin<AnalyticsPlugin>()?.tracker

        httpClient =
            HttpClient {
                install(AELogKtorInterceptor)
            }
    }
}
