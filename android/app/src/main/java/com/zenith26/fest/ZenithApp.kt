package com.zenith26.fest

import android.app.Application
import com.zenith26.fest.data.remote.ApiClient

/**
 * Zenith '26 Fest Application entry point.
 */
class ZenithApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize dynamic API client with persistence
        ApiClient.init(this)
    }
}
