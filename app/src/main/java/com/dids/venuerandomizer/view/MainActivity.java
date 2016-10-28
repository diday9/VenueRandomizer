package com.dids.venuerandomizer.view;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.dids.venuerandomizer.R;
import com.dids.venuerandomizer.controller.network.FacebookWrapper;
import com.dids.venuerandomizer.controller.network.VolleySingleton;
import com.dids.venuerandomizer.controller.utility.Utilities;
import com.dids.venuerandomizer.view.adapter.MainViewPagerAdapter;
import com.dids.venuerandomizer.view.base.BaseActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;

public class MainActivity extends BaseActivity implements ViewPager.OnPageChangeListener,
        FirebaseAuth.AuthStateListener, Drawer.OnDrawerItemClickListener {
    public static final String SKIP_LOGIN = "skip_login";
    private static final String TAG = "MainActivity";
    private static final String COMMUNITY_PAGE = "findmeaplacecommunity";
    private static final int PERMISSION_REQUEST_ACCESS_LOCATION = 1;
    private ViewPager mViewPager;
    private FirebaseAuth mAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        if (!getIntent().getBooleanExtra(SKIP_LOGIN, false)) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            return;
        }
        mAuth = FirebaseAuth.getInstance();

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

        createDrawer();
    }

    private void createDrawer() {
        DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(final ImageView imageView, Uri uri, Drawable placeholder) {
                ImageRequest request = new ImageRequest(uri.toString(),
                        new Response.Listener<Bitmap>() {
                            @Override
                            public void onResponse(Bitmap bitmap) {
                                imageView.setImageBitmap(bitmap);
                            }
                        }, 0, 0, ImageView.ScaleType.CENTER, null,
                        new Response.ErrorListener() {
                            public void onErrorResponse(VolleyError error) {
                            }
                        });
                VolleySingleton.getInstance(MainActivity.this).addToRequestQueue(request);
            }

            @Override
            public void cancel(ImageView imageView) {
            }
        });
        AccountHeader header = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeightDp(200)
                .withHeaderBackground(Utilities.getDrawableFromAsset(this, "bg.jpg"))
                .withHeaderBackgroundScaleType(ImageView.ScaleType.CENTER_CROP)
                .withProfileImagesClickable(false)
                .addProfiles(
                        new ProfileDrawerItem().withName(mAuth.getCurrentUser().getDisplayName()).
                                withEmail(mAuth.getCurrentUser().getEmail()).
                                withIcon(mAuth.getCurrentUser().getPhotoUrl())
                )
                .build();
        PrimaryDrawerItem logout = new PrimaryDrawerItem().
                withName(R.string.drawer_logout).withIcon(R.drawable.ic_logout);
        PrimaryDrawerItem license = new PrimaryDrawerItem().
                withName(R.string.drawer_license).withIcon(R.drawable.ic_license);
        new DrawerBuilder()
                .withActivity(this)
                .withToolbar((Toolbar) findViewById(R.id.toolbar))
                .withActionBarDrawerToggle(true)
                .withAccountHeader(header)
                .addDrawerItems(
                        license, logout
                )
                .withOnDrawerItemClickListener(this)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d(TAG, "Requesting for location runtime permission");
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.
                    WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.
                        WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_ACCESS_LOCATION);
            } else {
                Log.v(TAG, "Location permission already granted");
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_ACCESS_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2]
                        == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Location permission has been granted");
                } else {
                    Log.d(TAG, "Location permission has been denied");
                }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.about:
                intent = new Intent(this, HelpActivity.class);
                intent.putExtra(HelpActivity.TAG_MODE, HelpActivity.ABOUT);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            case R.id.settings:
                intent = new Intent(this, SettingsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            case R.id.contribute:
                FacebookWrapper.launch(this, COMMUNITY_PAGE);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            // User is signed in
            Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
        } else {
            // User is signed out
            Log.d(TAG, "onAuthStateChanged:signed_out");
        }
    }

    @Override
    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
        Log.d(TAG, "position: " + position);
        Intent intent;
        switch (position) {
            case 1:
                intent = new Intent(this, HelpActivity.class);
                intent.putExtra(HelpActivity.TAG_MODE, HelpActivity.LICENSE);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            default:
        }
        return false;
    }
}
