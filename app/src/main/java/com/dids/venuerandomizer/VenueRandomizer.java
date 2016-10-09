package com.dids.venuerandomizer;

import android.app.Application;

import com.dids.venuerandomizer.controller.location.LocationManager;
import com.dids.venuerandomizer.controller.network.VolleyRequestQueue;

public class VenueRandomizer extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        VolleyRequestQueue.getInstance(getApplicationContext());
        LocationManager.getInstance(this);
    }
}
