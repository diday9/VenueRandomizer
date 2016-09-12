package com.dids.venuerandomizer;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements LocationListener {
    private static final String TAG = "VenueRandomizer";
    private static final int PERMISSION_REQUEST_ACCESS_LOCATION = 1;

    /* Android Location */
    private LocationManager mLocationManager;
    private String mProvider;
    private double mLat;
    private double mLng;

    /* Foursquare API Constants */
    private final String FOURSQUARE_API = "https://api.foursquare.com/v2/venues/explore?";
    private final String SEARCH_CLIENT_ID = "client_id=";
    private final String SEARCH_CLIENT_SECRET = "&client_secret=";
    private final String SEARCH_LL = "&ll="; // latitute and longitude
    private final String SEARCH_VERSION = "&v="; // set to current date to get the current version of api
    private final String SEARCH_SORT_BY_DISTANCE = "&sortByDistance="; // 1 sort by distance instead of relevance
    private final String SEARCH_OPEN_NOW = "&openNow"; // 1 only to include open venues

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get Current date for API version
        // Format : YYYYMMDD
        Calendar today = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        final String currentDate = dateFormat.format(today.getTime());
        Log.d(TAG,currentDate);

        final Button find = (Button) findViewById(R.id.button_find);
        find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // AsyncTask fetch result from API
                URL searchUrl = null;
                try {
                    searchUrl = new URL(FOURSQUARE_API + SEARCH_CLIENT_ID + getString(R.string.client_id)
                            + SEARCH_CLIENT_SECRET + getString(R.string.client_secret)
                            + SEARCH_LL + mLat + "," + mLng
                            + SEARCH_VERSION + currentDate);
                } catch (MalformedURLException e) {
                    Log.d(TAG, "Malformed Foursquare search URL");
                }
                new FetchFromAPITask().execute(searchUrl);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Get runtime permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_ACCESS_LOCATION);
            } else {
                Log.v(TAG, "Location permission already granted.");
                getCurrentlocation();
                mLocationManager.requestLocationUpdates(mProvider, 400, 1, this);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
//        locationManager.removeUpdates(this);
    }

    private void getCurrentlocation() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Get if gps or network location is enabled
        boolean isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkLocationEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        // Check
        if (!isGPSEnabled && !isNetworkLocationEnabled) {
            // Show dialog to enable location
        } else {
            Criteria criteria = new Criteria();
            mProvider = mLocationManager.getBestProvider(criteria, false);
            Location location = mLocationManager.getLastKnownLocation(mProvider);

            if (location != null) {
                Log.d(TAG, "Location Provider: " + mProvider);

                onLocationChanged(location);
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mLat = location.getLatitude();
        mLng = location.getLongitude();
        Log.d(TAG, "Latitude: " + mLat);
        Log.d(TAG, "Longitude: " + mLng);

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String curProvider) {
        Log.d(TAG, "New Location Provider: " + curProvider);
    }

    @Override
    public void onProviderDisabled(String curProvider) {
        Log.d(TAG, "Disabled Provider: " + curProvider);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_ACCESS_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Access granted
                } else {
                    Log.d(TAG, "Location permission denied.");
                }
                return;
            }
        }
    }

    private class FetchFromAPITask extends AsyncTask<URL, Void, Void>{

        @Override
        protected Void doInBackground(URL... urls) {
            try{

                HttpURLConnection urlConnection = (HttpURLConnection) urls[0].openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                urlConnection.disconnect();
                String result = sb.toString();
                Log.d(TAG, "API search result = " + result);
            } catch (Exception e){
                Log.e(TAG, e.getMessage());
            }

            return null;
        }
    }
}
