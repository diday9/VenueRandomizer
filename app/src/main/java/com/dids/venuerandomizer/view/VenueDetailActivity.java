package com.dids.venuerandomizer.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.dids.venuerandomizer.R;
import com.dids.venuerandomizer.VenueRandomizerApplication;
import com.dids.venuerandomizer.controller.Utilities;
import com.dids.venuerandomizer.model.Category;
import com.dids.venuerandomizer.model.Venue;
import com.dids.venuerandomizer.view.adapter.SlidingImagePagerAdapter;
import com.dids.venuerandomizer.view.base.BaseActivity;
import com.dids.venuerandomizer.view.custom.TextDrawable;
import com.dids.venuerandomizer.view.fragment.MapViewFragment;

import java.util.List;

public class VenueDetailActivity extends BaseActivity implements View.OnClickListener,
        ViewPager.OnPageChangeListener {
    private static final String VERTICAL_POSITION_PROPERTY = "Y";
    private static final int VERTICAL_POSITION_BOUNCE_NORMAL = 270;
    private static final int VERTICAL_POSITION_BOUNCE_SHORT = 200;
    private static final int ANIMATION_DURATION = 700;
    private static final int ANIMATION_DURATION_OFFSET = 300;

    private Venue mVenue;
    private RadioGroup mRadioGroup;
    private ViewPager mViewPager;
    private MapViewFragment mMapFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.VenueDetailTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venue_detail);
        setToolbar(R.id.toolbar, true);
        mVenue = VenueRandomizerApplication.getInstance().getVenue();
        ImageView toolbarBg = (ImageView) findViewById(R.id.toolbar_bg);
        toolbarBg.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));

        /** Set name */
        TextView name = (TextView) findViewById(R.id.venue_name);
        name.setText(mVenue.getName());

        /** Set images */
        List<String> photoUrls = mVenue.getPhotoUrls();
        if (photoUrls != null && !photoUrls.isEmpty()) {
            mViewPager = (ViewPager) findViewById(R.id.view_pager);
            mViewPager.addOnPageChangeListener(this);
            mViewPager.setAdapter(new SlidingImagePagerAdapter(getSupportFragmentManager(), photoUrls));
            mViewPager.setOffscreenPageLimit(mViewPager.getAdapter().getCount());
        }

        /** Create radio group */
        createRadioButtonPages();
        updateRadioGroup();

        /** Set category */
        if (mVenue.getCategories() != null && !mVenue.getCategories().isEmpty()) {
            for (Category category : mVenue.getCategories()) {
                if (category.isPrimary()) {
                    TextView categoryName = (TextView) findViewById(R.id.category_name);
                    categoryName.setText(category.getName());
                    break;
                }
            }
        }

        /** Set status */
        TextView status = (TextView) findViewById(R.id.status);
        if (mVenue.getStatus() != null && !mVenue.getStatus().isEmpty()) {
            status.setText(mVenue.getStatus());
        } else {
            status.setVisibility(View.GONE);
        }

        /** Set rating */
        setRating();

        /** Set action buttons*/
        setupActionButtons();

        /** Setup map fragment */
        setupMapFragment();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_detail, menu);
        return true;
    }

    private void setupMapFragment() {
        mMapFragment = MapViewFragment.getInstance(mVenue.getName(),
                mVenue.getLatitude(), mVenue.getLongitude());
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, mMapFragment);
        ft.commit();
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
            case R.id.menu_item_facebook:
                String message = "Text I want to share.";
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                share.putExtra(Intent.EXTRA_TEXT, message);
                startActivity(Intent.createChooser(share, "Title of the dialog the system will open"));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.location:
                if (mMapFragment != null) {
                    mMapFragment.setCameraToLocation();
                }
                break;
            case R.id.telephone:
                intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + mVenue.getPhone()));
                startActivity(intent);
                break;
            case R.id.url:
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(mVenue.getUrl()));
                startActivity(intent);
                break;
            case R.id.twitter:
                try {
                    this.getPackageManager().getPackageInfo("com.twitter.android", 0);
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?screen_name=" +
                            mVenue.getTwitter()));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                } catch (Exception e) {
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/" +
                            mVenue.getTwitter()));
                }
                startActivity(intent);
                break;
        }
    }

    private void setRating() {
        LinearLayout ratingGroup = (LinearLayout) findViewById(R.id.rating_group);
        for (int rating = (int) mVenue.getRating(); rating > 0; rating -= 2) {
            ImageView star = new ImageView(this);
            star.setLayoutParams(new LayoutParams(Utilities.convertDPtoPixel(this, 20),
                    Utilities.convertDPtoPixel(this, 20)));
            if (rating >= 2) {
                star.setBackgroundResource(R.drawable.ic_star);
            } else {
                star.setBackgroundResource(R.drawable.ic_star_half);
            }
            ratingGroup.addView(star);
        }
    }

    private void createRadioButtonPages() {
        final RadioButton[] rb = new RadioButton[mVenue.getPhotoUrls().size()];
        mRadioGroup = (RadioGroup) findViewById(R.id.radiogroup);
        mRadioGroup.setOrientation(RadioGroup.HORIZONTAL);

        int size = (int) getResources().getDimension(R.dimen.radio_size);
        int margin = (int) getResources().getDimension(R.dimen.radio_margin);
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(size,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        param.gravity = Gravity.CENTER;
        param.leftMargin = margin;
        param.rightMargin = margin;

        for (int i = 0; i < mVenue.getPhotoUrls().size(); i++) {
            rb[i] = new RadioButton(this);
            mRadioGroup.addView(rb[i]);
            rb[i].setId(i);
            rb[i].setClickable(false);
            rb[i].setButtonDrawable(ContextCompat.getDrawable(this, R.drawable.selector_shape));
            rb[i].setLayoutParams(param);
        }
    }

    private void updateRadioGroup() {
        int id = mRadioGroup.getChildAt(mViewPager.getCurrentItem()).getId();
        RadioButton radioButton = (RadioButton) mRadioGroup.findViewById(id);
        radioButton.setChecked(true);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        updateRadioGroup();
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    private void setupActionButtons() {
        /** Location */
        FloatingActionButton actionButton = (FloatingActionButton) findViewById(R.id.location);
        actionButton.setOnClickListener(this);
        int animationOffset = animateView(actionButton, 0);

        /** Telephone */
        actionButton = (FloatingActionButton) findViewById(R.id.telephone);
        if (mVenue.getFormattedPhone() != null && !mVenue.getFormattedPhone().isEmpty()) {
            actionButton.setOnClickListener(this);
            animationOffset = animateView(actionButton, animationOffset);
        } else {
            actionButton.setVisibility(View.GONE);
        }

        /** Web */
        actionButton = (FloatingActionButton) findViewById(R.id.url);
        if (mVenue.getUrl() != null && !mVenue.getUrl().isEmpty()) {
            actionButton.setOnClickListener(this);
            animationOffset = animateView(actionButton, animationOffset);
        } else {
            actionButton.setVisibility(View.GONE);
        }

        /** Facebook */
        actionButton = (FloatingActionButton) findViewById(R.id.facebook);
        if (mVenue.getFacebook() != null && !mVenue.getFacebook().isEmpty()) {
            actionButton.setOnClickListener(this);
            actionButton.setImageDrawable(new TextDrawable(getResources(), "F", true));
            animationOffset = animateView(actionButton, animationOffset);
        } else {
            actionButton.setVisibility(View.GONE);
        }

        /** Twitter */
        actionButton = (FloatingActionButton) findViewById(R.id.twitter);
        if (mVenue.getTwitter() != null && !mVenue.getTwitter().isEmpty()) {
            actionButton.setImageDrawable(new TextDrawable(getResources(), "T", true));
            actionButton.setOnClickListener(this);
            animateView(actionButton, animationOffset);
        } else {
            actionButton.setVisibility(View.GONE);
        }
    }

    private int animateView(final View view, int offset) {
        boolean isNormal = mVenue.getStatus() != null && !mVenue.getStatus().isEmpty();
        ObjectAnimator moveAnim = ObjectAnimator.ofFloat(view, VERTICAL_POSITION_PROPERTY,
                isNormal ? VERTICAL_POSITION_BOUNCE_NORMAL : VERTICAL_POSITION_BOUNCE_SHORT);
        moveAnim.setDuration(ANIMATION_DURATION);
        moveAnim.setStartDelay(1000 + offset);
        moveAnim.setInterpolator(new BounceInterpolator());
        moveAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        moveAnim.start();
        return offset + ANIMATION_DURATION_OFFSET;
    }
}
