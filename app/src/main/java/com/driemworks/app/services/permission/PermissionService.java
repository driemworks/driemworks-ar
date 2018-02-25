package com.driemworks.app.services.permission;

import android.app.Activity;

/**
 * Created by Tony on 4/22/2017.
 */

public interface PermissionService {

    /**
     * Request the permission
     * @param activity
     */
    void requestPermission(Activity activity);

    /**
     * Handle the device's response
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    void handleResponse(int requestCode, String[] permissions, int[] grantResults);

}
