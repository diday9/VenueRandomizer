package com.dids.venuerandomizer.view;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.dids.venuerandomizer.R;
import com.dids.venuerandomizer.view.adapter.MainViewPagerAdapter;
import com.dids.venuerandomizer.view.base.BaseActivity;

public class MainActivity extends BaseActivity implements ViewPager.OnPageChangeListener {
    private static final String TAG = "VenueRandomizer";
    private static final int PERMISSION_REQUEST_ACCESS_LOCATION = 1;
    private ViewPager mViewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setToolbar(R.id.toolbar, false);
        TextView toolbar = (TextView) findViewById(R.id.toolbar_text);
        toolbar.setText(R.string.app_name);

        mViewPager = (ViewPager) findViewById(R.id.pager_main);
        mViewPager.setAdapter(new MainViewPagerAdapter(this, getSupportFragmentManager()));
        mViewPager.addOnPageChangeListener(this);
        mViewPager.setOffscreenPageLimit(mViewPager.getAdapter().getCount());
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout_main);
        tabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            Log.d(TAG, "Requesting for location runtime permission");
//            // Get runtime permission
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
//                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
//                        Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_ACCESS_LOCATION);
//            } else {
//                Log.v(TAG, "Location permission already granted");
//            }
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_ACCESS_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Access granted
                    Log.d(TAG, "Location permission has been granted");
                } else {
                    Log.d(TAG, "Location permission has been denied");
                }
                return;
            }
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(mViewPager.getWindowToken(), 0);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
