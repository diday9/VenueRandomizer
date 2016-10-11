package com.dids.venuerandomizer.controller;

import android.content.Context;

public class Utilities {

    public static int convertDPtoPixel(Context context, int dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}
