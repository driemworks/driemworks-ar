package com.driemworks.sensor.listeners;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

/**
 * Created by Tony on 7/4/2017.
 */

public class DWLocationListener implements LocationListener {

    protected Location lastLocation;

    public DWLocationListener(String provider) {
        lastLocation = new Location(provider);
    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation.set(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO
    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO
    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO
    }
}
