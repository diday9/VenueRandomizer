package com.dids.venuerandomizer.controller.task;

import android.content.Context;
import android.os.AsyncTask;

import com.android.volley.NoConnectionError;
import com.dids.venuerandomizer.controller.network.FourSquareWrapper;
import com.dids.venuerandomizer.model.Venue;

public class GetVenueTask extends AsyncTask<String, Void, Venue> {
    private final Context mContext;
    private final GetVenueListener mListener;
    private boolean mIsInternetConnected;

    public GetVenueTask(Context context, GetVenueListener listener) {
        mContext = context;
        mListener = listener;
        mIsInternetConnected = true;
    }

    @Override
    protected void onPreExecute() {
        mListener.onStarted();
    }

    @Override
    protected Venue doInBackground(String... args) {
        FourSquareWrapper wrapper = new FourSquareWrapper(mContext);
        Venue venue;
        try {
            venue = wrapper.getVenueById(args[0]);
        } catch (NoConnectionError noConnectionError) {
            mIsInternetConnected = false;
            return null;
        }
        return venue;
    }

    @Override
    protected void onPostExecute(Venue venue) {
        if (!mIsInternetConnected) {
            mListener.onConnectionError();
            return;
        }
        mListener.onCompleted(venue);
    }

    public interface GetVenueListener {
        void onStarted();

        void onCompleted(Venue venue);

        void onConnectionError();
    }
}
