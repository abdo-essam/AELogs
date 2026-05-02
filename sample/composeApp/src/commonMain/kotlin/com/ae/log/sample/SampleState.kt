package com.ae.log.sample

import com.ae.log.plugins.analytics.AnalyticsTracker
import com.ae.log.plugins.network.NetworkRecorder
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
}
