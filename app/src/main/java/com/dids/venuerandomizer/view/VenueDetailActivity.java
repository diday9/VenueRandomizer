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
import com.dids.venuerandomizer.VenueRandomizer;
import com.dids.venuerandomizer.model.Category;
import com.dids.venuerandomizer.model.Venue;
import com.dids.venuerandomizer.view.base.BaseActivity;
import com.nostra13.universalimageloader.core.ImageLoader;

public class VenueDetailActivity extends BaseActivity implements View.OnClickListener {
    private Venue mVenue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venue_detail);
        setToolbar(R.id.toolbar, true);
        ImageView toolbar = (ImageView) findViewById(R.id.toolbar_bg);
        toolbar.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        mVenue = ((VenueRandomizer) getApplication()).getVenue();

        TextView venueName = (TextView) findViewById(R.id.venue_name);
        venueName.setText(mVenue.getName());

        ImageLoader loader = ImageLoader.getInstance();
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
}
