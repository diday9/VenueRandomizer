package com.dids.venuerandomizer.view;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
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

public class VenueDetailActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venue_detail);
        setToolbar(R.id.toolbar, true);
        ImageView toolbar = (ImageView) findViewById(R.id.toolbar_bg);
        toolbar.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        Venue venue = ((VenueRandomizer) getApplication()).getVenue();

        TextView venueName = (TextView) findViewById(R.id.venue_name);
        venueName.setText(venue.getName());

        ImageLoader loader = ImageLoader.getInstance();
        /** Set category */
        if (venue.getCategories() != null && !venue.getCategories().isEmpty()) {
            for (Category category : venue.getCategories()) {
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
        address.setText(venue.getCity() + ", " + venue.getState());

        /** Set telephone */
        View telephoneGroup = findViewById(R.id.telephone_group);
        if (venue.getFormattedPhone() != null && !venue.getFormattedPhone().isEmpty()) {
            TextView telephone = (TextView) findViewById(R.id.telephone);
            telephone.setText(venue.getFormattedPhone());
        } else {
            telephoneGroup.setVisibility(View.GONE);
        }

        /** Set URL */
        View urlGroup = findViewById(R.id.url_group);
        if (venue.getUrl() != null && !venue.getUrl().isEmpty()) {
            TextView url = (TextView) findViewById(R.id.url);
            url.setText(venue.getUrl());
        } else {
            urlGroup.setVisibility(View.GONE);
        }

        /** Set status */
        View statusGroup = findViewById(R.id.status_group);
        if (venue.getStatus() != null && !venue.getStatus().isEmpty()) {
            TextView status = (TextView) findViewById(R.id.status);
            status.setText(venue.getStatus());
        } else {
            statusGroup.setVisibility(View.GONE);
        }

        /** Set rating */
        View ratingGroup = findViewById(R.id.rating_group);
        if (venue.getRating() != 0) {
            TextView rating = (TextView) findViewById(R.id.rating);
            rating.setText(String.valueOf(venue.getRating()));
        } else {
            ratingGroup.setVisibility(View.GONE);
        }

        /** Set Facebook */
        View fbGroup = findViewById(R.id.fb_group);
        if (venue.getFacebookUsername() != null && !venue.getFacebookUsername().isEmpty()) {
            TextView fb = (TextView) findViewById(R.id.fb);
            fb.setText(String.format(getString(R.string.venue_detail_facebook),
                    venue.getFacebookUsername()));
        } else {
            fbGroup.setVisibility(View.GONE);
        }

        /** Set Twitter */
        View twitterGroup = findViewById(R.id.twitter_group);
        if (venue.getTwitter() != null && !venue.getTwitter().isEmpty()) {
            TextView twitter = (TextView) findViewById(R.id.twitter);
            twitter.setText(venue.getTwitter());
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
}
