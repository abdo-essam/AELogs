package com.ae.log.sample

import android.app.Application

class SampleApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SampleState.initialize()
    }
}
