package com.dids.venuerandomizer.view;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.MenuItem;
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
