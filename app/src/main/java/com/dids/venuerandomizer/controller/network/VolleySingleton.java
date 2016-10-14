package com.dids.venuerandomizer.controller.network;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

public class VolleySingleton {
    private static VolleySingleton mSingleton;
    private static Context mContext;
    private final ImageLoader mImageLoader;
    private RequestQueue mRequestQueue;

    private VolleySingleton(Context context) {
        mContext = context;
        mRequestQueue = getRequestQueue();
        mImageLoader = new ImageLoader(this.mRequestQueue, new LruBitmapCache(LruBitmapCache.
                getCacheSize(mContext)));
    }

    public static synchronized VolleySingleton getInstance(Context context) {
        if (mSingleton == null) {
            mSingleton = new VolleySingleton(context);
        }
        return mSingleton;
    }

    private RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mContext);
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }
}
