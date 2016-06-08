package com.lumstic.ashoka.utils;

import android.app.Application;
import android.os.CountDownTimer;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;


public class AppController extends Application {
    private static AppController mInstance;
    private Preferences preferences;
    static Toast lastToast;
    static CountDownTimer lastToastTimer;

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
            cancelToastTimer();
            lastToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
            lastToast.show();
    }

    public void cancelToast() {
        if (null != lastToast) {
            cancelToastTimer();
            lastToast.cancel();
            lastToast=null;
        }
    }

    private void cancelToastTimer(){
        if(lastToastTimer!=null) {
            lastToastTimer.cancel();
            lastToastTimer = null;
        }
    }

    public void showToast(String message, int seconds){

        lastToast = Toast.makeText(this, message,Toast.LENGTH_SHORT);
        lastToast.show();

        cancelToastTimer();
        lastToastTimer = new CountDownTimer(seconds*1000, 1000)
        {

            public void onTick(long millisUntilFinished) {lastToast.show();}
            public void onFinish() {lastToast.show();}

        };
        lastToastTimer.start();

    }

    public synchronized Preferences getPreferences() {
        return preferences;
    }

}