package com.lumstic.ashoka.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import com.lumstic.ashoka.R;
import com.lumstic.ashoka.utils.AppController;


public class BaseActivity extends Activity implements LocationListener {
    // boolean flag to toggle periodic location updates
    public static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = BaseActivity.class.getSimpleName();
    public static String baseUrl, userBaseUrl;
    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 5000; // 5 sec
    private static int FASTEST_INTERVAL = 2000; // 2 sec
    private static int REQUEST_DURATION = 60000; // 60 sec
    boolean mIsInForegroundMode;
    Context mContext;
    AppController appController;
    private double latitude = 0, longitude = 0;
    LocationManager locationManager;

    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";
    // Bool to track whether the app is already resolving an error
    private boolean mResolvingError = false;

    protected boolean enableLocation = false;
    protected boolean locationReceived = false;
    private boolean locationRequested = false;

    /**
     * on start
     */
    @Override
    protected void onStart() {
        Log.d(TAG, "BASE ACTIVITY START CALLED");
        super.onStart();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        appController = AppController.getInstance();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        //create location objects if enabled
        if (enableLocation) {
            startLocationUpdates();

            //clear previous location
            appController.getPreferences().setLatitude(String.valueOf(latitude));
            appController.getPreferences().setLongitude(String.valueOf(longitude));
        }

        //set base urls
        com.lumstic.ashoka.utils.Preferences prefs = appController.getPreferences();
        baseUrl = (prefs.getBaseUrl() == null) ? getResources().getString(R.string.server_url) : prefs.getBaseUrl();
        userBaseUrl = (prefs.getUserBaseUrl() == null) ? getResources().getString(R.string.user_server_url) : prefs.getUserBaseUrl();

    }

    @Override
    protected void onResume() {
        super.onResume();

        mIsInForegroundMode = true;
        // Resuming the periodic location updates.
        if (enableLocation && mIsInForegroundMode) {
            startLocationUpdates();
            Log.d(TAG, "Location update resumed .....................");
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsInForegroundMode = false;
        if (enableLocation) {
            stopLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        final int keycode = event.getKeyCode();
        final int action = event.getAction();
        if (keycode == KeyEvent.KEYCODE_MENU && action == KeyEvent.ACTION_UP) {
            return true; // consume the key press
        }
        return super.dispatchKeyEvent(event);
    }

    /**
     * Method to display the location on UI
     */
    private void saveLocation(Location location) {

        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            appController.getPreferences().setLatitude(String.valueOf(latitude));
            appController.getPreferences().setLongitude(String.valueOf(longitude));
            locationReceived = true;
            if(locationRequested) {
                //appController.showToast(location.toString());
                appController.showToast("Location saved");
                locationRequested=false;
                onLocationReceived("",0);
            }
        } else {
            latitude = 0;
            longitude = 0;
        }

        Log.e("TAG", "Latitude -->>>" + latitude + "/ Longitude -->>>" + longitude);
    }

    protected void onLocationReceived(String s, int i) {
    }

    protected void requestLocation(String s, int i) {
        if(locationReceived) {
            locationRequested=false;
            onLocationReceived(s,i);
        }else{
            locationRequested=true;
            appController.showToast("Waiting for location ...");
            stopLocationUpdates();
            startLocationUpdates();
        }
    }

    /**
     * Starting the location updates
     */
    protected void startLocationUpdates() {
        Log.d(TAG, "Location update started .....................");
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 50, 0, this);
    }

    /**
     * Stopping location updates
     */
    protected void stopLocationUpdates() {
        Log.d(TAG, "Location update stopped .....................");
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        stopLocationUpdates();
        saveLocation(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String s){
    }

    @Override
    public void onProviderDisabled(String provider) {

        if(LocationManager.GPS_PROVIDER.equals(provider)){
            stopLocationUpdates();
            showEnableGPSDialog();
        }
    }


    private void showEnableGPSDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setMessage("GPS is disabled in your device.\nWithout GPS you will not be able to create a survey response.")
                .setCancelable(false)
                .setPositiveButton("Enable GPS",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }


}

