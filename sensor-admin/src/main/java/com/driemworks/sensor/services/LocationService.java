package com.driemworks.sensor.services;

import android.app.Service;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.driemworks.sensor.Tags;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Class for making calls to the location (GPS + Network) API
 */
public class LocationService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = Tags.LocationService.getTag();

    private static final long UPDATE_INTERVAL = 0;
    private static final long FASTEST_UPDATE_INTERVAL = UPDATE_INTERVAL/2;

    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;

    private Context context;

    /** ///////////////////////////////////////// */
    private getLocation getLocation;

    public interface getLocation {
        public void onLocationChanged(Location location);
    }
    /** ///////////////////////////////////////// */

    /**
     *
     * @param context
     */
    public LocationService(Context context) {
        this.context = context;
        buildGoogleApiClient();
    }


    private void startLocationUpdates() throws SecurityException {
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    /**
     *
     */
    private void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
    }

    /**
     *
     */
    private void createRelationshipRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
