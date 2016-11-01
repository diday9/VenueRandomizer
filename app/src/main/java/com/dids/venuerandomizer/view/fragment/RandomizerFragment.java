package com.dids.venuerandomizer.view.fragment;

import android.animation.Animator;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
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
import android.view.animation.BounceInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dids.venuerandomizer.R;
import com.dids.venuerandomizer.VenueRandomizerApplication;
import com.dids.venuerandomizer.controller.network.FourSquareWrapper;
import com.dids.venuerandomizer.controller.task.GetVenueListTask;
import com.dids.venuerandomizer.controller.utility.AnimationUtility;
import com.dids.venuerandomizer.controller.utility.Utilities;
import com.dids.venuerandomizer.model.Venue;
import com.dids.venuerandomizer.view.VenueDetailActivity;
import com.dids.venuerandomizer.view.base.BaseActivity;
import com.dids.venuerandomizer.view.custom.TextDrawable;
import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.SuperActivityToast;

public class RandomizerFragment extends Fragment implements View.OnClickListener,
        GetVenueListTask.GetVenueListListener, Animator.AnimatorListener {
    private static final String VARIANT = "variant";
    private static final int VERTICAL_OFFSET = 70;
    private static final int VERTICAL_POSITION_BOUNCE = 95;
    private static final int ANIMATION_DURATION = 700;

    private GetVenueListTask mGetVenueListTask;
    private boolean mIsButtonGroupAnimated;
    private String mVenueId;

    private View mRootView;
    private FloatingActionButton mSearchButton;
    private ProgressBar mProgress;
    private FloatingActionButton mCheckout;
    private View mButtonGroup;
    private View mResultView;

    public static RandomizerFragment newInstance(int type) {
        RandomizerFragment fragment = new RandomizerFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(VARIANT, type);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_randomizer, container, false);

        mSearchButton = (FloatingActionButton) mRootView.findViewById(R.id.search_button);
        mSearchButton.setOnClickListener(this);
        mSearchButton.setImageDrawable(new TextDrawable(getContext().getResources(),
                getString(R.string.random_find_now), false));

        mProgress = (ProgressBar) mRootView.findViewById(R.id.progress_indicator);

        mCheckout = (FloatingActionButton) mRootView.findViewById(R.id.checkout);
        mCheckout.setOnClickListener(this);

        mButtonGroup = mRootView.findViewById(R.id.button_group);
        mIsButtonGroupAnimated = false;
        mResultView = mRootView.findViewById(R.id.result);
        return mRootView;
    }

    @Override
    public void onClick(View view) {
        if (view.equals(mCheckout)) {
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
            switch (getArguments().getInt(VARIANT, MainFragment.FOOD)) {
                case MainFragment.DRINKS:
                    section = FourSquareWrapper.SECTION_DRINKS;
                    break;
                case MainFragment.COFFEE:
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
            SuperActivityToast.create(getActivity(), new Style(), Style.TYPE_BUTTON)
                    .setProgressBarColor(Color.WHITE)
                    .setText(getString(R.string.random_no_venue))
                    .setDuration(Style.DURATION_LONG)
                    .setFrame(Style.FRAME_LOLLIPOP)
                    .setColor(ContextCompat.getColor(getContext(), R.color.colorPrimary))
                    .setAnimations(Style.ANIMATIONS_POP).show();
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
        SuperActivityToast.create(getActivity(), new Style(), Style.TYPE_BUTTON)
                .setProgressBarColor(Color.WHITE)
                .setText(getString(R.string.random_no_location))
                .setDuration(Style.DURATION_LONG)
                .setFrame(Style.FRAME_LOLLIPOP)
                .setColor(ContextCompat.getColor(getContext(), R.color.colorPrimary))
                .setAnimations(Style.ANIMATIONS_POP).show();
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

    private void setVenue(Venue venue) {
        TextView textView = (TextView) mRootView.findViewById(R.id.venue_name);
        textView.setText(venue.getName());
        mVenueId = venue.getId();
        textView = (TextView) mRootView.findViewById(R.id.category_name);
        if (venue.getCategories() != null && !venue.getCategories().isEmpty()) {
            String category = Utilities.getPrimaryCategory(venue);
            if (category != null) {
                textView.setText(category);
            }
        } else {
            textView.setVisibility(View.GONE);
        }
        textView = (TextView) mRootView.findViewById(R.id.address);
        textView.setText(Utilities.getAddress(venue));
        textView = (TextView) mRootView.findViewById(R.id.telephone);
        if (venue.getFormattedPhone() != null && !venue.getFormattedPhone().isEmpty()) {
            textView.setText(venue.getFormattedPhone());
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.INVISIBLE);
        }
    }

    public void resetView() {
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
    public void onAnimationStart(Animator animator) {

    }

    @Override
    public void onAnimationEnd(Animator animator) {
        ((BaseActivity) getActivity()).interceptTouchEvents(false);
        mCheckout.setVisibility(View.VISIBLE);
        AnimationUtility.animateVerticalPosition(mCheckout, Utilities.
                        convertDPtoPixel(getContext(), VERTICAL_POSITION_BOUNCE),
                ANIMATION_DURATION, new BounceInterpolator());
    }

    @Override
    public void onAnimationCancel(Animator animator) {

    }

    @Override
    public void onAnimationRepeat(Animator animator) {

    }
}
