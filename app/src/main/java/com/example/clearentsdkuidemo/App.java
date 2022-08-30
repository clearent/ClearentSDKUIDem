package com.example.clearentsdkuidemo;

import android.app.Application;

import com.clearent.idtech.android.wrapper.SDKWrapper;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        initSdk();
    }

    private void initSdk() {
        SDKWrapper.INSTANCE.initializeReader(
                getApplicationContext(),
                Constants.BASE_URL_SANDBOX,
                Constants.PUBLIC_KEY_SANDBOX,
                Constants.API_KEY_SANDBOX
        );
    }
}
