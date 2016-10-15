package com.dids.venuerandomizer;

import android.app.Application;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.Log;

import com.dids.venuerandomizer.controller.utility.PreferencesUtility;
import com.dids.venuerandomizer.controller.location.LocationManager;
import com.dids.venuerandomizer.controller.network.VolleySingleton;
import com.dids.venuerandomizer.model.Assets;
import com.dids.venuerandomizer.model.Venue;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import java.util.Locale;
import java.util.Random;

public class VenueRandomizerApplication extends Application {
    private static final String TAG = "VenueRandomizerApp";
    /* Food constants */
    private static final int MAX_FOOD = 5;
    private static final String FOOD_RESOURCE_ID = "bg_food%d";

    /* Drinks constants */
    private static final int MAX_DRINKS = 1;
    private static final String DRINKS_RESOURCE_ID = "bg_drinks%d";

    /* Coffee constants */
    private static final int MAX_COFFEE = 3;
    private static final String COFFEE_RESOURCE_ID = "bg_coffee%d";
    private static VenueRandomizerApplication mInstance;
    private Venue mVenue;
    private Assets mFoodAsset;
    private Assets mCoffeeAsset;
    private Assets mDrinkAsset;

    public static VenueRandomizerApplication getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        VolleySingleton.getInstance(this);
        LocationManager.getInstance(this);
        PreferencesUtility.getInstance().init(this);

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        Resources res = getResources();
        mFoodAsset = preloadResource(res, FOOD_RESOURCE_ID, MAX_FOOD);
        mDrinkAsset = preloadResource(res, DRINKS_RESOURCE_ID, MAX_DRINKS);
        mCoffeeAsset = preloadResource(res, COFFEE_RESOURCE_ID, MAX_COFFEE);
    }

    private Assets preloadResource(Resources res, String resourceFormat,
                                   int maxResourceCount) {
        Random random = new Random();
        String resourceString = String.format(Locale.getDefault(), resourceFormat,
                random.nextInt(maxResourceCount) + 1);
        int arrayId = res.getIdentifier(resourceString, "array", getPackageName());
        TypedArray array = res.obtainTypedArray(arrayId);
        //noinspection ResourceType
        String copyright = array.getString(0);
        //noinspection ResourceType
        String link = array.getString(1);
        String url;
        if (PreferencesUtility.getInstance().isHiResImageSupported()) {
            Log.d(TAG, "hi-res enabled");
            //noinspection ResourceType
            url = array.getString(2);
        } else {
            Log.d(TAG, "hi-res disabled");
            //noinspection ResourceType
            url = array.getString(3);
        }
        array.recycle();
        return new Assets(copyright, link, url);
    }

    public Venue getVenue() {
        return mVenue;
    }

    public void setVenue(Venue venue) {
        mVenue = venue;
    }

    public Assets getFoodAsset() {
        return mFoodAsset;
    }

    public Assets getCoffeeAsset() {
        return mCoffeeAsset;
    }

    public Assets getDrinksAsset() {
        return mDrinkAsset;
    }
}
