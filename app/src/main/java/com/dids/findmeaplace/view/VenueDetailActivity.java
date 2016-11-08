package com.dids.findmeaplace.view;

import android.animation.Animator;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
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

import com.dids.findmeaplace.R;
import com.dids.findmeaplace.VenueRandomizerApplication;
import com.dids.findmeaplace.controller.database.DatabaseHelper;
import com.dids.findmeaplace.controller.network.FacebookWrapper;
import com.dids.findmeaplace.controller.network.FourSquareWrapper;
import com.dids.findmeaplace.controller.utility.AnimationUtility;
import com.dids.findmeaplace.controller.utility.Utilities;
import com.dids.findmeaplace.model.DatabaseVenue;
import com.dids.findmeaplace.model.Venue;
import com.dids.findmeaplace.view.adapter.SlidingImagePagerAdapter;
import com.dids.findmeaplace.view.base.BaseActivity;
import com.dids.findmeaplace.view.custom.TextDrawable;
import com.dids.findmeaplace.view.fragment.MapViewFragment;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class VenueDetailActivity extends BaseActivity implements View.OnClickListener,
        ViewPager.OnPageChangeListener, FacebookCallback<Sharer.Result> {
    public static final String VARIANT = "variant";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int VERTICAL_POSITION_BOUNCE = 88;
    private static final int ANIMATION_DURATION = 700;
    private static final int ANIMATION_DURATION_DELAY = 1000;
    private static final int ANIMATION_DURATION_DELAY_OFFSET = 300;

    private Venue mVenue;
    private DatabaseHelper mDbHelper;
    private RadioGroup mRadioGroup;
    private ViewPager mViewPager;
    private MapViewFragment mMapFragment;
    private File mPhotoFile;
    private CallbackManager mFbCallBackManager;
    private FirebaseAuth mAuth;
    private boolean mIsFavorite;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.VenueDetailTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venue_detail);
        setToolbar(R.id.toolbar, true);
        mVenue = VenueRandomizerApplication.getInstance().getVenue();
        ImageView toolbarBg = (ImageView) findViewById(R.id.toolbar_bg);
        toolbarBg.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));

        mFbCallBackManager = CallbackManager.Factory.create();
        mAuth = FirebaseAuth.getInstance();
        mDbHelper = DatabaseHelper.getInstance();

        /** Set name */
        TextView name = (TextView) findViewById(R.id.venue_name);
        name.setText(mVenue.getName());

        /** Set images */
        List<String> photoUrls = mVenue.getPhotoUrls();
        if (photoUrls != null && !photoUrls.isEmpty()) {
            mViewPager = (ViewPager) findViewById(R.id.view_pager);
            mViewPager.addOnPageChangeListener(this);
            mViewPager.setAdapter(new SlidingImagePagerAdapter(getSupportFragmentManager(), photoUrls));
        }

        /** Create radio group */
        createRadioButtonPages();
        updateRadioGroup();

        /** Set category */
        if (mVenue.getCategories() != null && !mVenue.getCategories().isEmpty()) {
            String category = Utilities.getPrimaryCategory(mVenue);
            if (category != null) {
                TextView categoryName = (TextView) findViewById(R.id.category_name);
                categoryName.setText(category);
            }
        }

        /** Set status */
        TextView status = (TextView) findViewById(R.id.status);
        if (mVenue.getStatus() != null && !mVenue.getStatus().isEmpty()) {
            status.setText(mVenue.getStatus());
        } else {
            status.setVisibility(View.INVISIBLE);
        }

        /** Set rating */
        setRating();

        /** Set action buttons*/
        setupActionButtons();

        /** Setup map fragment */
        setupMapFragment();

        //noinspection ConstantConditions
        Query favoriteList = mDbHelper.createFavoriteQuery(mAuth.getCurrentUser().getUid(),
                mVenue.getId());
        favoriteList.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mIsFavorite = dataSnapshot.getChildrenCount() != 0;
                invalidateOptionsMenu();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_detail, menu);

        MenuItem favorite = menu.findItem(R.id.menu_favorite);
        if (mIsFavorite) {
            favorite.setIcon(R.drawable.ic_favorite_full);
        } else {
            favorite.setIcon(R.drawable.ic_favorite_border);
        }
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
            case R.id.menu_item_share:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.share_fb_message);
                builder.setPositiveButton(R.string.control_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dispatchTakePictureIntent();
                    }
                });
                builder.setNegativeButton(R.string.control_cancel, null);
                builder.create().show();
                break;
            case R.id.menu_foursquare:
                FourSquareWrapper.launch(this, mVenue.getId());
                break;
            case R.id.menu_favorite:
                //noinspection ConstantConditions
                Query favoriteQuery = mDbHelper.createFavoriteQuery(mAuth.getCurrentUser().getUid(),
                        mVenue.getId());
                favoriteQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getChildrenCount() == 0) {
                            mDbHelper.addFavorite(mAuth.getCurrentUser().getUid(),
                                    new DatabaseVenue(mVenue.getId(), mVenue.getName(),
                                            Utilities.getPrimaryCategory(mVenue),
                                            Utilities.getAddress(mVenue), mVenue.getFormattedPhone(),
                                            getIntent().getIntExtra(VARIANT, 0)));
                            mIsFavorite = true;
                        } else {
                            mIsFavorite = false;
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                snapshot.getRef().removeValue();
                            }
                        }
                        invalidateOptionsMenu();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
                invalidateOptionsMenu();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            mPhotoFile = null;
            try {
                mPhotoFile = Utilities.createImageFile(this);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (mPhotoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.tompee.utilities.findmeaplace.fileprovider", mPhotoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
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
            case R.id.facebook:
                FacebookWrapper.launch(this, mVenue.getFacebookUsername());
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
        AnimationUtility.animateVerticalPosition(view, Utilities.convertDPtoPixel(this,
                VERTICAL_POSITION_BOUNCE), ANIMATION_DURATION, new BounceInterpolator(),
                ANIMATION_DURATION_DELAY + offset,
                new Animator.AnimatorListener() {

                    @Override
                    public void onAnimationStart(Animator animator) {
                        view.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {

                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
        return offset + ANIMATION_DURATION_DELAY_OFFSET;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            /** Get original bitmap */
            Bitmap bitmap = Utilities.decodeSampledBitmapFromResource(mPhotoFile.getAbsolutePath());

            /** Get EXIF info */
            ExifInterface exif;
            try {
                exif = new ExifInterface(mPhotoFile.getAbsolutePath());
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_UNDEFINED);
                bitmap = Utilities.rotateBitmap(bitmap, orientation);
                FacebookWrapper.share(this, mVenue, bitmap, mFbCallBackManager, this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            mFbCallBackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onSuccess(Sharer.Result result) {
        //noinspection ResultOfMethodCallIgnored
        mPhotoFile.delete();
    }

    @Override
    public void onCancel() {
        //noinspection ResultOfMethodCallIgnored
        mPhotoFile.delete();
    }

    @Override
    public void onError(FacebookException error) {
        //noinspection ResultOfMethodCallIgnored
        mPhotoFile.delete();
    }
}