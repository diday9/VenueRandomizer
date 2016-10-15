package com.dids.venuerandomizer;

import android.app.Application;

import com.dids.venuerandomizer.controller.location.LocationManager;
import com.dids.venuerandomizer.controller.network.VolleySingleton;
import com.dids.venuerandomizer.controller.task.RefreshImageTask;
import com.dids.venuerandomizer.controller.utility.AssetUtility;
import com.dids.venuerandomizer.controller.utility.PreferencesUtility;
import com.dids.venuerandomizer.model.Venue;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

public class VenueRandomizerApplication extends Application {
    private static VenueRandomizerApplication mInstance;
    private Venue mVenue;

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
        AssetUtility.getInstance().init(this);
        RefreshImageTask.getInstance();
    }

    public Venue getVenue() {
        return mVenue;
    }

    public void setVenue(Venue venue) {
        mVenue = venue;
    }
}
