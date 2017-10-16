package com.driemworks.simplecv.services.permission.impl;

import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.app.Activity;

import com.driemworks.simplecv.services.permission.PermissionService;

/**
 * Created by Tony on 4/22/2017.
 */

public class CameraPermissionServiceImpl implements PermissionService {

    /** The code used to request the camera */ // TODO move to enum!
    public static final int REQUEST_CODE = 101;

    /** The logcat tag */
    private static final String TAG = "CamraPrmissionSrvice: ";

    /** The default constructor */
    public CameraPermissionServiceImpl() {}

    /**
     * Request permission upon instantiation
     * @param activity
     */
    public CameraPermissionServiceImpl(Activity activity) {
        requestPermission(activity);
    }

    /**
     * Request permission to use the camera
     * @param activity
     */
    @Override
    public void requestPermission(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity,
                android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    android.Manifest.permission.CAMERA)) {
                // try again to request the permission
            } else {
                ActivityCompat.requestPermissions(activity,
                        new String[]{android.Manifest.permission.CAMERA},
                        REQUEST_CODE);
            }
        }
    }

    /**
     * Handle a response to request for permission to use the camera.
     * This should be called from the activity in the onRequestPermissionsResult method
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
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
