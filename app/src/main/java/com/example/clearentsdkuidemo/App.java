package com.example.clearentsdkuidemo;

import android.app.Application;

import com.clearent.idtech.android.wrapper.ClearentWrapper;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        initSdkWrapper();
    }

    private void initSdkWrapper() {
        ClearentWrapper.INSTANCE.initializeSDK(
                getApplicationContext(),
                Constants.BASE_URL_SANDBOX,
                Constants.PUBLIC_KEY_SANDBOX,
                Constants.API_KEY_SANDBOX,
                true // should be true otherwise the ClearentSDKUI does not show any messages
        );
    }
}
