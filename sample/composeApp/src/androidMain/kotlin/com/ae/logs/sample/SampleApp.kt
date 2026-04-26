package com.ae.logs.sample

import android.app.Application
import com.ae.logs.AELogsSetup
import com.ae.logs.plugins.analytics.AnalyticsPlugin
import com.ae.logs.plugins.logs.LogsPlugin
import com.ae.logs.plugins.network.NetworkPlugin

class SampleApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Create plugins first so we can extract their APIs before installing
        val networkPlugin = NetworkPlugin()
        val analyticsPlugin = AnalyticsPlugin()

        AELogsSetup.init(
            plugins =
                listOf(
                    LogsPlugin(),
                    networkPlugin,
                    analyticsPlugin,
                ),
        )

        // Expose APIs to commonMain via SampleState — avoids reified inline calls in App.kt
        SampleState.networkApi = networkPlugin.api
        SampleState.analyticsApi = analyticsPlugin.api
    }
}
