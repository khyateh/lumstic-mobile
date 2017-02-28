package com.lumstic.data.utils;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.crash.FirebaseCrash;
import com.lumstic.data.ui.SplashActivity;

import java.util.concurrent.Callable;

import io.fabric.sdk.android.Fabric;


public class AppController extends Application {
    private static AppController mInstance;
    private Preferences preferences;
    static Toast lastToast;
    static CountDownTimer lastToastTimer;
    //TODO Jyothi adding code for handling unhandled exception Feb 26 2017
    private static final String LUMSTIC_UNHANDLED_EXCEPTION_TAG = "lumstic_crashed_tag";
    private static final String LUMSTIC_APP_CRASHED = "lumstic_crashed";
    private static final String LUMSTIC_UNHANDLED_EXCEPTION = "lumstic_unhandled_exception";



    public static synchronized AppController getInstance() {
        return mInstance;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        doInit();
        //TODO Jyothi adding code for handling unhandled exception Feb 26 2017
        final Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler( new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable exception) {
                // Save the fact we crashed out.
                getSharedPreferences( LUMSTIC_UNHANDLED_EXCEPTION_TAG , Context.MODE_PRIVATE ).edit()
                        .putBoolean( LUMSTIC_APP_CRASHED, true ).apply();
                // Chain default exception handler.
                if ( defaultHandler != null ) {
                    FirebaseCrash.logcat(Log.ERROR, LUMSTIC_UNHANDLED_EXCEPTION_TAG,LUMSTIC_UNHANDLED_EXCEPTION);
                    FirebaseCrash.report(exception);
                    FirebaseCrash.log(exception.getStackTrace().toString());
                    defaultHandler.uncaughtException( thread, exception );
                }
            }
        } );

        boolean bRestartAfterCrash = getSharedPreferences( LUMSTIC_UNHANDLED_EXCEPTION_TAG , Context.MODE_PRIVATE )
                .getBoolean( LUMSTIC_APP_CRASHED, false );
        if ( bRestartAfterCrash ) {
            // Clear crash flag.
            getSharedPreferences( LUMSTIC_UNHANDLED_EXCEPTION_TAG , Context.MODE_PRIVATE ).edit()
                    .putBoolean( LUMSTIC_APP_CRASHED, false ).apply();
            // Re-launch from root activity with cleared stack.
            Intent intent = new Intent( this, SplashActivity.class );
            intent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK );
            startActivity( intent );
        }
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

    public void showToast(final String message, int seconds){
        showToast(message, seconds, false, null);
    }

    public void showToast(final String message, int seconds, final boolean countDown, final Callable<Integer> func){

        lastToast = Toast.makeText(this, message,Toast.LENGTH_SHORT);
        lastToast.show();

        cancelToastTimer();
        lastToastTimer = new CountDownTimer(seconds*1000, 1000)
        {
            public void onTick(long millisUntilFinished) {
                if(countDown) lastToast.setText(message + millisUntilFinished/1000);
                lastToast.show();
            }
            public void onFinish() {
                try {
                    func.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        lastToastTimer.start();
    }

    public synchronized Preferences getPreferences() {
        return preferences;
    }

}