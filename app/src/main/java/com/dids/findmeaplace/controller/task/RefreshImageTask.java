package com.dids.findmeaplace.controller.task;

import android.os.AsyncTask;
import android.util.Log;

import com.dids.findmeaplace.controller.utility.AssetUtility;
import com.dids.findmeaplace.controller.utility.PreferencesUtility;
import com.dids.findmeaplace.model.Assets;

import java.util.HashSet;
import java.util.Set;

public class RefreshImageTask extends AsyncTask<Void, Assets, Void> {
    private static final String TAG = "RefreshImageTask";
    private static final int DELAY = 20000;
    private static final int MAX_RETRY_COUNT = 5;
    private static RefreshImageTask mSingleton;
    private final Set<RefreshImageListener> mListenerList;

    private RefreshImageTask() {
        mListenerList = new HashSet<>();
        executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static RefreshImageTask getInstance() {
        if (mSingleton == null) {
            mSingleton = new RefreshImageTask();
        }
        return mSingleton;
    }

    public void addListener(RefreshImageListener listener) {
        mListenerList.add(listener);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Assets foodAsset = AssetUtility.getInstance().getFoodAsset();
        Assets drinksAsset = AssetUtility.getInstance().getDrinksAsset();
        Assets coffeeAsset = AssetUtility.getInstance().getCoffeeAsset();
        while (!isCancelled()) {
            foodAsset = getUniqueFoodAsset(foodAsset);
            drinksAsset = getUniqueDrinksAsset(drinksAsset);
            coffeeAsset = getUniqueCoffeeAsset(coffeeAsset);
            try {
                Thread.sleep(DELAY);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (PreferencesUtility.getInstance().isDynamicImagesSupported()) {
                Log.d(TAG, "New asset available");
                publishProgress(foodAsset, drinksAsset, coffeeAsset);
            }
        }
        return null;
    }

    private Assets getUniqueFoodAsset(Assets asset) {
        Assets newAsset = asset;
        for (int count = 0; count < MAX_RETRY_COUNT; count++) {
            newAsset = AssetUtility.getInstance().getFoodAsset();
            if (!newAsset.equals(asset)) {
                break;
            }
        }
        return newAsset;
    }

    private Assets getUniqueCoffeeAsset(Assets asset) {
        Assets newAsset = asset;
        for (int count = 0; count < MAX_RETRY_COUNT; count++) {
            newAsset = AssetUtility.getInstance().getCoffeeAsset();
            if (!newAsset.equals(asset)) {
                break;
            }
        }
        return newAsset;
    }

    private Assets getUniqueDrinksAsset(Assets asset) {
        Assets newAsset = asset;
        for (int count = 0; count < MAX_RETRY_COUNT; count++) {
            newAsset = AssetUtility.getInstance().getDrinksAsset();
            if (!newAsset.equals(asset)) {
                break;
            }
        }
        return newAsset;
    }

    @Override
    protected void onProgressUpdate(Assets... assets) {
        for (RefreshImageListener listener : mListenerList) {
            listener.onNewImageSelected(assets[0], assets[1], assets[2]);
        }
    }

    public interface RefreshImageListener {
        void onNewImageSelected(Assets food, Assets drinks, Assets coffee);
    }
}
