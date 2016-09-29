package com.dids.venuerandomizer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
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
import android.view.ViewGroup;
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
import java.util.Random;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "VenueRandomizer";

    private static final int PERMISSION_REQUEST_ACCESS_LOCATION = 1;

    /* Section */
    private String[] mSectionSpinnerArray;

    /* Android Location and Runtime Permission */
    private GoogleApiClient mGoogleApiClient;
    private double mLat;
    private double mLng;

    private int mHasCoarseLocPermission;
    private int mHasFineLocPermission;

    /*Foursquare API Search Result */
    private String mSearchResult = null;
    private String mVenue = null;

    /* Foursquare API Constants */
    private static final String FOURSQUARE_API = "https://api.foursquare.com/v2/venues/explore?";
    private static final String SEARCH_CLIENT_ID = "client_id=";
    private static final String SEARCH_CLIENT_SECRET = "&client_secret=";
    private static final String SEARCH_LL = "&ll="; // latitute and longitude
    private static final String SEARCH_VERSION = "&v="; // set to current date to get the current version of api
    private static final String SEARCH_SORT_BY_DISTANCE = "&sortByDistance=1"; // 1 sort by distance instead of relevance
    private static final String SEARCH_OPEN_NOW = "&openNow="; // 1 only to include open venues
    private static final String SEARCH_SECTION = "&section=";
    private static final String SEARCH_LIMIT = "&limit=50";

    private static final String SEARCH_ALL_VENUE_TYPE = "All";
    private static final String SEARCH_NO_SELETION_VENUE_TYPE = "Select location preference...";

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
       * As per https://developer.foursquare.com/overview/versioning,
       * Recommended to set a single date across API calls. Options for updates:
       * (1) Increase date every few months
       * (2) Check if Foursquare made an update, and update to this one
       * (3) Always get current date to get latest version from API (implemented below)
        // Get Current date for API version
        // Format : YYYYMMDD
        Calendar today = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        final String currentDate = dateFormat.format(today.getTime());
        Log.d(TAG,currentDate);
        */

        createGoogleApiClient();

        // Setup section spinner
        mSectionSpinnerArray = new String[] {SEARCH_NO_SELETION_VENUE_TYPE, SEARCH_ALL_VENUE_TYPE, "Food", "Drinks", "Coffee"};
        final Spinner sectionSpinner = (Spinner) findViewById(R.id.selection_spinner);
        final ArrayAdapter<String> sectionAdapter = new ArrayAdapter<String>(this,R.layout.spinner_item_list, mSectionSpinnerArray){
            @Override
            public boolean isEnabled(int position) {
                if(position == 0) {
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;
                if (position == 0){
                    textView.setTextColor(Color.GRAY);
                } else {
                    textView.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        sectionAdapter.setDropDownViewResource(R.layout.spinner_item_list);
        sectionSpinner.setAdapter(sectionAdapter);

        // Setup button
        final Button find = (Button) findViewById(R.id.button_find);
        find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // AsyncTask fetch result from API
                URL searchUrl = null;
                try {
                    String url = FOURSQUARE_API + SEARCH_CLIENT_ID + getString(R.string.client_id)
                            + SEARCH_CLIENT_SECRET + getString(R.string.client_secret)
                            + SEARCH_LL + mLat + "," + mLng
                            + SEARCH_VERSION + getString(R.string.api_version) //currentDate
                            + SEARCH_LIMIT
                            + SEARCH_SORT_BY_DISTANCE;
                    int selectionSection = sectionSpinner.getSelectedItemPosition();
                    if(selectionSection > 1){
                        // if spinner selection is not hint or All
                        url.concat(SEARCH_SECTION + selectionSection);
                    }
                    searchUrl = new URL(url);

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
            Log.d(TAG, "Requesting for location runtime permission");
            // Get runtime permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_ACCESS_LOCATION);
            } else {
                Log.v(TAG, "Location permission already granted");
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
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_ACCESS_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Access granted
                    Log.d(TAG, "Location permission has been granted");
                } else {
                    Log.d(TAG, "Location permission has been denied");
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
