package com.realscanseries.rssample;

import android.app.Application;
import android.util.Log;

public class MyApplication extends Application {
    private static final String TAG = "MyApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "=== Application onCreate - Configuring SSL ===");
        
        // Configure SSL to trust all certificates BEFORE anything else
        // This must be done early to ensure SignalR uses the configured SSL context
        SSLHelper.trustAllCertificates();
        
        Log.d(TAG, "SSL configuration completed in Application.onCreate()");
    }
}

