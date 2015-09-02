package lumstic.ashoka.com.lumstic.UI;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import lumstic.ashoka.com.lumstic.R;
import lumstic.ashoka.com.lumstic.Utils.LumsticApp;


public class BaseActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    public static double latitude = 0, longitude = 0;
    Context mContext;
    String baseUrl;
    public static boolean mIsInForegroundMode;
    private static final String TAG = BaseActivity.class.getSimpleName();
    GoogleCloudMessaging gcm;
    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    // boolean flag to toggle periodic location updates
    public static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private LocationRequest mLocationRequest;
    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 60000; // 60 sec
    private static int FASTEST_INTERVAL = 30000; // 30 sec
    private static int DISPLACEMENT = 100; // 100 meters
    LumsticApp lumsticApp;

    /**
     * on start
     */
    @Override
    protected void onStart() {
        Log.d(TAG, "BASE ACTIVITY START CALLED");
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        lumsticApp = (LumsticApp) getApplication();

        if (lumsticApp.getPreferences().getBaseUrl() == null) {
            baseUrl = getResources().getString(R.string.server_url);
        } else
            baseUrl = lumsticApp.getPreferences().getBaseUrl();

        if (checkPlayServices()) {

            // Building the GoogleApi client
            buildGoogleApiClient();
            createLocationRequest();
            gcm = GoogleCloudMessaging.getInstance(this);

        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
            return;
        }
    }

    /**
     * checking Google play services is available or not
     *
     * @return
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }


    @Override
    protected void onResume() {
        super.onResume();


        mIsInForegroundMode = true;
        // Resuming the periodic location updates.
        if (mGoogleApiClient != null) {
            if (mGoogleApiClient.isConnected() && mIsInForegroundMode) {
                startLocationUpdates();
                Log.d(TAG, "Location update resumed .....................");
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsInForegroundMode = false;
        if (mGoogleApiClient != null) {
            if (mGoogleApiClient.isConnected()) {
                stopLocationUpdates();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
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
     * Creating google api client object
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Creating location request object
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    /**
     * Starting the location updates
     */
    protected void startLocationUpdates() {

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        Log.d(TAG, "Location update started .....................");
    }

    /**
     * Stopping location updates
     */
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        Log.d(TAG, "Location update stopped .....................");
    }


    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {

        // Once connected with google api, get the location
        displayLocation();

        if (mIsInForegroundMode) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location
        mLastLocation = location;
        // Displaying the new location on UI
        displayLocation();
    }

    /**
     * Method to display the location on UI
     */
    private void displayLocation() {

        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();
            lumsticApp.getPreferences().setLatitude(String.valueOf(latitude));
            lumsticApp.getPreferences().setLongitude(String.valueOf(longitude));
        } else {
            latitude = 0;
            longitude = 0;
        }

        Log.e("TAG", "Latitude -->>>" + latitude + "/ Longitude -->>>" + longitude);
    }

}
