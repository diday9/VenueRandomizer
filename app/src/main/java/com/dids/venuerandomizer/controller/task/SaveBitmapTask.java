package com.dids.venuerandomizer.controller.task;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.dids.venuerandomizer.controller.utility.Utilities;

public class SaveBitmapTask extends AsyncTask<Bitmap, Void, Void> {
    private final String mPath;

    public SaveBitmapTask(String path) {
        mPath = path;
    }

    @Override
    protected Void doInBackground(Bitmap... bitmaps) {
        Utilities.saveBitmap(mPath, bitmaps[0]);
        return null;
    }
}
