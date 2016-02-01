package com.lumstic.ashoka.utils;

import android.app.Application;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;


public class AppController extends Application {
    private static AppController mInstance;
    private Preferences preferences;

    public static synchronized AppController getInstance() {
        return mInstance;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        doInit();
    }

    private void doInit() {
        mInstance = this;
        preferences = new Preferences(this);
    }


    public void showToast(String message) {
        if (null != message)
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public synchronized Preferences getPreferences() {
        return preferences;
    }

}