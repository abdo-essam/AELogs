package com.ae.logs.sample

import android.app.Application
import com.ae.logs.AELogs
import com.ae.logs.plugins.analytics.AnalyticsPlugin
import com.ae.logs.plugins.logs.LogsPlugin
import com.ae.logs.plugins.network.NetworkPlugin

class SampleApp : Application() {
    override fun onCreate() {
        super.onCreate()

        AELogs.init(
            LogsPlugin(),
            NetworkPlugin(),
            AnalyticsPlugin(),
        )

        SampleState.networkApi = AELogs.plugin<NetworkPlugin>()?.api
        SampleState.analyticsApi = AELogs.plugin<AnalyticsPlugin>()?.api
    }
}
