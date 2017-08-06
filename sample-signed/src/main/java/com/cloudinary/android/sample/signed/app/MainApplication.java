package com.cloudinary.android.sample.signed.app;


import android.app.Application;

import com.cloudinary.android.CldAndroid;
import com.cloudinary.android.LogLevel;
import com.cloudinary.android.sample.rest.BackendServerSignatureProvider;

public class MainApplication extends Application {
    static MainApplication _instance;

    public static MainApplication get() {
        return _instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // This can be called any time regardless of initialization.
        CldAndroid.setLogLevel(LogLevel.DEBUG);

        // Mandatory - call a flavor of init. Config can be null if cloudinary_url is provided in the manifest.
        CldAndroid.init(this, new BackendServerSignatureProvider(), null);

        _instance = this;
    }
}
