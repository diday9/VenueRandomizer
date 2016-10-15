package com.dids.venuerandomizer.controller.utility;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesUtility {
    private static final String PREF_NAME = "VenueRandomizer";
    private static final String DATA_SAVER = "data_saver";
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

    public boolean isDataSaverModeOn() {
        return mSharedPreferences.getBoolean(DATA_SAVER, false);
    }
}
