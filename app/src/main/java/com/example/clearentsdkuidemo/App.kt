package com.example.clearentsdkuidemo

import android.app.Application
import com.clearent.idtech.android.wrapper.SDKWrapper

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize the sdk
        initSdk()
    }

    private fun initSdk() = SDKWrapper.initializeReader(
        applicationContext,
        Constants.BASE_URL_SANDBOX,
        Constants.PUBLIC_KEY_SANDBOX,
        Constants.API_KEY_SANDBOX
    )
}