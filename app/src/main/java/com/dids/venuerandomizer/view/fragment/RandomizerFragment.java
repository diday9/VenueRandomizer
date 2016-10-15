package com.dids.venuerandomizer.view.fragment;

import android.animation.Animator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
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
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.android.volley.toolbox.ImageLoader;
import com.dids.venuerandomizer.R;
import com.dids.venuerandomizer.VenueRandomizerApplication;
import com.dids.venuerandomizer.controller.network.FourSquareWrapper;
import com.dids.venuerandomizer.controller.network.VolleySingleton;
import com.dids.venuerandomizer.controller.task.GetVenueListTask;
import com.dids.venuerandomizer.controller.task.RefreshImageTask;
import com.dids.venuerandomizer.controller.utility.AnimationUtility;
import com.dids.venuerandomizer.controller.utility.AssetUtility;
import com.dids.venuerandomizer.controller.utility.Utilities;
import com.dids.venuerandomizer.model.Assets;
import com.dids.venuerandomizer.model.Category;
import com.dids.venuerandomizer.model.Venue;
import com.dids.venuerandomizer.view.VenueDetailActivity;
import com.dids.venuerandomizer.view.base.BaseActivity;
import com.dids.venuerandomizer.view.custom.NetworkImageView;
import com.dids.venuerandomizer.view.custom.TextDrawable;

import java.io.IOException;
import java.io.InputStream;

public class RandomizerFragment extends Fragment implements View.OnClickListener,
        GetVenueListTask.GetVenueListListener, Animator.AnimatorListener,
        RefreshImageTask.RefreshImageListener, ViewTreeObserver.OnGlobalLayoutListener {
    public static final int FOOD = 0;
    public static final int DRINKS = 1;
    public static final int COFFEE = 2;
    private static final int VERTICAL_OFFSET = 70;
    private static final int VERTICAL_POSITION_BOUNCE = 95;
    private static final int ANIMATION_DURATION = 700;
    private static final String TYPE = "type";

    private boolean mIsButtonGroupAnimated;
    private String mVenueId;
    private View mRootView;
    private ProgressBar mProgress;
    private FloatingActionButton mSearchButton;
    private GetVenueListTask mGetVenueListTask;
    private View mButtonGroup;
    private View mResultView;
    private FloatingActionButton mCheckout;

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
        mRootView = inflater.inflate(R.layout.fragment_randomizer, container, false);
        mRootView.getViewTreeObserver().addOnGlobalLayoutListener(this);

        mSearchButton = (FloatingActionButton) mRootView.findViewById(R.id.search_button);
        mSearchButton.setOnClickListener(this);

        /** Populate background image */
        loadDefaultAssets(mRootView);

        /** Populate other views */
        mProgress = (ProgressBar) mRootView.findViewById(R.id.progress_indicator);
        mSearchButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.
                getColor(getContext(), R.color.colorPrimaryAlpha)));
        mSearchButton.setImageDrawable(new TextDrawable(getContext().getResources(),
                getString(R.string.random_find_now), false));
        mButtonGroup = mRootView.findViewById(R.id.button_group);
        mIsButtonGroupAnimated = false;
        mResultView = mRootView.findViewById(R.id.result);
        mCheckout = (FloatingActionButton) mRootView.findViewById(R.id.checkout);
        mCheckout.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.
                getColor(getContext(), R.color.colorAccent)));
        mCheckout.setOnClickListener(this);
        return mRootView;
    }

    private void setupForDynamicView(final Assets asset) {
        final ViewSwitcher switcher = (ViewSwitcher) mRootView.findViewById(R.id.image_switcher);
        NetworkImageView.ImageLoaderListener listener = new NetworkImageView.ImageLoaderListener() {
            @Override
            public void onImageLoaded() {
                if (switcher.getDisplayedChild() == 0) {
                    switcher.showNext();
                } else {
                    switcher.showPrevious();
                }
                TextView textView = (TextView) mRootView.findViewById(R.id.copyright);
                textView.setText(asset.getCopyright());
                textView = (TextView) mRootView.findViewById(R.id.link);
                textView.setText(asset.getLink());
            }
        };
        NetworkImageView imageView;
        if (switcher.getDisplayedChild() == 0) {
            imageView = (NetworkImageView) mRootView.findViewById(R.id.image2);

        } else {
            imageView = (NetworkImageView) mRootView.findViewById(R.id.image1);
            imageView.setImageLoaderListener(listener);
        }
        imageView.setImageLoaderListener(listener);
        ImageLoader loader = VolleySingleton.getInstance(getContext()).getImageLoader();
        imageView.setImageUrl(asset.getUrl(), loader);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(mCheckout)) {
            Intent intent = new Intent(getContext(), VenueDetailActivity.class);
            Pair<View, String> name = Pair.create((mRootView.findViewById(R.id.venue_name)), "venue_name");
            Pair<View, String> category = Pair.create(mRootView.findViewById(R.id.category_name), "category");
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
                mGetVenueListTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, section, mVenueId);
            } else {
                mGetVenueListTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, section);
            }
        }
    }

    private void animateButtonGroup() {
        if (!mIsButtonGroupAnimated) {
            mIsButtonGroupAnimated = true;
            AnimationUtility.animateVerticalTranslation(mButtonGroup, Utilities.
                    convertDPtoPixel(getContext(), VERTICAL_OFFSET), ANIMATION_DURATION, this);
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
        TextView textView = (TextView) mRootView.findViewById(R.id.venue_name);
        textView.setText(venue.getName());
        mVenueId = venue.getId();
        textView = (TextView) mRootView.findViewById(R.id.category_name);
        if (venue.getCategories() != null && !venue.getCategories().isEmpty()) {
            for (Category category : venue.getCategories()) {
                if (category.isPrimary()) {
                    textView.setText(category.getName());
                    break;
                }
            }
        } else {
            textView.setVisibility(View.GONE);
        }
        StringBuilder address = new StringBuilder();
        if (venue.getCity() != null && !venue.getCity().isEmpty()) {
            address.append(venue.getCity());
            address.append(", ");
        }
        address.append(venue.getState());
        textView = (TextView) mRootView.findViewById(R.id.address);
        textView.setText(address.toString());
        textView = (TextView) mRootView.findViewById(R.id.telephone);
        if (venue.getFormattedPhone() != null && !venue.getFormattedPhone().isEmpty()) {
            textView.setText(venue.getFormattedPhone());
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.INVISIBLE);
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
    public void onLocationNotFound() {
        mProgress.setVisibility(View.GONE);
        mSearchButton.setEnabled(true);
        mGetVenueListTask = null;
        ((BaseActivity) getActivity()).interceptTouchEvents(false);
        Toast.makeText(getContext(), getString(R.string.random_no_location), Toast.LENGTH_SHORT).show();
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

    private void resetView() {
        if (mResultView != null) {
            mResultView.setVisibility(View.GONE);
        }
        if (mCheckout != null) {
            mCheckout.setVisibility(View.INVISIBLE);
        }
        if (mCheckout != null && mIsButtonGroupAnimated) {
            AnimationUtility.animateVerticalPosition(mCheckout, -Utilities.
                    convertDPtoPixel(getContext(), VERTICAL_POSITION_BOUNCE), 0);
        }
        if (mButtonGroup != null && mIsButtonGroupAnimated) {
            mIsButtonGroupAnimated = false;
            AnimationUtility.animateVerticalTranslation(mButtonGroup, 0, 0);
        }
    }

    @Override
    public void onAnimationStart(Animator animation) {
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        ((BaseActivity) getActivity()).interceptTouchEvents(false);
        mCheckout.setVisibility(View.VISIBLE);
        AnimationUtility.animateVerticalPosition(mCheckout, Utilities.
                        convertDPtoPixel(getContext(), VERTICAL_POSITION_BOUNCE),
                ANIMATION_DURATION, new BounceInterpolator());
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
            ImageView imageView = (ImageView) view.findViewById(R.id.image1);
            imageView.setImageDrawable(d);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNewImageSelected(Assets food, Assets drinks, Assets coffee) {
        Assets asset;
        switch (getArguments().getInt(TYPE, FOOD)) {
            case DRINKS:
                asset = drinks;
                break;
            case COFFEE:
                asset = coffee;
                break;
            default:
                asset = food;
                break;
        }
        setupForDynamicView(asset);
    }

    @Override
    public void onGlobalLayout() {
        mRootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        Assets asset;
        switch (getArguments().getInt(TYPE, FOOD)) {
            case DRINKS:
                asset = AssetUtility.getInstance().getDrinksAsset();
                break;
            case COFFEE:
                asset = AssetUtility.getInstance().getCoffeeAsset();
                break;
            default:
                asset = AssetUtility.getInstance().getFoodAsset();
                break;
        }
        setupForDynamicView(asset);
        RefreshImageTask.getInstance().addListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RefreshImageTask.getInstance().cancel(true);
    }
}
