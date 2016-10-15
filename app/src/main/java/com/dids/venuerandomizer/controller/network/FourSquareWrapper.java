package com.dids.venuerandomizer.controller.network;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.RequestFuture;
import com.dids.venuerandomizer.R;
import com.dids.venuerandomizer.controller.utility.PreferencesUtility;
import com.dids.venuerandomizer.model.Category;
import com.dids.venuerandomizer.model.Venue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class FourSquareWrapper {
    public static final String SECTION_FOOD = "food";
    public static final String SECTION_DRINKS = "drinks";
    public static final String SECTION_COFFEE = "coffee";

    private static final String TAG = "FourSquareWrapper";
    private static final int MAX_UNIQUE_RETRY = 5;

    /* Foursquare API Constants */
    private static final String FOURSQUARE_SEARCH_URL = "https://api.foursquare.com/v2/venues/explore?";
    private static final String FOURSQUARE_PHOTO_URL = "https://api.foursquare.com/v2/venues/%s/photos?";
    private static final String SEARCH_CLIENT_INFO = "client_id=%s&client_secret=%s";
    private static final String SEARCH_LOCATION = "&ll=%s,%s";
    private static final String SEARCH_VERSION = "&v=%s";
    private static final String SEARCH_SORT_BY_DISTANCE = "&sortByDistance=1";
    private static final String SEARCH_OPEN_NOW = "&openNow=1";
    private static final String SEARCH_SECTION = "&section=%s";
    private static final String SEARCH_LOCALE = "&locale=%s";

    /* JSON tags */
    private static final String TAG_RESPONSE = "response";
    private static final String TAG_GROUPS = "groups";
    private static final String TAG_ITEMS = "items";
    private static final String TAG_VENUE = "venue";
    private static final String TAG_ID = "id";
    private static final String TAG_NAME = "name";
    private static final String TAG_CONTACT = "contact";
    private static final String TAG_PHONE = "phone";
    private static final String TAG_FORMATTED_PHONE = "formattedPhone";
    private static final String TAG_TWITTER = "twitter";
    private static final String TAG_FACEBOOK = "facebook";
    private static final String TAG_FACEBOOK_USERNAME = "facebookUsername";
    private static final String TAG_FACEBOOK_NAME = "facebookName";
    private static final String TAG_LOCATION = "location";
    private static final String TAG_ADDRESS = "address";
    private static final String TAG_LONGITUDE = "lat";
    private static final String TAG_LATITUDE = "lng";
    private static final String TAG_DISTANCE = "distance";
    private static final String TAG_POSTAL_CODE = "postalCode";
    private static final String TAG_COUNTRY_CODE = "cc";
    private static final String TAG_CITY = "city";
    private static final String TAG_STATE = "state";
    private static final String TAG_COUNTRY = "country";
    private static final String TAG_CATEGORIES = "categories";
    private static final String TAG_PLURAL_NAME = "pluralName";
    private static final String TAG_SHORT_NAME = "shortName";
    private static final String TAG_ICON = "icon";
    private static final String TAG_PREFIX = "prefix";
    private static final String TAG_SUFFIX = "suffix";
    private static final String TAG_PRIMARY = "primary";
    private static final String TAG_URL = "url";
    private static final String TAG_RATING = "rating";
    private static final String TAG_HOURS = "hours";
    private static final String TAG_STATUS = "status";
    private static final String TAG_IS_OPEN = "isOpen";
    private static final String TAG_PHOTOS = "photos";

    private final Context mContext;

    public FourSquareWrapper(Context context) {
        mContext = context;
    }

    public Venue getRandomVenue(Location location, String section) throws NoConnectionError {
        StringBuilder builder = new StringBuilder();
        builder.append(FOURSQUARE_SEARCH_URL);
        builder.append(String.format(SEARCH_CLIENT_INFO, mContext.getString(R.string.client_id),
                mContext.getString(R.string.client_secret)));
        builder.append(String.format(SEARCH_LOCATION, String.valueOf(location.getLatitude()),
                String.valueOf(location.getLongitude())));
        builder.append(String.format(SEARCH_VERSION, mContext.getString(R.string.api_version)));
        builder.append(SEARCH_SORT_BY_DISTANCE);
        builder.append(SEARCH_OPEN_NOW);
        builder.append(String.format(SEARCH_SECTION, section));
        builder.append(String.format(SEARCH_LOCALE, getCurrentLocale()));
        Log.d(TAG, builder.toString());

        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, builder.toString(),
                null, future, future);
        VolleySingleton.getInstance(mContext).addToRequestQueue(jsonRequest);
        try {
            JSONObject response = future.get();
            JSONArray groupArray = response.getJSONObject(TAG_RESPONSE).getJSONArray(TAG_GROUPS);
            JSONArray itemArray = groupArray.getJSONObject(0).getJSONArray(TAG_ITEMS);
            Random random = new Random();
            JSONObject venueObject = itemArray.getJSONObject(random.nextInt(itemArray.
                    length())).getJSONObject(TAG_VENUE);
            Venue venue = new Venue();
            getVenueDetails(venueObject, venue);
            return venue;
        } catch (InterruptedException | ExecutionException | JSONException e) {
            Log.e(TAG, "Error in get venue list request: " + e.getMessage());
            if (e.getCause() instanceof NoConnectionError) {
                throw (NoConnectionError) e.getCause();
            }
        }
        return null;
    }

    public Venue getRandomVenue(String id, Location location, String section) throws NoConnectionError {
        StringBuilder builder = new StringBuilder();
        builder.append(FOURSQUARE_SEARCH_URL);
        builder.append(String.format(SEARCH_CLIENT_INFO, mContext.getString(R.string.client_id),
                mContext.getString(R.string.client_secret)));
        builder.append(String.format(SEARCH_LOCATION, String.valueOf(location.getLatitude()),
                String.valueOf(location.getLongitude())));
        builder.append(String.format(SEARCH_VERSION, mContext.getString(R.string.api_version)));
        builder.append(SEARCH_SORT_BY_DISTANCE);
        builder.append(SEARCH_OPEN_NOW);
        builder.append(String.format(SEARCH_SECTION, section));
        builder.append(String.format(SEARCH_LOCALE, getCurrentLocale()));
        Log.d(TAG, builder.toString());

        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, builder.toString(),
                null, future, future);
        VolleySingleton.getInstance(mContext).addToRequestQueue(jsonRequest);
        try {
            JSONObject response = future.get();
            JSONArray groupArray = response.getJSONObject(TAG_RESPONSE).getJSONArray(TAG_GROUPS);
            JSONArray itemArray = groupArray.getJSONObject(0).getJSONArray(TAG_ITEMS);
            Venue venue = new Venue();
            Random random = new Random();
            for (int retryCount = 0; retryCount < MAX_UNIQUE_RETRY; retryCount++) {
                JSONObject venueObject = itemArray.getJSONObject(random.nextInt(itemArray.
                        length())).getJSONObject(TAG_VENUE);
                getVenueDetails(venueObject, venue);
                if (!venue.getId().equals(id)) {
                    break;
                }
            }
            return venue;
        } catch (InterruptedException | ExecutionException | JSONException e) {
            Log.e(TAG, "Error in get venue list request: " + e.getMessage());
            if (e.getCause() instanceof NoConnectionError) {
                throw (NoConnectionError) e.getCause();
            }
        }
        return null;
    }

    public List<Venue> getVenueList(Location location, String section) throws NoConnectionError {
        StringBuilder builder = new StringBuilder();
        builder.append(FOURSQUARE_SEARCH_URL);
        builder.append(String.format(SEARCH_CLIENT_INFO, mContext.getString(R.string.client_id),
                mContext.getString(R.string.client_secret)));
        builder.append(String.format(SEARCH_LOCATION, String.valueOf(location.getLatitude()),
                String.valueOf(location.getLongitude())));
        builder.append(String.format(SEARCH_VERSION, mContext.getString(R.string.api_version)));
        builder.append(SEARCH_SORT_BY_DISTANCE);
        builder.append(SEARCH_OPEN_NOW);
        builder.append(String.format(SEARCH_SECTION, section));
        builder.append(String.format(SEARCH_LOCALE, getCurrentLocale()));
        Log.d(TAG, builder.toString());

        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, builder.toString(),
                null, future, future);
        VolleySingleton.getInstance(mContext).addToRequestQueue(jsonRequest);
        try {
            List<Venue> venueList = new ArrayList<>();
            JSONObject response = future.get();
            JSONArray groupArray = response.getJSONObject(TAG_RESPONSE).getJSONArray(TAG_GROUPS);
            for (int i = 0; i < groupArray.length(); i++) {
                JSONArray itemArray = groupArray.getJSONObject(i).getJSONArray(TAG_ITEMS);
                for (int j = 0; j < itemArray.length(); j++) {
                    JSONObject venueObject = itemArray.getJSONObject(j).getJSONObject(TAG_VENUE);
                    Venue venue = new Venue();
                    getVenueDetails(venueObject, venue);
                    venueList.add(venue);
                }
            }
            return venueList;
        } catch (InterruptedException | ExecutionException | JSONException e) {
            Log.e(TAG, "Error in get venue list request: " + e.getMessage());
            if (e.getCause() instanceof NoConnectionError) {
                throw (NoConnectionError) e.getCause();
            }
        }
        return null;
    }

    private void getVenueDetails(JSONObject venueObject, Venue venue) throws JSONException,
            NoConnectionError {
        if (venueObject.has(TAG_ID)) {
            venue.setId(venueObject.getString(TAG_ID));
        }
        if (venueObject.has(TAG_NAME)) {
            venue.setName(venueObject.getString(TAG_NAME));
        }

        if (venueObject.has(TAG_CONTACT)) {
            JSONObject contactObject = venueObject.getJSONObject(TAG_CONTACT);
            setContact(contactObject, venue);
        }

        if (venueObject.has(TAG_LOCATION)) {
            JSONObject locationObject = venueObject.getJSONObject(TAG_LOCATION);
            setLocation(locationObject, venue);
        }

        if (venueObject.has(TAG_CATEGORIES)) {
            JSONArray categoryArray = venueObject.getJSONArray(TAG_CATEGORIES);
            setCategories(categoryArray, venue);
        }

        if (venueObject.has(TAG_URL)) {
            venue.setUrl(venueObject.getString(TAG_URL));
        }
        if (venueObject.has(TAG_RATING)) {
            venue.setRating(venueObject.getDouble(TAG_RATING));
        }

        if (venueObject.has(TAG_HOURS)) {
            JSONObject hoursObject = venueObject.getJSONObject(TAG_HOURS);
            setHours(hoursObject, venue);
        }
        fetchImages(venue);
    }

    private void setContact(JSONObject object, Venue venue) {
        try {
            if (object.has(TAG_PHONE)) {
                venue.setPhone(object.getString(TAG_PHONE));
            }
            if (object.has(TAG_FORMATTED_PHONE)) {
                venue.setFormattedPhone(object.getString(TAG_FORMATTED_PHONE));
            }
            if (object.has(TAG_TWITTER)) {
                venue.setTwitter(object.getString(TAG_TWITTER));
            }
            if (object.has(TAG_FACEBOOK)) {
                venue.setFacebook(object.getString(TAG_FACEBOOK));
            }
            if (object.has(TAG_FACEBOOK_USERNAME)) {
                venue.setFacebookUsername(object.getString(TAG_FACEBOOK_USERNAME));
            }
            if (object.has(TAG_FACEBOOK_NAME)) {
                venue.setFacebookName(object.getString(TAG_FACEBOOK_NAME));
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void setLocation(JSONObject object, Venue venue) {
        try {
            if (object.has(TAG_ADDRESS)) {
                venue.setAddress(object.getString(TAG_ADDRESS));
            }
            if (object.has(TAG_LATITUDE)) {
                venue.setLatitude(object.getDouble(TAG_LATITUDE));
            }
            if (object.has(TAG_LONGITUDE)) {
                venue.setLongitude(object.getDouble(TAG_LONGITUDE));
            }
            if (object.has(TAG_DISTANCE)) {
                venue.setDistance(object.getInt(TAG_DISTANCE));
            }
            if (object.has(TAG_POSTAL_CODE)) {
                venue.setPostalCode(object.getString(TAG_POSTAL_CODE));
            }
            if (object.has(TAG_COUNTRY_CODE)) {
                venue.setCountryCode(object.getString(TAG_COUNTRY_CODE));
            }
            if (object.has(TAG_CITY)) {
                venue.setCity(object.getString(TAG_CITY));
            }
            if (object.has(TAG_STATE)) {
                venue.setState(object.getString(TAG_STATE));
            }
            if (object.has(TAG_COUNTRY)) {
                venue.setCountry(object.getString(TAG_COUNTRY));
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void setCategories(JSONArray array, Venue venue) {
        try {
            for (int index = 0; index < array.length(); index++) {
                JSONObject categoryObject = array.getJSONObject(index);
                Category category = new Category();
                if (categoryObject.has(TAG_ID)) {
                    category.setId(categoryObject.getString(TAG_ID));
                }
                if (categoryObject.has(TAG_NAME)) {
                    category.setName(categoryObject.getString(TAG_NAME));
                }
                if (categoryObject.has(TAG_PLURAL_NAME)) {
                    category.setPluralName(categoryObject.getString(TAG_PLURAL_NAME));
                }
                if (categoryObject.has(TAG_SHORT_NAME)) {
                    category.setShortName(categoryObject.getString(TAG_SHORT_NAME));
                }
                if (categoryObject.has(TAG_ICON)) {
                    JSONObject iconObject = categoryObject.getJSONObject(TAG_ICON);
                    if (iconObject.has(TAG_PREFIX)) {
                        category.setIconPrefix(iconObject.getString(TAG_PREFIX));
                    }
                    if (iconObject.has(TAG_SUFFIX)) {
                        category.setIconSuffix(iconObject.getString(TAG_SUFFIX));
                    }
                }
                if (categoryObject.has(TAG_PRIMARY)) {
                    category.setPrimary(categoryObject.getBoolean(TAG_PRIMARY));
                }
                venue.addCategory(category);
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void setHours(JSONObject hoursObject, Venue venue) {
        try {
            if (hoursObject.has(TAG_STATUS)) {
                venue.setStatus(hoursObject.getString(TAG_STATUS));
            }
            if (hoursObject.has(TAG_IS_OPEN)) {
                venue.setIsOpen(hoursObject.getBoolean(TAG_IS_OPEN));
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void fetchImages(Venue venue) throws NoConnectionError {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format(FOURSQUARE_PHOTO_URL, venue.getId()));
        builder.append(String.format(SEARCH_CLIENT_INFO, mContext.getString(R.string.client_id),
                mContext.getString(R.string.client_secret)));
        builder.append(String.format(SEARCH_VERSION, mContext.getString(R.string.api_version)));
        builder.append(String.format(SEARCH_LOCALE, getCurrentLocale()));
        Log.d(TAG, builder.toString());

        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, builder.toString(),
                null, future, future);
        VolleySingleton.getInstance(mContext).addToRequestQueue(jsonRequest);
        try {
            JSONObject response = future.get();
            JSONArray itemArray = response.getJSONObject(TAG_RESPONSE).
                    getJSONObject(TAG_PHOTOS).getJSONArray(TAG_ITEMS);

            for (int index = 0; index < itemArray.length() && index < PreferencesUtility.
                    getInstance().getMaxImageCount(); index++) {
                venue.addPhotoUrl(itemArray.getJSONObject(index).getString(TAG_PREFIX) +
                        "original" + itemArray.getJSONObject(index).getString(TAG_SUFFIX));
            }
        } catch (InterruptedException | ExecutionException | JSONException e) {
            Log.e(TAG, "Error in fetch photo request: " + e.getMessage());
            if (e.getCause() instanceof NoConnectionError) {
                throw (NoConnectionError) e.getCause();
            }
        }
    }

    private String getCurrentLocale() {
        Log.d(TAG, Locale.getDefault().getLanguage());
        return Locale.getDefault().getLanguage();
    }
}
