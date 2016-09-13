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
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "VenueRandomizer";
    private static final int PERMISSION_REQUEST_ACCESS_LOCATION = 1;

    private String[] mSectionSpinnerArray;

    /* Android Location */
    private GoogleApiClient mGoogleApiClient;
    private LocationManager mLocationManager;
    private String mProvider;
    private double mLat;
    private double mLng;

    /*Foursquare API Search Result */
    private String mSearchResult = null;
    private String mVenue = null;

    /* Foursquare API Constants */
    private static final String FOURSQUARE_API = "https://api.foursquare.com/v2/venues/explore?";
    private static final String SEARCH_CLIENT_ID = "client_id=";
    private static final String SEARCH_CLIENT_SECRET = "&client_secret=";
    private static final String SEARCH_LL = "&ll="; // latitute and longitude
    private static final String SEARCH_VERSION = "&v="; // set to current date to get the current version of api
    private static final String SEARCH_SORT_BY_DISTANCE = "&sortByDistance="; // 1 sort by distance instead of relevance
    private static final String SEARCH_OPEN_NOW = "&openNow="; // 1 only to include open venues
    private static final String SEARCH_SECTION = "&section=";
    private static final String SEARCH_LIMIT = "&limit=50";

    private static final String RESULT_RESPONSE = "response";
    private static final String RESULT_GROUPS = "groups";
    private static final String RESULT_ITEMS = "items";
    private static final String RESULT_VENUE = "venue";
    private static final String RESULT_NAME = "name";
    private static final String RESULT_META = "meta";
    private static final String RESULT_CODE = "code";

    private static final String DISPLAY_ERROR_MESSAGE = "Sorry, cannot generate random venue. Please try again.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

      /**
       * Code block removed.
       * As per https://developer.foursquare.com/overview/versioning,
       * Recommended to set a single date across API calls. Options for updates:
       * (1) Increase date every few months
       * (2) Check if Foursquare made an update, and update to this one
       * (3) Always get current date (implemented below)
        // Get Current date for API version
        // Format : YYYYMMDD
        Calendar today = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        final String currentDate = dateFormat.format(today.getTime());
        Log.d(TAG,currentDate);
        */

        createGoogleApiClient();

        // Setup section spinner
        mSectionSpinnerArray = new String[] {"food", "drinks", "coffee"};
        final Spinner sectionSpinner = (Spinner) findViewById(R.id.selection_spinner);
        ArrayAdapter<String> sectionAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, mSectionSpinnerArray);
        sectionSpinner.setAdapter(sectionAdapter);

        // Setup button
        final Button find = (Button) findViewById(R.id.button_find);
        find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // AsyncTask fetch result from API
                URL searchUrl = null;
                try {
                    String selectionSection = sectionSpinner.getSelectedItem().toString();
                    searchUrl = new URL(FOURSQUARE_API + SEARCH_CLIENT_ID + getString(R.string.client_id)
                            + SEARCH_CLIENT_SECRET + getString(R.string.client_secret)
                            + SEARCH_LL + mLat + "," + mLng
                            + SEARCH_VERSION + getString(R.string.api_version) /*currentDate*/
                            + SEARCH_SECTION + selectionSection
                            + SEARCH_LIMIT);
                    Log.d(TAG, "URL: " + searchUrl);
                    Log.d(TAG, "Searching nearby " + selectionSection + " venue");

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
                // Check if location is enabled
                //checkLocationOn();
                //mLocationManager.requestLocationUpdates(mProvider, 400, 1, this);
            }
        }
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        mLat = location.getLatitude();
        mLng = location.getLongitude();
        Log.d(TAG, "onLocationChanged Latitude: " + mLat);
        Log.d(TAG, "onLocationChanged Longitude: " + mLng);

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
                    Log.d(TAG, "Location permission granted.");
                } else {
                    Log.d(TAG, "Location permission denied.");
                }
                return;
            }
        }
    }

    private synchronized void createGoogleApiClient(){
        // Create instance of GoogleAPIClient
        if(mGoogleApiClient == null){
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

  /**
    * Checks if GPS or Network Location is enabled
    */
    private void checkLocationOn() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Get if gps or network location is enabled
        boolean isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkLocationEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        Log.d(TAG, "GPS enabled " + isGPSEnabled);
        Log.d(TAG, "Network Location enabled " + isNetworkLocationEnabled);
        // Check
        if (!isGPSEnabled && !isNetworkLocationEnabled) {
            // Show dialog to enable location
            Log.d(TAG, "GPS and Network Location disabled");
        } else {
            Criteria criteria = new Criteria();
            mProvider = mLocationManager.getBestProvider(criteria, false);
            Log.d(TAG, "Location Provider: " + mProvider);
            Location location = mLocationManager.getLastKnownLocation(mProvider);
            if (location != null) {
                // Get current location
                onLocationChanged(location);
            } else {
                Log.e(TAG, "Location is null");
            }
        }
    }

    private int generateRandomItemsIndex(int itemsLength){
        Random randomNum = new Random();
        return randomNum.nextInt(itemsLength);
    }

    /**
     * Read JSON result from API call
     */
    private void readJSON(){
        if(mSearchResult == null){
            Log.e(TAG, "Error in reading result");
            mVenue = DISPLAY_ERROR_MESSAGE;
            return;
        } else {
            JSONObject jsonResult;
            try{
                jsonResult = new JSONObject(mSearchResult);
                JSONObject jsonResponse = jsonResult.optJSONObject(RESULT_RESPONSE);
                // get first group
                JSONObject jsonGroups = jsonResponse.optJSONArray(RESULT_GROUPS).getJSONObject(0);
                // get item
                JSONArray jsonItems = jsonGroups.optJSONArray(RESULT_ITEMS);
                // get random item
                int itemsLength = jsonItems.length();
                Log.d(TAG, "Items length = " + itemsLength);
                int randomItemIndex = generateRandomItemsIndex(itemsLength);
                Log.d(TAG, "Random item number " + randomItemIndex);
                JSONObject jsonItemsObject = jsonItems.getJSONObject(randomItemIndex);
                // get venue
                JSONObject jsonVenue = jsonItemsObject.optJSONObject(RESULT_VENUE);
                mVenue = jsonVenue.optString(RESULT_NAME);

                Log.d(TAG, "Venue: " + mVenue);
            } catch (JSONException e){
                Log.e(TAG, e.getMessage());
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        mLat = mLastLocation.getLatitude();
        mLng = mLastLocation.getLongitude();
        Log.d(TAG, "Latitude = " + mLat);
        Log.d(TAG, "Longitude = " + mLng);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private class FetchFromAPITask extends AsyncTask<URL, Void, Void>{
        private static final String TAG = "FetchFromAPITask";
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
                mSearchResult = sb.toString();
                Log.d(TAG, "API search result = " + mSearchResult);

            } catch (Exception e){
                Log.e(TAG, e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            readJSON();

            if(mVenue != null){
                // Display Venue to Text View
                TextView venueTextView = (TextView) findViewById(R.id.txtview_result);
                venueTextView.setText(mVenue);
            }

        }
    }
}
