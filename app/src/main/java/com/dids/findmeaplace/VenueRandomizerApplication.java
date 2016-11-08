package com.dids.findmeaplace;

import android.app.Application;

import com.dids.findmeaplace.controller.location.LocationManager;
import com.dids.findmeaplace.controller.network.VolleySingleton;
import com.dids.findmeaplace.controller.task.RefreshImageTask;
import com.dids.findmeaplace.controller.utility.AssetUtility;
import com.dids.findmeaplace.controller.utility.PreferencesUtility;
import com.dids.findmeaplace.model.Venue;
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
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
