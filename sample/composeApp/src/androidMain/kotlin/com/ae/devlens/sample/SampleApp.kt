package com.ae.devlens.sample

import android.app.Application
import com.ae.devlens.DevLensSetup

class SampleApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // ✅ Init once at app start — proper lifecycle, not in Composable
        DevLensSetup.init()
    }
}
