package com.lumstic.data.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtil {
    public static final int TYPE_NOT_CONNECTED = 0;
    public static final int TYPE_CONNECTED = 1;

    private NetworkUtil() {

    }

    public static int iSConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork) {

            return TYPE_CONNECTED;
        }
        return TYPE_NOT_CONNECTED;
    }


}