package com.dids.venuerandomizer.controller.utility;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.Log;

import com.dids.venuerandomizer.model.Assets;

import java.util.Locale;
import java.util.Random;

public class AssetUtility {
    /* Food constants */
    private static final int MAX_FOOD = 6;
    private static final String FOOD_RESOURCE_ID = "bg_food%d";

    /* Drinks constants */
    private static final int MAX_DRINKS = 1;
    private static final String DRINKS_RESOURCE_ID = "bg_drinks%d";

    /* Coffee constants */
    private static final int MAX_COFFEE = 5;
    private static final String COFFEE_RESOURCE_ID = "bg_coffee%d";

    @SuppressLint("StaticFieldLeak")
    private static AssetUtility mSingleton;
    private Context mContext;

    public static AssetUtility getInstance() {
        if (mSingleton == null) {
            mSingleton = new AssetUtility();
        }
        return mSingleton;
    }

    public void init(Context context) {
        mContext = context;
    }

    public Assets getFoodAsset() {
        Random random = new Random();
        String resourceString = String.format(Locale.getDefault(), FOOD_RESOURCE_ID,
                random.nextInt(MAX_FOOD) + 1);
        return getAsset(resourceString);
    }

    public Assets getDrinksAsset() {
        Random random = new Random();
        String resourceString = String.format(Locale.getDefault(), DRINKS_RESOURCE_ID,
                random.nextInt(MAX_DRINKS) + 1);
        return getAsset(resourceString);
    }

    public Assets getCoffeeAsset() {
        Random random = new Random();
        String resourceString = String.format(Locale.getDefault(), COFFEE_RESOURCE_ID,
                random.nextInt(MAX_COFFEE) + 1);
        return getAsset(resourceString);
    }

    private Assets getAsset(String resourceString) {
        int arrayId = mContext.getResources().getIdentifier(resourceString, "array",
                mContext.getPackageName());
        TypedArray array = mContext.getResources().obtainTypedArray(arrayId);
        //noinspection ResourceType
        String copyright = array.getString(0);
        //noinspection ResourceType
        String link = array.getString(1);
        String url;
        if (PreferencesUtility.getInstance().isHiResImageSupported()) {
//            url = array.getString(2); TODO: to reduce memory crashes
            //noinspection ResourceType
            url = array.getString(3);
        } else {
            //noinspection ResourceType
            url = array.getString(3);
        }
        //noinspection ResourceType
        String path = Utilities.getFilesDir(mContext) + array.getString(4);
        array.recycle();
        return new Assets(copyright, link, url, path);
    }
}
