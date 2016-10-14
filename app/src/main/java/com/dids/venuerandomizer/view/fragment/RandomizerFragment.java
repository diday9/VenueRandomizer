package com.dids.venuerandomizer.view.fragment;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.android.volley.toolbox.ImageLoader;
import com.dids.venuerandomizer.R;
import com.dids.venuerandomizer.VenueRandomizerApplication;
import com.dids.venuerandomizer.controller.network.FourSquareWrapper;
import com.dids.venuerandomizer.controller.network.VolleySingleton;
import com.dids.venuerandomizer.controller.task.GetVenueListTask;
import com.dids.venuerandomizer.model.Assets;
import com.dids.venuerandomizer.model.Category;
import com.dids.venuerandomizer.model.Venue;
import com.dids.venuerandomizer.view.VenueDetailActivity;
import com.dids.venuerandomizer.view.base.BaseActivity;
import com.dids.venuerandomizer.view.custom.EventNetworkImageView;
import com.dids.venuerandomizer.view.custom.TextDrawable;

import java.io.IOException;
import java.io.InputStream;

public class RandomizerFragment extends Fragment implements View.OnClickListener,
        GetVenueListTask.GetVenueListListener, Animator.AnimatorListener {
    public static final int FOOD = 0;
    public static final int DRINKS = 1;
    public static final int COFFEE = 2;
    private static final String VERTICAL_TRANSLATION_PROPERTY = "translationY";
    private static final String VERTICAL_POSITION_PROPERTY = "Y";
    private static final int VERTICAL_OFFSET = 200;
    private static final int VERTICAL_POSITION_BOUNCE_NORMAL = 300;
    private static final int VERTICAL_POSITION_BOUNCE_SHORT = 240;
    private static final int ANIMATION_DURATION = 700;
    private static final String TYPE = "type";

    private boolean mIsButtonGroupAnimated;
    private String mVenueId;
    private ProgressBar mProgress;
    private FloatingActionButton mSearchButton;
    private GetVenueListTask mGetVenueListTask;
    private View mButtonGroup;
    private View mResultView;
    private TextView mVenueName;
    private TextView mCategoryName;
    private TextView mAddress;
    private TextView mTelephone;
    private FloatingActionButton mCheckout;

    private EventNetworkImageView mImageView;

    public static RandomizerFragment newInstance(int type) {
        RandomizerFragment fragment = new RandomizerFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(TYPE, type);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_randomizer, container, false);
        mSearchButton = (FloatingActionButton) view.findViewById(R.id.search_button);
        mSearchButton.setOnClickListener(this);

        /** Populate background image */
        loadDefaultAssets(view);
        final ViewSwitcher switcher = (ViewSwitcher) view.findViewById(R.id.image_switcher);
        mImageView = (EventNetworkImageView) view.findViewById(R.id.background);
        mImageView.setImageLoaderListener(new EventNetworkImageView.ImageLoaderListener() {
            @Override
            public void onImageLoaded() {
                switcher.showNext();
                setAttributions(view);
            }
        });
        mImageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                loadImage();
            }
        });

        /** Populate other views */
        mProgress = (ProgressBar) view.findViewById(R.id.progress_indicator);
        mSearchButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.
                getColor(getContext(), R.color.colorPrimaryAlpha)));
        mSearchButton.setImageDrawable(new TextDrawable(getContext().getResources(),
                getString(R.string.random_find_now), false));
        mButtonGroup = view.findViewById(R.id.button_group);
        mIsButtonGroupAnimated = false;
        mResultView = view.findViewById(R.id.result);
        mCheckout = (FloatingActionButton) view.findViewById(R.id.checkout);
        mCheckout.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.
                getColor(getContext(), R.color.colorAccent)));
        mCheckout.setOnClickListener(this);
        mVenueName = (TextView) view.findViewById(R.id.venue_name);
        mCategoryName = (TextView) view.findViewById(R.id.category_name);
        mAddress = (TextView) view.findViewById(R.id.address);
        mTelephone = (TextView) view.findViewById(R.id.telephone);
        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.equals(mCheckout)) {
            Intent intent = new Intent(getContext(), VenueDetailActivity.class);
            Pair<View, String> name = Pair.create((View) mVenueName, "venue_name");
            Pair<View, String> category = Pair.create((View) mCategoryName, "category");
            Pair<View, String> card = Pair.create((View) mCheckout, "card");
            //noinspection unchecked
            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation(getActivity(), card, name, category);
            startActivity(intent, options.toBundle());
            return;
        }
        if (mGetVenueListTask == null) {
            ((BaseActivity) getActivity()).interceptTouchEvents(true);
            mProgress.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getContext(),
                    R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
            mProgress.setVisibility(View.VISIBLE);
            mSearchButton.setEnabled(false);
            String section;
            switch (getArguments().getInt(TYPE, FOOD)) {
                case DRINKS:
                    section = FourSquareWrapper.SECTION_DRINKS;
                    break;
                case COFFEE:
                    section = FourSquareWrapper.SECTION_COFFEE;
                    break;
                default:
                    section = FourSquareWrapper.SECTION_FOOD;
                    break;
            }
            mGetVenueListTask = new GetVenueListTask(getContext(), this);
            if (mVenueId != null) {
                mGetVenueListTask.execute(section, mVenueId);
            } else {
                mGetVenueListTask.execute(section);
            }
        }
    }

    private void animateButtonGroup() {
        if (!mIsButtonGroupAnimated) {
            mIsButtonGroupAnimated = true;
            ObjectAnimator anim = ObjectAnimator.ofFloat(mButtonGroup, VERTICAL_TRANSLATION_PROPERTY,
                    VERTICAL_OFFSET);
            anim.setDuration(ANIMATION_DURATION);
            anim.addListener(this);
            anim.start();
        } else {
            ((BaseActivity) getActivity()).interceptTouchEvents(false);
        }
        mResultView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCompleted(Venue venue) {
        mProgress.setVisibility(View.GONE);
        mSearchButton.setEnabled(true);
        mGetVenueListTask = null;
        if (venue != null) {
            VenueRandomizerApplication.getInstance().setVenue(venue);
            setVenue(venue);
            animateButtonGroup();
        } else {
            ((BaseActivity) getActivity()).interceptTouchEvents(false);
        }
    }

    private void setVenue(Venue venue) {
        mVenueName.setText(venue.getName());
        mVenueId = venue.getId();
        if (venue.getCategories() != null && !venue.getCategories().isEmpty()) {
            for (Category category : venue.getCategories()) {
                if (category.isPrimary()) {
                    mCategoryName.setText(category.getName());
                    break;
                }
            }
        } else {
            mCategoryName.setVisibility(View.GONE);
        }
        StringBuilder address = new StringBuilder();
        if (venue.getCity() != null && !venue.getCity().isEmpty()) {
            address.append(venue.getCity());
            address.append(", ");
        }
        address.append(venue.getState());
        mAddress.setText(address.toString());
        if (venue.getFormattedPhone() != null && !venue.getFormattedPhone().isEmpty()) {
            mTelephone.setText(venue.getFormattedPhone());
            mTelephone.setVisibility(View.VISIBLE);
        } else {
            mTelephone.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLocationDisabled() {
        mProgress.setVisibility(View.GONE);
        mSearchButton.setEnabled(true);
        mGetVenueListTask = null;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.random_gps_disabled);
        builder.setMessage(R.string.random_gps_disabled_msg);
        builder.setPositiveButton(R.string.control_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
        builder.setNegativeButton(R.string.control_cancel, null);
        builder.create().show();
        ((BaseActivity) getActivity()).interceptTouchEvents(false);
    }

    @Override
    public void onConnectionError() {
        mProgress.setVisibility(View.GONE);
        mSearchButton.setEnabled(true);
        mGetVenueListTask = null;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.random_no_internet);
        builder.setMessage(R.string.random_no_internet_msg);
        builder.setPositiveButton(R.string.control_ok, null);
        builder.create().show();
        ((BaseActivity) getActivity()).interceptTouchEvents(false);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser) {
            resetView();
        }
    }

    private void loadImage() {
        VenueRandomizerApplication app = VenueRandomizerApplication.getInstance();
        Assets asset;
        switch (getArguments().getInt(TYPE, FOOD)) {
            case DRINKS:
                asset = app.getDrinksAsset();
                break;
            case COFFEE:
                asset = app.getCoffeeAsset();
                break;
            default:
                asset = app.getFoodAsset();
                break;
        }
        ImageLoader loader = VolleySingleton.getInstance(getContext()).getImageLoader();
        mImageView.setImageUrl(asset.getUrl(), loader);
    }

    private void setAttributions(View view) {
        VenueRandomizerApplication app = VenueRandomizerApplication.getInstance();
        Assets asset;
        switch (getArguments().getInt(TYPE, FOOD)) {
            case DRINKS:
                asset = app.getDrinksAsset();
                break;
            case COFFEE:
                asset = app.getCoffeeAsset();
                break;
            default:
                asset = app.getFoodAsset();
                break;
        }
        TextView textView = (TextView) view.findViewById(R.id.copyright);
        textView.setText(asset.getCopyright());
        textView = (TextView) view.findViewById(R.id.link);
        textView.setText(asset.getLink());
    }

    private void resetView() {
        if (mResultView != null) {
            mResultView.setVisibility(View.GONE);
        }
        if (mCheckout != null) {
            mCheckout.setVisibility(View.INVISIBLE);
        }
        if (mCheckout != null && mIsButtonGroupAnimated) {
            ObjectAnimator anim = ObjectAnimator.ofFloat(mCheckout,
                    VERTICAL_POSITION_PROPERTY, -VERTICAL_POSITION_BOUNCE_NORMAL);
            anim.setDuration(0);
            anim.start();
        }
        if (mButtonGroup != null && mIsButtonGroupAnimated) {
            mIsButtonGroupAnimated = false;
            ObjectAnimator anim = ObjectAnimator.ofFloat(mButtonGroup,
                    VERTICAL_TRANSLATION_PROPERTY, 0);
            anim.setDuration(0);
            anim.start();
        }
    }

    @Override
    public void onAnimationStart(Animator animation) {
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        ((BaseActivity) getActivity()).interceptTouchEvents(false);
        mCheckout.setVisibility(View.VISIBLE);
        ObjectAnimator moveAnim = ObjectAnimator.ofFloat(mCheckout, VERTICAL_POSITION_PROPERTY,
                mTelephone.getVisibility() == View.VISIBLE ? VERTICAL_POSITION_BOUNCE_NORMAL :
                        VERTICAL_POSITION_BOUNCE_SHORT);
        moveAnim.setDuration(ANIMATION_DURATION);
        moveAnim.setInterpolator(new BounceInterpolator());
        moveAnim.start();
    }

    @Override
    public void onAnimationCancel(Animator animation) {
    }

    @Override
    public void onAnimationRepeat(Animator animation) {
    }

    private void loadDefaultAssets(View view) {
        String type;
        switch (getArguments().getInt(TYPE, FOOD)) {
//            case DRINKS: TODO enable when default image is available
//                type = FourSquareWrapper.SECTION_DRINKS;
//                break;
            case COFFEE:
                type = FourSquareWrapper.SECTION_COFFEE;
                break;
            default:
                type = FourSquareWrapper.SECTION_FOOD;
        }

        int arrayId = getContext().getResources().getIdentifier("default_" + type, "array",
                getContext().getPackageName());
        TypedArray array = getContext().getResources().obtainTypedArray(arrayId);
        TextView textView = (TextView) view.findViewById(R.id.copyright);
        //noinspection ResourceType
        textView.setText(array.getString(0));
        textView = (TextView) view.findViewById(R.id.link);
        //noinspection ResourceType
        textView.setText(array.getString(1));
        array.recycle();

        try {
            InputStream ims = getContext().getAssets().open(String.format("default_%s.jpg", type));
            Drawable d = Drawable.createFromStream(ims, null);
            ImageView imageView = (ImageView) view.findViewById(R.id.default_image);
            imageView.setImageDrawable(d);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
