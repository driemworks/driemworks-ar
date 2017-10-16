package com.driemworks.simplecv.services.permission.impl;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.driemworks.simplecv.services.permission.PermissionService;

/**
 * Created by Tony on 7/4/2017.
 */

public class LocationPermissionServiceImpl implements PermissionService {

    /** The code used to request the location (gps) service */
    public static final int REQUEST_CODE = 110;

    /** The tag used for logging */
    private static final String TAG = "GPSPermission: ";

    /** The default constructor */
    public LocationPermissionServiceImpl() {}

    /** Request permission upon instantiation */
    public LocationPermissionServiceImpl(Activity activity) {
        requestPermission(activity);
    }

    @Override
    public void requestPermission(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // try again to request the permission
            } else {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_CODE);
            }
        }
    }

    @Override
    public void handleResponse(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted!
                Log.i(TAG, "Camera permission is granted.");
            } else {
                Log.i(TAG, "Camera permission not granted.");
            }
            return;
        }
    }
}
