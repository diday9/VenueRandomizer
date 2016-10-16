package com.dids.venuerandomizer.controller.task;

import android.os.AsyncTask;

import com.dids.venuerandomizer.controller.utility.AssetUtility;
import com.dids.venuerandomizer.controller.utility.PreferencesUtility;
import com.dids.venuerandomizer.model.Assets;

import java.util.HashSet;
import java.util.Set;

public class RefreshImageTask extends AsyncTask<Void, Assets, Void> {
    private static final int DELAY = 20000;
    private static RefreshImageTask mSingleton;
    private final Set<RefreshImageListener> mListenerList;

    private RefreshImageTask() {
        mListenerList = new HashSet<>();
        execute();
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
        while (!isCancelled()) {
            Assets foodAsset = AssetUtility.getInstance().getFoodAsset();
            Assets drinksAsset = AssetUtility.getInstance().getDrinksAsset();
            Assets coffeeAsset = AssetUtility.getInstance().getCoffeeAsset();
            try {
                Thread.sleep(DELAY);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (PreferencesUtility.getInstance().isDynamicImagesSupported()) {
                publishProgress(foodAsset, drinksAsset, coffeeAsset);
            }
        }
        return null;
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
