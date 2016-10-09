package com.dids.venuerandomizer;

import android.app.Application;

import com.dids.venuerandomizer.controller.location.LocationManager;
import com.dids.venuerandomizer.controller.network.VolleyRequestQueue;
import com.dids.venuerandomizer.model.Venue;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class VenueRandomizer extends Application {
    private Venue mVenue;

    @Override
    public void onCreate() {
        super.onCreate();
        VolleyRequestQueue.getInstance(getApplicationContext());
        LocationManager.getInstance(this);
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);
    }

    public Venue getVenue() {
        return mVenue;
    }

    public void setVenue(Venue venue) {
        mVenue = venue;
    }
}
