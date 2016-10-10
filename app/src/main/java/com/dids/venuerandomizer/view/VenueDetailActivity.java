package com.dids.venuerandomizer.view;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dids.venuerandomizer.R;
import com.dids.venuerandomizer.VenueRandomizerApplication;
import com.dids.venuerandomizer.model.Category;
import com.dids.venuerandomizer.model.Venue;
import com.dids.venuerandomizer.view.base.BaseActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

public class VenueDetailActivity extends BaseActivity implements View.OnClickListener,
        OnMapReadyCallback {
    private Venue mVenue;
    private MapView mMapView;
    private boolean mIsMapDisplayed;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venue_detail);
        setToolbar(R.id.toolbar, true);
        ImageView toolbar = (ImageView) findViewById(R.id.toolbar_bg);
        toolbar.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        mVenue = ((VenueRandomizerApplication) getApplication()).getVenue();

        TextView venueName = (TextView) findViewById(R.id.venue_name);
        venueName.setText(mVenue.getName());

        ImageLoader loader = ImageLoader.getInstance();

        /** Set images */
        setImages(loader);

        /** Initialize map */
        initializeMap(savedInstanceState);

        /** Set category */
        if (mVenue.getCategories() != null && !mVenue.getCategories().isEmpty()) {
            for (Category category : mVenue.getCategories()) {
                if (category.isPrimary()) {
                    TextView categoryName = (TextView) findViewById(R.id.category_name);
                    ImageView imageView = (ImageView) findViewById(R.id.category_icon);
                    loader.displayImage(category.getIconPrefix() + "bg_88" +
                            category.getIconSuffix(), imageView);
                    categoryName.setText(category.getName());
                    break;
                }
            }
        }

        /** Set Address */
        mIsMapDisplayed = false;
        View addressGroup = findViewById(R.id.address_group);
        addressGroup.setOnClickListener(this);
        TextView address = (TextView) findViewById(R.id.address_name);
        address.setText(mVenue.getCity() + ", " + mVenue.getState());

        /** Set telephone */
        View telephoneGroup = findViewById(R.id.telephone_group);
        if (mVenue.getFormattedPhone() != null && !mVenue.getFormattedPhone().isEmpty()) {
            TextView telephone = (TextView) findViewById(R.id.telephone);
            telephone.setText(mVenue.getFormattedPhone());
            telephoneGroup.setOnClickListener(this);
        } else {
            telephoneGroup.setVisibility(View.GONE);
        }

        /** Set URL */
        View urlGroup = findViewById(R.id.url_group);
        if (mVenue.getUrl() != null && !mVenue.getUrl().isEmpty()) {
            TextView url = (TextView) findViewById(R.id.url);
            url.setText(mVenue.getUrl());
            urlGroup.setOnClickListener(this);
        } else {
            urlGroup.setVisibility(View.GONE);
        }

        /** Set status */
        View statusGroup = findViewById(R.id.status_group);
        if (mVenue.getStatus() != null && !mVenue.getStatus().isEmpty()) {
            TextView status = (TextView) findViewById(R.id.status);
            status.setText(mVenue.getStatus());
        } else {
            statusGroup.setVisibility(View.GONE);
        }

        /** Set rating */
        View ratingGroup = findViewById(R.id.rating_group);
        if (mVenue.getRating() != 0) {
            TextView rating = (TextView) findViewById(R.id.rating);
            rating.setText(String.valueOf(mVenue.getRating()));
        } else {
            ratingGroup.setVisibility(View.GONE);
        }

        /** Set Facebook */
        View fbGroup = findViewById(R.id.fb_group);
        if (mVenue.getFacebookUsername() != null && !mVenue.getFacebookUsername().isEmpty()) {
            TextView fb = (TextView) findViewById(R.id.fb);
            fb.setText(String.format(getString(R.string.venue_detail_facebook),
                    mVenue.getFacebookUsername()));
            fbGroup.setOnClickListener(this);
        } else {
            fbGroup.setVisibility(View.GONE);
        }

        /** Set Twitter */
        View twitterGroup = findViewById(R.id.twitter_group);
        if (mVenue.getTwitter() != null && !mVenue.getTwitter().isEmpty()) {
            TextView twitter = (TextView) findViewById(R.id.twitter);
            twitter.setText(String.format(getString(R.string.venue_detail_twitter),
                    mVenue.getTwitter()));
            twitterGroup.setOnClickListener(this);
        } else {
            twitterGroup.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    finish();
                } else {
                    finishAfterTransition();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.address_group:
                if (mIsMapDisplayed) {
                    mMapView.setVisibility(View.GONE);
                } else {
                    mMapView.setVisibility(View.VISIBLE);
                }
                mIsMapDisplayed = !mIsMapDisplayed;
                break;
            case R.id.telephone_group:
                Intent callIntent = new Intent(Intent.ACTION_DIAL,
                        Uri.parse("tel:" + mVenue.getPhone()));
                startActivity(callIntent);
                break;
            case R.id.url_group:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mVenue.getUrl()));
                startActivity(browserIntent);
                break;
            case R.id.twitter_group:
                Intent twitterIntent;
                try {
                    getPackageManager().getPackageInfo("com.twitter.android", 0);
                    twitterIntent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("twitter://user?screen_name=" + mVenue.getTwitter()));
                    twitterIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                } catch (Exception e) {
                    twitterIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/"
                            + mVenue.getTwitter()));
                }
                startActivity(twitterIntent);
                break;
            case R.id.fb_group:
                Intent fbIntent;
                try {
                    PackageManager packageManager = getPackageManager();
                    ApplicationInfo applicationInfo = packageManager.
                            getApplicationInfo("com.facebook.katana", 0);
                    if (applicationInfo.enabled) {
                        fbIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://facewebmodal/f?href=" +
                                "http://www.facebook.com/" + mVenue.getFacebookUsername()));
                    } else {
                        fbIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.facebook.com/"
                                + mVenue.getFacebookUsername()));
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    fbIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.facebook.com/"
                            + mVenue.getFacebookUsername()));
                }
                startActivity(fbIntent);
        }
    }

    private void setImages(ImageLoader loader) {
        List<String> photoUrls = mVenue.getPhotoUrls();
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder().
                cacheInMemory(false).
                cacheOnDisk(false).build();
        if (photoUrls != null && !photoUrls.isEmpty()) {
            ImageView image1 = (ImageView) findViewById(R.id.image1);
            ImageView image2 = (ImageView) findViewById(R.id.image2);
            ImageView image3 = (ImageView) findViewById(R.id.image3);
            switch (photoUrls.size()) {
                case 1:
                    loader.displayImage(photoUrls.get(0), image1, defaultOptions);
                    image2.setVisibility(View.GONE);
                    image3.setVisibility(View.GONE);
                    break;
                case 2:
                    loader.displayImage(photoUrls.get(0), image1, defaultOptions);
                    loader.displayImage(photoUrls.get(1), image2, defaultOptions);
                    image3.setVisibility(View.GONE);
                    break;
                default:
                    loader.displayImage(photoUrls.get(0), image1, defaultOptions);
                    loader.displayImage(photoUrls.get(1), image2, defaultOptions);
                    loader.displayImage(photoUrls.get(2), image3, defaultOptions);
                    break;
            }
        }
    }

    private void initializeMap(Bundle savedInstanceState) {
        MapsInitializer.initialize(this);
        mMapView = (MapView) findViewById(R.id.mapview);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        LatLng latLng = new LatLng(mVenue.getLongitude(), mVenue.getLatitude());
        googleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(mVenue.getName()));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        mMapView.onLowMemory();
        super.onLowMemory();
    }
}
