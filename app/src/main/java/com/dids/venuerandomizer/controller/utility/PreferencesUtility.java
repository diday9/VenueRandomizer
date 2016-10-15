package com.dids.venuerandomizer.controller.utility;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesUtility {
    private static final String PREF_NAME = "VenueRandomizer";
    private static final String HI_RES = "hires";
    private static final String DYNAMIC_IMAGES = "dynamic_images";
    private static final String MAX_IMAGE_COUNT = "max_image_count";
    private static final String LOCALE = "locale";
    private static final int DEFAULT_IMAGE_COUNT = 5;
    private static final String DEFAULT_LOCALE = "English";
    private static PreferencesUtility mSingleTon;
    private SharedPreferences mSharedPreferences;

    public static PreferencesUtility getInstance() {
        if (mSingleTon == null) {
            mSingleTon = new PreferencesUtility();
        }
        return mSingleTon;
    }

    public void init(Context context) {
        mSharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isHiResImageSupported() {
        return mSharedPreferences.getBoolean(HI_RES, false);
    }

    public void setHiResImageSupport(boolean set) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(HI_RES, set);
        editor.apply();
    }

    public boolean isDynamicImagesSupported() {
        return mSharedPreferences.getBoolean(DYNAMIC_IMAGES, false);
    }

    public void setDynamicImagesSupport(boolean set) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(DYNAMIC_IMAGES, set);
        editor.apply();
    }

    public int getMaxImageCount() {
        return mSharedPreferences.getInt(MAX_IMAGE_COUNT, DEFAULT_IMAGE_COUNT);
    }

    public void setMaxImageCount(int count) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(MAX_IMAGE_COUNT, count);
        editor.apply();
    }

    public String getLocale() {
        return mSharedPreferences.getString(LOCALE, DEFAULT_LOCALE);
    }

    public void setLocale(String locale) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(LOCALE, locale);
        editor.apply();
    }
}
