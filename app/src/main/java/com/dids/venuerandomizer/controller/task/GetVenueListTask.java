package com.dids.venuerandomizer.controller.task;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.NoConnectionError;
import com.dids.venuerandomizer.controller.location.LocationManager;
import com.dids.venuerandomizer.controller.network.FourSquareWrapper;
import com.dids.venuerandomizer.model.Venue;

import java.util.List;
import java.util.Random;

public class GetVenueListTask extends AsyncTask<String, Void, Venue> {
    private final Context mContext;
    private final GetVenueListListener mListener;
    private boolean mIsLocationEnabled;
    private boolean mIsInternetConnected;

    public GetVenueListTask(Context context, GetVenueListListener listener) {
        mContext = context;
        mListener = listener;
        mIsLocationEnabled = false;
        mIsInternetConnected = true;
    }

    @Override
    protected Venue doInBackground(String... args) {
        LocationManager manager = LocationManager.getInstance(mContext);
        mIsLocationEnabled = manager.isLocationEnabled();
        if (!mIsLocationEnabled) {
            return null;
        }
        Location location = manager.getLocation();
        if (location == null) {
            Log.d("tompee", "location is null");
            /** TODO handle null location */
            return null;
        }
        FourSquareWrapper wrapper = new FourSquareWrapper(mContext);
        Venue venue;
        try {
            List<Venue> list = wrapper.getVenueList(location, args[0]);
            if (list == null || list.isEmpty()) {
                return null;
            }
            Random random = new Random();
            venue = list.get(random.nextInt(list.size()));
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
        mListener.onCompleted(venue);
    }

    public interface GetVenueListListener {
        void onCompleted(Venue venue);

        void onLocationDisabled();

        void onConnectionError();
    }
}
