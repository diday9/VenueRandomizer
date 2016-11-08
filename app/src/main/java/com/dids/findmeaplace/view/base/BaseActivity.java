package com.dids.findmeaplace.view.base;

import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;

public class BaseActivity extends FragmentActivity {
    private static final int MIN_WIDTH = 600;
    private AppCompatDelegate mDelegate;
    private boolean mIsTouchIntercepted;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isFullLayoutSupported()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        mDelegate = AppCompatDelegate.create(this, null);
        mDelegate.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(int layoutResID) {
        mDelegate.setContentView(layoutResID);
    }

    @Override
    public void invalidateOptionsMenu() {
        mDelegate.invalidateOptionsMenu();
    }

    protected void setToolbar(int toolbarId, boolean enableHomeButton) {
        Toolbar toolbar = (Toolbar) findViewById(toolbarId);
        mDelegate.setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        mDelegate.getSupportActionBar().setDisplayShowTitleEnabled(false);
        mDelegate.getSupportActionBar().setDisplayHomeAsUpEnabled(enableHomeButton);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU && mDelegate.getSupportActionBar() != null) {
            mDelegate.getSupportActionBar().openOptionsMenu();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean isFullLayoutSupported() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealSize(size);
        } else {
            display.getSize(size);
        }
        size.y = (int) (size.y / displayMetrics.density);
        size.x = (int) (size.x / displayMetrics.density);
        int rotation = display.getRotation();
        if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {
            int temp = size.x;
            //noinspection SuspiciousNameCombination
            size.x = size.y;
            size.y = temp;
        }
        return size.x >= MIN_WIDTH;
    }

    public void interceptTouchEvents(boolean intercept) {
        mIsTouchIntercepted = intercept;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return mIsTouchIntercepted || super.dispatchTouchEvent(ev);
    }
}
