package com.dids.venuerandomizer;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements LocationListener {
    private static final String TAG = "VenueRandomizer";
    private LocationManager locationManager;
    private String provider;
    private static final int PERMISSION_REQUEST_ACCESS_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if((int) Build.VERSION.SDK_INT >= 23){
            // Get runtime permission
            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission_group.LOCATION);
            if(ContextCompat.checkSelfPermission(this,Manifest.permission_group.LOCATION)
                    != PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission_group.LOCATION)){
                    // Show dialog
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission_group.LOCATION},
                            PERMISSION_REQUEST_ACCESS_LOCATION);
                }
            }
            else
            {
                Log.v(TAG, "Location permission already granted.");

            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(provider, 400, 1, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    private void getCurrentlocation(){
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Get if gps or network location is enabled
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkLocationEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        // Check
        if(!isGPSEnabled && !isNetworkLocationEnabled){
            // Show dialog to enable location
        }
        else {
            Criteria criteria = new Criteria();
            provider = locationManager.getBestProvider(criteria, false);
            Location location = locationManager.getLastKnownLocation(provider);

            if (location != null) {
                Log.v(TAG, "Location Provider: " + provider);

                onLocationChanged(location);
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        int lat = (int) (location.getLatitude());
        int lng = (int) (location.getLongitude());
        Log.v(TAG,"Latitude: " + lat);
        Log.v(TAG,"Longitude: " + lng);

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String curProvider) {
        Log.v(TAG,"New Location Provider: " + curProvider);
    }

    @Override
    public void onProviderDisabled(String curProvider) {
        Log.v(TAG,"Disabled Provider: " + curProvider);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSION_REQUEST_ACCESS_LOCATION: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    // Access granted
                }
                else {
                    Log.v(TAG, "Runtime permission denied.")
                }
                return;
            }
        }
    }
}
