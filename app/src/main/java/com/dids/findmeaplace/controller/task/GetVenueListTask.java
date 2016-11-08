package com.dids.findmeaplace.controller.task;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;

import com.android.volley.NoConnectionError;
import com.dids.findmeaplace.controller.location.LocationManager;
import com.dids.findmeaplace.controller.network.FourSquareWrapper;
import com.dids.findmeaplace.model.Venue;

public class GetVenueListTask extends AsyncTask<String, Void, Venue> {
    private static final int MAX_RETRY = 5;
    private static final int DELAY = 1000;
    private final Context mContext;
    private final GetVenueListListener mListener;
    private boolean mIsLocationEnabled;
    private boolean mIsInternetConnected;
    private boolean mIsLocationFound;

    public GetVenueListTask(Context context, GetVenueListListener listener) {
        mContext = context;
        mListener = listener;
        mIsLocationEnabled = false;
        mIsInternetConnected = true;
        mIsLocationFound = true;
    }

    @Override
    protected Venue doInBackground(String... args) {
        LocationManager manager = LocationManager.getInstance(mContext);
        mIsLocationEnabled = manager.isLocationEnabled();
        if (!mIsLocationEnabled) {
            return null;
        }
        Location location = manager.getLocation();
        for (int count = 0; location == null && count < MAX_RETRY; count++) {
            try {
                Thread.sleep(DELAY);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            location = manager.getLocation();
        }
        if (location == null) {
            mIsLocationFound = false;
            return null;
        }
        FourSquareWrapper wrapper = new FourSquareWrapper(mContext);
        Venue venue;
        try {
            if (args.length == 2) {
                venue = wrapper.getRandomVenue(args[1], location, args[0]);
            } else {
                venue = wrapper.getRandomVenue(location, args[0]);
            }
        } catch (NoConnectionError e) {
            mIsInternetConnected = false;
            return null;
        }
        return venue;
    }

    @Override
    protected void onPostExecute(Venue venue) {
        if (!mIsLocationEnabled) {
            mListener.onLocationDisabled();
            return;
        }
        if (!mIsInternetConnected) {
            mListener.onConnectionError();
            return;
        }
        if (!mIsLocationFound) {
            mListener.onLocationNotFound();
            return;
        }
        mListener.onCompleted(venue);
    }

    public interface GetVenueListListener {
        void onCompleted(Venue venue);

        void onLocationDisabled();

        void onLocationNotFound();

        void onConnectionError();
    }
}
