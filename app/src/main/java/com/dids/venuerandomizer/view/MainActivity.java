package com.dids.venuerandomizer.view;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.dids.venuerandomizer.BuildConfig;
import com.dids.venuerandomizer.R;
import com.dids.venuerandomizer.controller.database.DatabaseHelper;
import com.dids.venuerandomizer.controller.network.FacebookWrapper;
import com.dids.venuerandomizer.controller.network.VolleySingleton;
import com.dids.venuerandomizer.controller.utility.PreferencesUtility;
import com.dids.venuerandomizer.controller.utility.Utilities;
import com.dids.venuerandomizer.model.UserData;
import com.dids.venuerandomizer.view.adapter.MainViewPagerAdapter;
import com.dids.venuerandomizer.view.base.BaseActivity;
import com.dids.venuerandomizer.view.fragment.AboutFragment;
import com.dids.venuerandomizer.view.fragment.HtmlFragment;
import com.dids.venuerandomizer.view.fragment.SettingsFragment;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
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
    private static final int MAX_LAUNCH_COUNT = 10;
    private ViewPager mViewPager;
    private FirebaseAuth mAuth;
    private ViewSwitcher mViewSwitcher;

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
        final DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        //noinspection ConstantConditions
        final UserData user = new UserData(mAuth.getCurrentUser().getDisplayName(),
                mAuth.getCurrentUser().getEmail());
        Query userQuery = dbHelper.createUserQuery(mAuth.getCurrentUser().getEmail());
        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() == 0) {
                    dbHelper.addUser(mAuth.getCurrentUser().getUid(), user);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        setContentView(R.layout.activity_main);
        setToolbar(R.id.toolbar, false);
        setToolbarTitle(R.string.app_name);

        mViewSwitcher = (ViewSwitcher) findViewById(R.id.view_switcher);

        mViewPager = (ViewPager) findViewById(R.id.pager_main);
        mViewPager.setAdapter(new MainViewPagerAdapter(this, getSupportFragmentManager()));
        mViewPager.addOnPageChangeListener(this);
        mViewPager.setOffscreenPageLimit(mViewPager.getAdapter().getCount());
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout_main);
        tabLayout.setupWithViewPager(mViewPager);

        createDrawer();

        PreferencesUtility util = PreferencesUtility.getInstance();
        if (util.getLaunchCount() == MAX_LAUNCH_COUNT) {
            util.setLaunchCount(0);
            showAppRater();
        } else {
            util.setLaunchCount(util.getLaunchCount() + 1);
        }
    }

    private void showAppRater() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.rate_title);
        builder.setMessage(R.string.rate_message);
        builder.setNeutralButton(R.string.rate_cancel, null);
        builder.setNegativeButton(R.string.rate_no, null);
        builder.setPositiveButton(R.string.rate_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" +
                        BuildConfig.APPLICATION_ID));
                startActivity(intent);
            }
        });
        builder.create().show();
    }

    private void setToolbarTitle(int resId) {
        TextView toolbar = (TextView) findViewById(R.id.toolbar_text);
        toolbar.setText(resId);
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
        //noinspection ConstantConditions
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
        PrimaryDrawerItem logout = new PrimaryDrawerItem().withSelectable(false).
                withName(R.string.drawer_logout).withIcon(R.drawable.ic_logout);
        PrimaryDrawerItem license = new PrimaryDrawerItem().
                withName(R.string.drawer_license).withIcon(R.drawable.ic_license);
        PrimaryDrawerItem about = new PrimaryDrawerItem().
                withName(R.string.title_about).withIcon(R.drawable.ic_about);
        PrimaryDrawerItem privacy = new PrimaryDrawerItem().
                withName(R.string.drawer_privacy).withIcon(R.drawable.ic_lock);
        PrimaryDrawerItem contact = new PrimaryDrawerItem().withSelectable(false).
                withName(R.string.drawer_contact).withIcon(R.drawable.ic_letter);
        PrimaryDrawerItem contribute = new PrimaryDrawerItem().withSelectable(false).
                withName(R.string.contribute).withIcon(R.drawable.ic_present);
        PrimaryDrawerItem settings = new PrimaryDrawerItem().
                withName(R.string.settings).withIcon(R.drawable.ic_settings);
        PrimaryDrawerItem find = new PrimaryDrawerItem().
                withName(R.string.drawer_find).withIcon(R.drawable.ic_find);
        PrimaryDrawerItem favorite = new PrimaryDrawerItem().
                withName(R.string.drawer_favorite).withIcon(R.drawable.ic_heart);
        new DrawerBuilder()
                .withActivity(this)
                .withToolbar((Toolbar) findViewById(R.id.toolbar))
                .withActionBarDrawerToggle(true)
                .withAccountHeader(header, true)
                .addDrawerItems(
                        find, favorite,
                        new DividerDrawerItem(),
                        settings,
                        new DividerDrawerItem(),
                        contribute, contact,
                        new DividerDrawerItem(),
                        about, privacy, license,
                        new DividerDrawerItem(),
                        logout
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
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        switch (position) {
            case 0:
                setToolbarTitle(R.string.app_name);
                if (mViewSwitcher.getDisplayedChild() == 1) {
                    mViewSwitcher.showPrevious();
                }
                ((MainViewPagerAdapter) mViewPager.getAdapter()).switchType(true);
                break;
            case 1:
                setToolbarTitle(R.string.drawer_favorite);
                if (mViewSwitcher.getDisplayedChild() == 1) {
                    mViewSwitcher.showPrevious();
                }
                ((MainViewPagerAdapter) mViewPager.getAdapter()).switchType(false);
                break;
            case 3:
                setToolbarTitle(R.string.settings);
                if (mViewSwitcher.getDisplayedChild() == 0) {
                    mViewSwitcher.showNext();
                }
                transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.main_container, SettingsFragment.newInstance());
                break;
            case 5:
                FacebookWrapper.launch(this, COMMUNITY_PAGE);
                break;
            case 6:
                intent = new Intent(Intent.ACTION_SEND);
                intent.setType("message/rfc822");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"tompee26@gmail.com"});
                intent.putExtra(Intent.EXTRA_SUBJECT, "Re: Find Me A Place");
                intent.putExtra(Intent.EXTRA_TEXT, "");
                startActivity(Intent.createChooser(intent, getString(R.string.drawer_contact)));
                break;
            case 8:
                setToolbarTitle(R.string.title_about);
                if (mViewSwitcher.getDisplayedChild() == 0) {
                    mViewSwitcher.showNext();
                }
                transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.main_container, AboutFragment.newInstance());
                break;
            case 9:
                setToolbarTitle(R.string.drawer_privacy);
                if (mViewSwitcher.getDisplayedChild() == 0) {
                    mViewSwitcher.showNext();
                }
                transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.main_container, HtmlFragment.newInstance(HtmlFragment.PRIVACY));
                break;
            case 10:
                setToolbarTitle(R.string.drawer_license);
                if (mViewSwitcher.getDisplayedChild() == 0) {
                    mViewSwitcher.showNext();
                }
                transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.main_container, HtmlFragment.newInstance(HtmlFragment.LICENSE));
                break;
            case 12:
                LoginManager.getInstance().logOut();
                intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                break;
            default:
        }
        transaction.commit();
        fragmentManager.executePendingTransactions();
        return false;
    }

    public void onUpdateFragmentData() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_container);
        if (fragment instanceof SettingsFragment) {
            ((SettingsFragment) fragment).updateData();
        }
    }
}
