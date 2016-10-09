package com.dids.venuerandomizer.controller.task;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;

import com.android.volley.NoConnectionError;
import com.dids.venuerandomizer.controller.location.LocationManager;
import com.dids.venuerandomizer.controller.network.FourSquareWrapper;

public class GetVenueListTask extends AsyncTask<String, Void, Void> {
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
    protected Void doInBackground(String... args) {
        LocationManager manager = LocationManager.getInstance(mContext);
        mIsLocationEnabled = manager.isLocationEnabled();
        if (!mIsLocationEnabled) {
            return null;
        }
        Location location = manager.getLocation();
        if (location == null) {
            /** TODO handle null location */
            return null;
        }
        FourSquareWrapper wrapper = new FourSquareWrapper(mContext);
        try {
            wrapper.getVenueList(location, args[0]);
        } catch (NoConnectionError e) {
            mIsInternetConnected = false;
            return null;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (!mIsLocationEnabled) {
            mListener.onLocationDisabled();
            return;
        }
        if (!mIsInternetConnected) {
            mListener.onConnectionError();
            return;
        }
        mListener.onCompleted();
    }

    public interface GetVenueListListener {
        void onCompleted();

        void onLocationDisabled();

        void onConnectionError();
    }
}
